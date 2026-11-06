package com.example.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

enum class ToolCategory(
    val displayName: String,
    val shortTitle: String,
    val description: String,
    val badgeLabel: String
) {
    IMAGE(
        displayName = "Universal Image Studio",
        shortTitle = "Images",
        description = "JPG, PNG, WEBP, BMP, HEIC, GIF & SVG optimization",
        badgeLabel = "8+ Image Formats"
    ),
    PDF(
        displayName = "PDF Shrink & Dual Merger",
        shortTitle = "PDF & Merge",
        description = "Single PDF shrink OR combine 2 PDFs into 1 compressed PDF",
        badgeLabel = "Single + 2-PDF Merge"
    ),
    OFFICE_EBOOK(
        displayName = "Office, OpenDoc & E-Books",
        shortTitle = "Office & Docs",
        description = "DOCX, PPTX, XLSX, ODT, ODS, ODP & EPUB media compaction",
        badgeLabel = "7+ Document Suites"
    ),
    TEXT_ARCHIVE(
        displayName = "Text, Data & Archives",
        shortTitle = "Archives & Data",
        description = "ZIP, GZIP, JAR, JSON, XML, HTML, CSV, TXT, RTF & MD",
        badgeLabel = "10+ Data & ZIP Formats"
    )
}

enum class StudioOperationMode(val label: String) {
    COMPRESS("Compress"),
    CONVERT("Convert")
}

enum class QualityPreset(
    val title: String,
    val subtitle: String,
    val compressionPercent: Int,
    val resolutionScalePercent: Int
) {
    EXTREME(
        title = "Max Squeeze",
        subtitle = "~75% smaller • High reduction",
        compressionPercent = 80,
        resolutionScalePercent = 60
    ),
    BALANCED(
        title = "Balanced",
        subtitle = "~55% smaller • Recommended",
        compressionPercent = 55,
        resolutionScalePercent = 80
    ),
    HIGH_QUALITY(
        title = "High Fidelity",
        subtitle = "~30% smaller • Crisp detail",
        compressionPercent = 30,
        resolutionScalePercent = 95
    ),
    CUSTOM(
        title = "Custom Dial",
        subtitle = "Fine-tuned slider & target size",
        compressionPercent = 50,
        resolutionScalePercent = 85
    )
}

data class SupportedFormatInfo(
    val extension: String,
    val name: String,
    val category: ToolCategory,
    val mimeTypes: List<String>,
    val techniqueSummary: String
)

data class SmartFormatRecommendation(
    val recommendedFormat: String,
    val recommendedPreset: QualityPreset,
    val rationale: String,
    val isAutoDetected: Boolean = true
)

