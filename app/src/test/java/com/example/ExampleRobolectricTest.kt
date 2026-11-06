package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FormatCatalog
import com.example.data.QualityPreset
import com.example.data.ToolCategory
import com.example.data.UniversalCompressionEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `verify global format catalog and category detection`() {
        assertTrue(FormatCatalog.allFormats.size >= 25)
        assertEquals(ToolCategory.IMAGE, FormatCatalog.detectCategory("WEBP"))
        assertEquals(ToolCategory.PDF, FormatCatalog.detectCategory("PDF"))
        assertEquals(ToolCategory.OFFICE_EBOOK, FormatCatalog.detectCategory("DOCX"))
        assertEquals(ToolCategory.TEXT_ARCHIVE, FormatCatalog.detectCategory("ZIP"))
    }

    @Test
    fun `verify smart recommendation auto-detection`() {
        val pngRec = FormatCatalog.getSmartRecommendation("PNG", 2_000_000L)
        assertEquals("WEBP", pngRec.recommendedFormat)
        assertEquals(QualityPreset.EXTREME, pngRec.recommendedPreset)

        val docxRec = FormatCatalog.getSmartRecommendation("DOCX", 100_000L)
        assertEquals("PDF", docxRec.recommendedFormat)
        assertEquals(QualityPreset.BALANCED, docxRec.recommendedPreset)

        val bmpRec = FormatCatalog.getSmartRecommendation("BMP", 500_000L)
        assertEquals("WEBP", bmpRec.recommendedFormat)

        val conversionTargets = FormatCatalog.getAvailableConversionTargets("PNG")
        assertTrue(conversionTargets.contains("JPEG"))
        assertTrue(conversionTargets.contains("WEBP"))
        assertTrue(conversionTargets.contains("PDF"))
    }

    @Test
    fun `verify text file inspection and compression`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = UniversalCompressionEngine(context)

        val testFile = File(context.cacheDir, "sample_document.txt").apply {
            writeText("SqueezeStudio is a universal compression and conversion utility for Android.")
        }
        val fileInfo = engine.inspectFile(testFile)
        assertNotNull(fileInfo)
        assertEquals("TXT", fileInfo.extension)
        assertEquals(ToolCategory.TEXT_ARCHIVE, fileInfo.category)

        val result = engine.compressSingleFile(
            source = fileInfo,
            compressionPercent = 50,
            resolutionScalePercent = 100
        )
        assertNotNull(result)
        assertTrue(File(result.record.localCachePath).exists())
    }
}
