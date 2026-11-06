package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class CompressionRepository(
    private val context: Context,
    private val dao: CompressionDao,
    private val engine: UniversalCompressionEngine = UniversalCompressionEngine(context)
) {
    val allHistory: Flow<List<CompressionRecord>> = dao.getAllRecords()

    /**
     * Copies a content:// Uri picked by the user into app cache and inspects its metadata.
     */
    suspend fun loadFromContentUri(uri: Uri): SelectedFileInfo = withContext(Dispatchers.IO) {
        val resolvedName = queryDisplayName(uri) ?: "Selected_File_${System.currentTimeMillis()}"
        val cacheDir = File(context.cacheDir, "picked_inputs").apply { mkdirs() }
        val localFile = File(cacheDir, resolvedName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(localFile).use { output ->
                input.copyTo(output)
            }
        }
        engine.inspectFile(localFile, displayNameOverride = resolvedName)
    }

    suspend fun compressSingleFile(
        source: SelectedFileInfo,
        compressionPercent: Int,
        resolutionScalePercent: Int,
        targetOutputFormatOverride: String?
    ): CompressionResult {
        val result = engine.compressSingleFile(
            source = source,
            compressionPercent = compressionPercent,
            resolutionScalePercent = resolutionScalePercent,
            targetOutputFormatOverride = targetOutputFormatOverride
        )
        val insertedId = dao.insertRecord(result.record).toInt()
        return result.copy(record = result.record.copy(id = insertedId))
    }

    suspend fun mergeAndCompressPdfs(
        pdfs: List<SelectedFileInfo>,
        compressionPercent: Int,
        resolutionScalePercent: Int,
        customOutputName: String?
    ): CompressionResult {
        val result = engine.mergeAndCompressPdfs(
            pdfs = pdfs,
            compressionPercent = compressionPercent,
            resolutionScalePercent = resolutionScalePercent,
            customOutputName = customOutputName
        )
        val insertedId = dao.insertRecord(result.record).toInt()
        return result.copy(record = result.record.copy(id = insertedId))
    }

    /**
     * Exports a compressed file directly to the device's public Downloads folder.
     */
    suspend fun downloadToDevice(record: CompressionRecord): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceFile = File(record.localCachePath)
            require(sourceFile.exists()) { "Compressed file no longer exists in local cache." }

            val mimeType = resolveMimeType(record.compressedFileName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, record.compressedFileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SqueezeStudio")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, values)
                    ?: error("Could not create entry in MediaStore.Downloads")

                resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)

                if (record.id > 0) {
                    dao.updateDownloadedUri(record.id, itemUri.toString())
                }
                "Downloads/SqueezeStudio/${record.compressedFileName}"
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SqueezeStudio"
                ).apply { mkdirs() }
                val destFile = File(downloadsDir, record.compressedFileName)
                sourceFile.copyTo(destFile, overwrite = true)
                if (record.id > 0) {
                    dao.updateDownloadedUri(record.id, Uri.fromFile(destFile).toString())
                }
                "Downloads/SqueezeStudio/${destFile.name}"
            }
        }
    }

    /**
     * Launches the Android system Share sheet for the compressed file via FileProvider.
     */
    fun shareCompressedFile(record: CompressionRecord): Result<Unit> {
        return runCatching {
            val file = File(record.localCachePath)
            require(file.exists()) { "Compressed file not found." }
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = resolveMimeType(record.compressedFileName)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Compressed with SqueezeStudio: ${record.compressedFileName} (${formatFileSize(record.originalSizeBytes)} → ${formatFileSize(record.compressedSizeBytes)}, -${record.savedPercentage}%)"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share ${record.compressedFileName}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    suspend fun deleteHistoryItem(id: Int) = withContext(Dispatchers.IO) {
        runCatching {
            val record = dao.getRecordById(id)
            if (record != null) {
                val file = File(record.localCachePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            dao.deleteRecordById(id)
        }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        runCatching {
            dao.clearAllRecords()
            val outputsDir = File(context.filesDir, "compressed_outputs")
            if (outputsDir.exists()) {
                outputsDir.listFiles()?.forEach { it.delete() }
            }
            val inputsDir = File(context.cacheDir, "picked_inputs")
            if (inputsDir.exists()) {
                inputsDir.listFiles()?.forEach { it.delete() }
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun resolveMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        return when (ext) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
            "odp" -> "application/vnd.oasis.opendocument.presentation"
            "epub" -> "application/epub+zip"
            "zip" -> "application/zip"
            "gz", "gzip" -> "application/gzip"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "html" -> "text/html"
            "csv" -> "text/csv"
            "txt", "md" -> "text/plain"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        }
    }
}