object FormatCatalog {
    val allFormats: List<SupportedFormatInfo> = listOf(
        // Images
        SupportedFormatInfo("JPG", "JPEG Image", ToolCategory.IMAGE, listOf("image/jpeg"), "Chroma subsampling & DCT quantization"),
        SupportedFormatInfo("JPEG", "JPEG High-Res", ToolCategory.IMAGE, listOf("image/jpeg"), "Adaptive Huffman & resolution scaling"),
        SupportedFormatInfo("PNG", "Portable Network Graphics", ToolCategory.IMAGE, listOf("image/png"), "Bit-depth quantization & DEFLATE stream"),
        SupportedFormatInfo("WEBP", "Google WebP Image", ToolCategory.IMAGE, listOf("image/webp"), "Predictive lossy/lossless block coding"),
        SupportedFormatInfo("BMP", "Uncompressed Bitmap", ToolCategory.IMAGE, listOf("image/bmp", "image/x-ms-bmp"), "Raw raster to compressed WebP/JPEG"),
        SupportedFormatInfo("HEIC", "High Efficiency Image", ToolCategory.IMAGE, listOf("image/heic", "image/heif"), "HEIF decode & cross-format re-encoding"),
        SupportedFormatInfo("GIF", "Graphics Interchange", ToolCategory.IMAGE, listOf("image/gif"), "Palette & frame dimension optimization"),
        SupportedFormatInfo("SVG", "Scalable Vector XML", ToolCategory.IMAGE, listOf("image/svg+xml"), "XML metadata strip & path minification"),
        // PDFs
        SupportedFormatInfo("PDF", "Portable Document Format", ToolCategory.PDF, listOf("application/pdf"), "Page bitmap DPI scaling & stream compression"),
        SupportedFormatInfo("2-PDF", "Dual-PDF Merge + Compress", ToolCategory.PDF, listOf("application/pdf"), "Sequential multi-PDF page unification & shrink"),
        // Office, OpenDocument & E-Books
        SupportedFormatInfo("DOCX", "Word OpenXML Document", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), "word/media image downscale & Level-9 DEFLATE"),
        SupportedFormatInfo("PPTX", "PowerPoint Presentation", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.openxmlformats-officedocument.presentationml.presentation"), "ppt/media slide asset squeeze & XML pack"),
        SupportedFormatInfo("XLSX", "Excel Workbook", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), "xl/media compaction & sharedStrings DEFLATE"),
        SupportedFormatInfo("ODT", "OpenDocument Text", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.oasis.opendocument.text"), "Pictures/ container re-encoding & ZIP level 9"),
        SupportedFormatInfo("ODS", "OpenDocument Spreadsheet", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.oasis.opendocument.spreadsheet"), "Embedded object & XML stream compaction"),
        SupportedFormatInfo("ODP", "OpenDocument Presentation", ToolCategory.OFFICE_EBOOK, listOf("application/vnd.oasis.opendocument.presentation"), "Slide graphic re-compression & archive pack"),
        SupportedFormatInfo("EPUB", "Digital E-Book Publication", ToolCategory.OFFICE_EBOOK, listOf("application/epub+zip"), "OEBPS image downscale & XHTML minification"),
        // Text, Data & Archives
        SupportedFormatInfo("ZIP", "ZIP Archive Container", ToolCategory.TEXT_ARCHIVE, listOf("application/zip"), "Recursive inner media squeeze & Level-9 DEFLATE"),
        SupportedFormatInfo("GZIP", "GNU Zip Stream (.gz)", ToolCategory.TEXT_ARCHIVE, listOf("application/gzip"), "High-ratio LZ77 + Huffman stream compression"),
        SupportedFormatInfo("JAR", "Java Archive Package", ToolCategory.TEXT_ARCHIVE, listOf("application/java-archive"), "Asset & resource entry max-ratio repacking"),
        SupportedFormatInfo("JSON", "JSON Structured Data", ToolCategory.TEXT_ARCHIVE, listOf("application/json"), "Whitespace/indent stripping & optional GZIP"),
        SupportedFormatInfo("XML", "Extensible Markup", ToolCategory.TEXT_ARCHIVE, listOf("application/xml", "text/xml"), "Comment removal & tag whitespace minification"),
        SupportedFormatInfo("HTML", "Web Document Markup", ToolCategory.TEXT_ARCHIVE, listOf("text/html"), "DOM whitespace collapse & inline cleanup"),
        SupportedFormatInfo("CSV", "Comma-Separated Values", ToolCategory.TEXT_ARCHIVE, listOf("text/csv", "text/comma-separated-values"), "Trailing delimiter trim & GZIP/ZIP archival"),
        SupportedFormatInfo("TXT", "Plain Text Document", ToolCategory.TEXT_ARCHIVE, listOf("text/plain"), "Line-ending normalization & archive packing"),
        SupportedFormatInfo("RTF", "Rich Text Format", ToolCategory.TEXT_ARCHIVE, listOf("application/rtf", "text/rtf"), "Redundant control-word cleanup & compression"),
        SupportedFormatInfo("MD", "Markdown Document", ToolCategory.TEXT_ARCHIVE, listOf("text/markdown"), "Whitespace optimization & compact bundling")
    )

    fun byCategory(category: ToolCategory): List<SupportedFormatInfo> =
        allFormats.filter { it.category == category }

    fun detectCategory(extension: String): ToolCategory {
        return when (extension.uppercase(Locale.US).removePrefix(".")) {
            "JPG", "JPEG", "PNG", "WEBP", "BMP", "HEIC", "HEIF", "GIF", "SVG", "WBMP", "ICO" -> ToolCategory.IMAGE
            "PDF" -> ToolCategory.PDF
            "DOCX", "PPTX", "XLSX", "ODT", "ODS", "ODP", "EPUB" -> ToolCategory.OFFICE_EBOOK
            else -> ToolCategory.TEXT_ARCHIVE
        }
    }

    /**
     * Returns valid target conversion formats for a given source extension.
     */
    fun getAvailableConversionTargets(sourceExtension: String): List<String> {
        val ext = sourceExtension.uppercase(Locale.US).removePrefix(".")
        return when (ext) {
            "PNG", "BMP", "HEIC", "HEIF", "GIF" -> listOf("JPEG", "WEBP", "PNG", "BMP", "PDF")
            "JPG", "JPEG" -> listOf("PNG", "WEBP", "BMP", "PDF")
            "WEBP" -> listOf("JPEG", "PNG", "BMP", "PDF")
            "SVG" -> listOf("PNG", "JPEG", "WEBP", "PDF")
            "PDF" -> listOf("JPG", "PNG", "WEBP", "ZIP")
            "DOCX", "TXT", "RTF", "MD", "HTML" -> listOf("PDF", "TXT", "HTML", "ZIP", "GZIP")
            "JSON", "XML", "CSV" -> listOf("TXT", "CSV", "PDF", "ZIP", "GZIP")
            "ZIP", "JAR" -> listOf("ZIP", "GZIP")
            else -> listOf("PDF", "ZIP", "GZIP", "TXT")
        }
    }

    /**
     * Auto-detects the file attributes and provides smart target format and preset suggestions.
     */
    fun getSmartRecommendation(sourceExtension: String, sizeBytes: Long): SmartFormatRecommendation {
        val ext = sourceExtension.uppercase(Locale.US).removePrefix(".")
        return when (ext) {
            "PNG" -> {
                if (sizeBytes > 1_500_000L) {
                    SmartFormatRecommendation("WEBP", QualityPreset.EXTREME, "High-res PNG detected: WEBP cuts 75%+ size with transparent quality")
                } else {
                    SmartFormatRecommendation("JPEG", QualityPreset.BALANCED, "PNG photo detected: JPEG conversion offers balanced size reduction")
                }
            }
            "BMP" -> SmartFormatRecommendation("WEBP", QualityPreset.EXTREME, "Uncompressed BMP detected: Converting to WEBP saves up to 90%")
            "HEIC", "HEIF" -> SmartFormatRecommendation("JPEG", QualityPreset.BALANCED, "Universal compatibility: Converting HEIC to standard JPEG")
            "JPG", "JPEG" -> {
                if (sizeBytes > 2_000_000L) {
                    SmartFormatRecommendation("WEBP", QualityPreset.BALANCED, "Modern WebP re-encoding recommended for 60% size savings")
                } else {
                    SmartFormatRecommendation("WEBP", QualityPreset.HIGH_QUALITY, "WebP conversion suggested for web & mobile optimization")
                }
            }
            "SVG" -> SmartFormatRecommendation("PNG", QualityPreset.HIGH_QUALITY, "Raster conversion: Render SVG vector to high-res PNG")
            "DOCX", "TXT", "RTF", "MD" -> SmartFormatRecommendation("PDF", QualityPreset.BALANCED, "Document detected: Clean print-ready PDF conversion suggested")
            "JSON", "XML", "CSV" -> {
                if (sizeBytes > 500_000L) {
                    SmartFormatRecommendation("GZIP", QualityPreset.EXTREME, "Large structured data detected: GZIP compression cuts 80%+ bytes")
                } else {
                    SmartFormatRecommendation("PDF", QualityPreset.BALANCED, "Export structured records as readable PDF summary")
                }
            }
            "PDF" -> SmartFormatRecommendation("PDF", QualityPreset.BALANCED, "Multi-page stream optimization: Downscale DPI & compress stream")
            else -> SmartFormatRecommendation("ZIP", QualityPreset.BALANCED, "Archive packing: Compress file into high-ratio ZIP container")
        }
    }
}

data class SelectedFileInfo(
    val filePath: String,
    val displayName: String,
    val extension: String,
    val category: ToolCategory,
    val sizeBytes: Long,
    val metadataSummary: String,
    val pageCount: Int = 1,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val previewBitmap: Bitmap? = null,
    val smartRecommendation: SmartFormatRecommendation? = null
)

data class DualPdfMergeState(
    val isMergeModeEnabled: Boolean = false,
    val primaryPdf: SelectedFileInfo? = null,
    val secondaryPdf: SelectedFileInfo? = null,
    val extraPdfs: List<SelectedFileInfo> = emptyList(),
    val mergedFileName: String = "Merged_Compressed_Document.pdf"
) {
    val allSelectedPdfs: List<SelectedFileInfo>
        get() = listOfNotNull(primaryPdf, secondaryPdf) + extraPdfs

    val totalOriginalBytes: Long
        get() = allSelectedPdfs.sumOf { it.sizeBytes }

    val totalPageCount: Int
        get() = allSelectedPdfs.sumOf { it.pageCount.coerceAtLeast(1) }

    val isReadyToMerge: Boolean
        get() = primaryPdf != null && secondaryPdf != null
}

data class CompressionResult(
    val record: CompressionRecord,
    val beforePreviewBitmap: Bitmap? = null,
    val afterPreviewBitmap: Bitmap? = null,
    val secondPdfPreviewBitmap: Bitmap? = null,
    val structureBreakdown: String = ""
)

@Entity(tableName = "compression_history")
data class CompressionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalFileName: String,
    val compressedFileName: String,
    val categoryName: String,
    val originalFormat: String,
    val outputFormat: String,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val compressionPercent: Int,
    val localCachePath: String,
    val downloadedUriString: String? = null,
    val isMergedPdf: Boolean = false,
    val isConverted: Boolean = false,
    val mergedSourceCount: Int = 1,
    val totalPagesOrResolution: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val savedBytes: Long
        get() = (originalSizeBytes - compressedSizeBytes).coerceAtLeast(0L)

    val savedPercentage: Int
        get() = if (originalSizeBytes > 0) {
            (((originalSizeBytes - compressedSizeBytes).toDouble() / originalSizeBytes.toDouble()) * 100.0)
                .toInt()
                .coerceIn(0, 99)
        } else 0
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1.0) return "$bytes B"
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.2f MB", mb)
    } else {
        String.format(Locale.US, "%.1f KB", kb)
    }
}
