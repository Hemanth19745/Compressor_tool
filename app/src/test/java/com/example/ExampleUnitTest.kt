package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun formatCatalog_recommendations_areAccurate() {
    val pngRec = com.example.data.FormatCatalog.getSmartRecommendation("PNG", 2_000_000L)
    assertEquals("WEBP", pngRec.recommendedFormat)

    val docxRec = com.example.data.FormatCatalog.getSmartRecommendation("DOCX", 100_000L)
    assertEquals("PDF", docxRec.recommendedFormat)

    val targets = com.example.data.FormatCatalog.getAvailableConversionTargets("PNG")
    assertTrue(targets.contains("JPEG"))
    assertTrue(targets.contains("WEBP"))
    assertTrue(targets.contains("PDF"))
  }
}
