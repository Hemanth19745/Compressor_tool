package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SampleFileGenerator(private val context: Context) {

    private val samplesDir: File
        get() = File(context.cacheDir, "sample_inputs").apply { mkdirs() }

    suspend fun generateSampleImage(extension: String = "PNG"): File = withContext(Dispatchers.IO) {
        val ext = extension.lowercase()
        val outFile = File(samplesDir, "Studio_Landscape_Artwork.$ext")
        val width = 1400
        val height = 1000
        val bitmap = createRichSampleBitmap(
            width = width,
            height = height,
            title = "SqueezeStudio High-Res Sample",
            subtitle = "1400x1000px • Format: ${extension.uppercase()}",
            primaryColor = Color.parseColor("#1E5E4A"),
            secondaryColor = Color.parseColor("#255C85"),
            accentColor = Color.parseColor("#F4B400")
        )
        if (ext == "svg") {
            val svgText = buildSampleSvg(width, height)
            outFile.writeText(svgText)
        } else {
            FileOutputStream(outFile).use { fos ->
                when (ext) {
                    "jpg", "jpeg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    "webp" -> bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos)
                    else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
            }
        }
        outFile
    }

    suspend fun generateSingleSamplePdf(): File = withContext(Dispatchers.IO) {
        val outFile = File(samplesDir, "Quarterly_Design_Portfolio.pdf")
        createSamplePdfFile(
            targetFile = outFile,
            docTitle = "Quarterly Design Portfolio",
            pageCount = 4,
            themeStartColor = Color.parseColor("#1B4F72"),
            themeEndColor = Color.parseColor("#1E5E4A")
        )
        outFile
    }

    suspend fun generateDualSamplePdfsForMerge(): Pair<File, File> = withContext(Dispatchers.IO) {
        val pdf1 = File(samplesDir, "Part1_Product_Proposal.pdf")
        val pdf2 = File(samplesDir, "Part2_Financial_Roadmap.pdf")

        createSamplePdfFile(
            targetFile = pdf1,
            docTitle = "PDF #1: Product Architecture Proposal",
            pageCount = 3,
            themeStartColor = Color.parseColor("#1E5E4A"),
            themeEndColor = Color.parseColor("#2A7B62")
        )
        createSamplePdfFile(
            targetFile = pdf2,
            docTitle = "PDF #2: Financial Appendix & Charts",
            pageCount = 3,
            themeStartColor = Color.parseColor("#255C85"),
            themeEndColor = Color.parseColor("#53387E")
        )
        Pair(pdf1, pdf2)
    }

    suspend fun generateSampleOfficeOrEbook(extension: String = "DOCX"): File = withContext(Dispatchers.IO) {
        val ext = extension.lowercase()
        val fileName = when (ext) {
            "pptx" -> "Executive_Keynote_Deck.pptx"
            "xlsx" -> "Global_Metrics_Workbook.xlsx"
            "odt" -> "Research_Manuscript.odt"
            "ods" -> "Inventory_Ledger.ods"
            "odp" -> "Product_Showcase.odp"
            "epub" -> "Digital_Design_Handbook.epub"
            else -> "Enterprise_Annual_Report.docx"
        }
        val outFile = File(samplesDir, fileName)
        val mediaFolder = when (ext) {
            "pptx" -> "ppt/media"
            "xlsx" -> "xl/media"
            "odt", "ods", "odp" -> "Pictures"
            "epub" -> "OEBPS/images"
            else -> "word/media"
        }

        // Create an uncompressed/low-compression OpenXML / OpenDoc / EPUB ZIP container
        // with high-res PNG bitmaps inside mediaFolder + verbose XML so compression shrinks it significantly.
        FileOutputStream(outFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                zos.setLevel(Deflater.BEST_SPEED)

                // Content_Types or mimetype entry
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                val verboseXml = buildString {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                    append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n")
                    repeat(120) { idx ->
                        append("  <!-- Document section metadata block #$idx with generous formatting whitespace -->\n")
                        append("  <Default Extension=\"png\" ContentType=\"image/png\" />\n")
                        append("  <Default Extension=\"xml\" ContentType=\"application/xml\" />\n")
                    }
                    append("</Types>")
                }
                zos.write(verboseXml.toByteArray())
                zos.closeEntry()

                // Document body XML
                zos.putNextEntry(ZipEntry("document_content.xml"))
                val bodyXml = buildString {
                    append("<document>\n")
                    repeat(200) { p ->
                        append("    <paragraph id=\"$p\">   SqueezeStudio sample $extension document paragraph #$p containing embedded high-resolution figures and structured tables for realistic container compression testing.   </paragraph>\n")
                    }
                    append("</document>")
                }
                zos.write(bodyXml.toByteArray())
                zos.closeEntry()

                // Add 2 embedded high-resolution PNG media assets inside mediaFolder
                for (imgIdx in 1..2) {
                    val bmp = createRichSampleBitmap(
                        width = 1100,
                        height = 800,
                        title = "$extension Embedded Media #$imgIdx",
                        subtitle = "Stored in $mediaFolder/figure_$imgIdx.png",
                        primaryColor = if (imgIdx == 1) Color.parseColor("#53387E") else Color.parseColor("#1B4F72"),
                        secondaryColor = Color.parseColor("#1E5E4A"),
                        accentColor = Color.parseColor("#FF8A65")
                    )
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    zos.putNextEntry(ZipEntry("$mediaFolder/figure_$imgIdx.png"))
                    zos.write(baos.toByteArray())
                    zos.closeEntry()
                }
            }
        }
        outFile
    }

    suspend fun generateSampleArchiveOrData(extension: String = "ZIP"): File = withContext(Dispatchers.IO) {
        val ext = extension.lowercase()
        val outFile = File(samplesDir, "Analytics_Bundle_Sample.$ext")
        when (ext) {
            "json" -> {
                val json = buildString {
                    append("{\n  \"dataset\": \"Global Compression Benchmark\",\n  \"records\": [\n")
                    repeat(350) { i ->
                        val comma = if (i < 349) "," else ""
                        append("    {\n      \"id\": $i,\n      \"region\": \"Global-Node-${i % 12}\",\n      \"description\": \"Verbose padded JSON telemetry payload for minification and compression verification\",\n      \"metrics\": [1024, 2048, 4096, 8192]\n    }$comma\n")
                    }
                    append("  ]\n}")
                }
                outFile.writeText(json)
            }
            "xml", "html", "csv", "txt", "md", "rtf" -> {
                val content = buildString {
                    repeat(450) { i ->
                        append("Line #$i   |   SqueezeStudio Universal Document & Data Payload   |   Format: ${ext.uppercase()}   |   Redundant spacing & structured text block for real byte optimization.\n")
                    }
                }
                outFile.writeText(content)
            }
            else -> {
                // ZIP / JAR / GZIP
                FileOutputStream(outFile).use { fos ->
                    ZipOutputStream(fos).use { zos ->
                        zos.setLevel(Deflater.NO_COMPRESSION)
                        val bmp = createRichSampleBitmap(
                            width = 1000,
                            height = 750,
                            title = "Archive Visual Asset",
                            subtitle = "Uncompressed asset inside ${ext.uppercase()}",
                            primaryColor = Color.parseColor("#7D3C1D"),
                            secondaryColor = Color.parseColor("#1E5E4A"),
                            accentColor = Color.parseColor("#FFD54F")
                        )
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        zos.putNextEntry(ZipEntry("assets/banner_preview.png"))
                        zos.write(baos.toByteArray())
                        zos.closeEntry()

                        zos.putNextEntry(ZipEntry("data/manifest_log.json"))
                        val logText = (1..250).joinToString("\n") {
                            "{ \"entry\": $it, \"status\": \"UNCOMPRESSED_SOURCE_BLOCK\", \"notes\": \"                padded archive text                \" }"
                        }
                        zos.write(logText.toByteArray())
                        zos.closeEntry()
                    }
                }
            }
        }
        outFile
    }

    private fun createSamplePdfFile(
        targetFile: File,
        docTitle: String,
        pageCount: Int,
        themeStartColor: Int,
        themeEndColor: Int
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 1080
        val pageHeight = 1520

        for (pageIndex in 1..pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = pdfDocument.startPage(pageInfo)
            val bmp = createRichSampleBitmap(
                width = pageWidth,
                height = pageHeight,
                title = docTitle,
                subtitle = "Page $pageIndex of $pageCount • High-DPI Embedded Graphics",
                primaryColor = if (pageIndex % 2 == 1) themeStartColor else themeEndColor,
                secondaryColor = if (pageIndex % 2 == 1) themeEndColor else themeStartColor,
                accentColor = Color.parseColor("#FFB74D")
            )
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }

        FileOutputStream(targetFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()
    }

    private fun buildSampleSvg(width: Int, height: Int): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        repeat(40) {
            append("<!-- Verbose SVG editor metadata & layer comments that can be safely stripped during compression #$it -->\n")
        }
        append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">\n")
        append("    <rect width=\"100%\" height=\"100%\" fill=\"#D8F3E5\" />\n")
        repeat(60) { i ->
            val cx = (i * 37) % width
            val cy = (i * 53) % height
            val r = 20 + (i % 50)
            append("    <circle   cx=\"$cx\"   cy=\"$cy\"   r=\"$r\"   fill=\"#1E5E4A\"   fill-opacity=\"0.35\"   />\n")
        }
        append("</svg>")
    }

    fun createRichSampleBitmap(
        width: Int,
        height: Int,
        title: String,
        subtitle: String,
        primaryColor: Int,
        secondaryColor: Int,
        accentColor: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Rich gradient background
        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            primaryColor, secondaryColor, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Decorative geometric circles & grid details (adds realistic visual entropy)
        for (i in 0..18) {
            paint.color = Color.argb(38, 255, 255, 255)
            val radius = (width * 0.06f) + (i * 19f)
            val cx = ((i * 173) % width).toFloat()
            val cy = ((i * 229) % height).toFloat()
            canvas.drawCircle(cx, cy, radius, paint)
        }

        // Card container in center
        val cardRect = RectF(
            width * 0.08f,
            height * 0.14f,
            width * 0.92f,
            height * 0.86f
        )
        paint.color = Color.argb(235, 250, 249, 246)
        canvas.drawRoundRect(cardRect, 42f, 42f, paint)

        // Accent header band
        paint.color = accentColor
        canvas.drawRoundRect(
            RectF(width * 0.14f, height * 0.22f, width * 0.42f, height * 0.26f),
            16f, 16f, paint
        )

        // Title & Subtitle
        paint.color = Color.parseColor("#191C1B")
        paint.textSize = (width * 0.042f).coerceIn(28f, 56f)
        paint.isFakeBoldText = true
        canvas.drawText(title.take(34), width * 0.14f, height * 0.36f, paint)

        paint.color = Color.parseColor("#424945")
        paint.textSize = (width * 0.028f).coerceIn(20f, 38f)
        paint.isFakeBoldText = false
        canvas.drawText(subtitle.take(48), width * 0.14f, height * 0.44f, paint)

        // Simulated chart bars
        val barColors = intArrayOf(primaryColor, secondaryColor, accentColor, Color.parseColor("#2B6E57"))
        for (b in 0..5) {
            paint.color = barColors[b % barColors.size]
            val left = width * (0.14f + b * 0.12f)
            val barTop = height * (0.76f - (b % 3 + 1) * 0.07f)
            canvas.drawRoundRect(
                RectF(left, barTop, left + width * 0.08f, height * 0.78f),
                14f, 14f, paint
            )
        }

        return bitmap
    }
}
