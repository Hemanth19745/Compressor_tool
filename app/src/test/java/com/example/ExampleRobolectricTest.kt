package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FormatCatalog
import com.example.data.SampleFileGenerator
import com.example.data.ToolCategory
import com.example.data.UniversalCompressionEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SqueezeStudio", appName)
    }

    @Test
    fun `verify global format catalog and dual pdf merge compression`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(FormatCatalog.allFormats.size >= 25)
        assertEquals(ToolCategory.IMAGE, FormatCatalog.detectCategory("WEBP"))
        assertEquals(ToolCategory.PDF, FormatCatalog.detectCategory("PDF"))
        assertEquals(ToolCategory.OFFICE_EBOOK, FormatCatalog.detectCategory("DOCX"))

        val generator = SampleFileGenerator(context)
        val engine = UniversalCompressionEngine(context)

        val (pdf1, pdf2) = generator.generateDualSamplePdfsForMerge()
        val info1 = engine.inspectFile(pdf1, isSample = true)
        val info2 = engine.inspectFile(pdf2, isSample = true)

        val mergedResult = engine.mergeAndCompressPdfs(
            pdfs = listOf(info1, info2),
            compressionPercent = 60,
            resolutionScalePercent = 75,
            customOutputName = "Test_Merged.pdf"
        )
        assertTrue(File(mergedResult.record.localCachePath).exists())
        assertTrue(mergedResult.record.isMergedPdf)
        assertEquals(2, mergedResult.record.mergedSourceCount)
    }

    @Test
    fun `verify smart recommendation auto-detection and conversion`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pngRec = FormatCatalog.getSmartRecommendation("PNG", 2_000_000L)
        assertEquals("WEBP", pngRec.recommendedFormat)

        val docxRec = FormatCatalog.getSmartRecommendation("DOCX", 100_000L)
        assertEquals("PDF", docxRec.recommendedFormat)

        val generator = SampleFileGenerator(context)
        val engine = UniversalCompressionEngine(context)

        val sampleImage = generator.generateSampleImage("PNG")
        val imageInfo = engine.inspectFile(sampleImage, isSample = true)
        assertEquals("WEBP", imageInfo.smartRecommendation?.recommendedFormat)

        // Convert PNG to JPEG
        val conversionResult = engine.compressSingleFile(
            source = imageInfo,
            compressionPercent = 50,
            resolutionScalePercent = 100,
            targetOutputFormatOverride = "JPEG"
        )
        assertEquals("JPEG", conversionResult.record.outputFormat)
        assertTrue(File(conversionResult.record.localCachePath).exists())
    }
}
