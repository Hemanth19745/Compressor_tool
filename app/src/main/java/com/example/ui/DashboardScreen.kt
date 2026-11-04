package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CompressionRecord
import com.example.data.FormatCatalog
import com.example.data.SupportedFormatInfo
import com.example.data.ToolCategory
import com.example.data.formatFileSize
import com.example.ui.theme.EmeraldSavingsBg
import com.example.ui.theme.EmeraldSavingsText
import com.example.ui.theme.PastelButterAccent
import com.example.ui.theme.PastelButterBorder
import com.example.ui.theme.PastelButterContainer
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderBorder
import com.example.ui.theme.PastelLavenderContainer
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintContainer
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachBorder
import com.example.ui.theme.PastelPeachContainer
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    historyRecords: List<CompressionRecord>,
    onSelectCategory: (ToolCategory, Boolean) -> Unit,
    onPickAnyFile: () -> Unit,
    onLoadSample: (ToolCategory, String) -> Unit,
    onLoadTwoSamplePdfs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSavedBytes = remember(historyRecords) {
        historyRecords.sumOf { it.savedBytes }
    }
    val averageSavedPercent = remember(historyRecords) {
        if (historyRecords.isEmpty()) 0
        else historyRecords.map { it.savedPercentage }.average().toInt()
    }
    var selectedFormatDetail by remember { mutableStateOf<SupportedFormatInfo?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hero Savings & Visual Banner
        item {
            HeroSavingsCard(
                totalSavedBytes = totalSavedBytes,
                totalFilesOptimized = historyRecords.size,
                averageReductionPercent = averageSavedPercent,
                onQuickPickFile = onPickAnyFile,
                onQuickMergeTwoPdfs = { onSelectCategory(ToolCategory.PDF, true) }
            )
        }

        // 2. Interactive Pastel Color Dashboard Grid (Headline Only)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Pastel Tools",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = EmeraldSavingsBg,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "26+ Formats",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSavingsText,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Row 1: Images (Mint) & PDF + 2-PDF Merger (Sky Blue)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PastelToolDashboardCard(
                        title = "Universal Images",
                        badgeText = "8+ Formats",
                        icon = Icons.Default.Image,
                        containerColor = PastelMintContainer,
                        accentColor = PastelMintAccent,
                        borderColor = PastelMintBorder,
                        testTag = "tool_card_image",
                        onClick = { onSelectCategory(ToolCategory.IMAGE, false) },
                        modifier = Modifier.weight(1f)
                    )
                    PastelToolDashboardCard(
                        title = "PDF & 2-PDF Merge",
                        badgeText = "Single + Dual",
                        icon = Icons.Default.PictureAsPdf,
                        containerColor = PastelSkyContainer,
                        accentColor = PastelSkyAccent,
                        borderColor = PastelSkyBorder,
                        testTag = "tool_card_pdf",
                        onClick = { onSelectCategory(ToolCategory.PDF, false) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Office & E-Books (Lavender) & Text/Data/Archives (Peach)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PastelToolDashboardCard(
                        title = "Office & E-Books",
                        badgeText = "DOCX • PPTX",
                        icon = Icons.Default.Description,
                        containerColor = PastelLavenderContainer,
                        accentColor = PastelLavenderAccent,
                        borderColor = PastelLavenderBorder,
                        testTag = "tool_card_office",
                        onClick = { onSelectCategory(ToolCategory.OFFICE_EBOOK, false) },
                        modifier = Modifier.weight(1f)
                    )
                    PastelToolDashboardCard(
                        title = "Archives & Data",
                        badgeText = "ZIP • GZIP",
                        icon = Icons.Default.FolderZip,
                        containerColor = PastelPeachContainer,
                        accentColor = PastelPeachAccent,
                        borderColor = PastelPeachBorder,
                        testTag = "tool_card_archive",
                        onClick = { onSelectCategory(ToolCategory.TEXT_ARCHIVE, false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Dedicated Highlight Card: Combine 2 PDFs & Compress (Headline Only)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("featured_dual_pdf_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PastelSkyContainer),
                border = BorderStroke(1.5.dp, PastelSkyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = PastelSkyAccent,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallMerge,
                                    contentDescription = "Merge 2 PDFs",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Combine 2 PDFs & Compress",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF0A2E46),
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.85f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "NEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelSkyAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onSelectCategory(ToolCategory.PDF, true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PastelSkyAccent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_pdf_merger_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallMerge,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add 2 PDFs Now")
                        }
                        OutlinedButton(
                            onClick = onLoadTwoSamplePdfs,
                            border = BorderStroke(1.dp, PastelSkyAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelSkyAccent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("try_two_sample_pdfs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try 2 Sample PDFs")
                        }
                    }
                }
            }
        }

        // 4. Instant Sample Playground (Headline Only)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sample_playground_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PastelButterContainer),
                border = BorderStroke(1.dp, PastelButterBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = PastelButterAccent,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Instant Sample Playground",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Instant Multi-Format Sample Playground",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF432F06),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickSamples = listOf(
                            Triple("Sample PNG Photo", ToolCategory.IMAGE, "PNG"),
                            Triple("Sample JPG Photo", ToolCategory.IMAGE, "JPG"),
                            Triple("Sample WEBP", ToolCategory.IMAGE, "WEBP"),
                            Triple("Sample SVG Vector", ToolCategory.IMAGE, "SVG"),
                            Triple("Single 4-Page PDF", ToolCategory.PDF, "PDF"),
                            Triple("Word DOCX", ToolCategory.OFFICE_EBOOK, "DOCX"),
                            Triple("Slides PPTX", ToolCategory.OFFICE_EBOOK, "PPTX"),
                            Triple("Excel XLSX", ToolCategory.OFFICE_EBOOK, "XLSX"),
                            Triple("OpenDoc ODT", ToolCategory.OFFICE_EBOOK, "ODT"),
                            Triple("E-Book EPUB", ToolCategory.OFFICE_EBOOK, "EPUB"),
                            Triple("Archive ZIP", ToolCategory.TEXT_ARCHIVE, "ZIP"),
                            Triple("Data JSON", ToolCategory.TEXT_ARCHIVE, "JSON")
                        )
                        quickSamples.forEach { (label, cat, ext) ->
                            FilterChip(
                                selected = false,
                                onClick = { onLoadSample(cat, ext) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.88f),
                                    labelColor = Color(0xFF432F06)
                                ),
                                modifier = Modifier.testTag("sample_chip_${ext.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 5. Global Format Directory (Headline Only)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Global Supported Formats (${FormatCatalog.allFormats.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatCatalog.allFormats.forEach { fmt ->
                        val isSelected = selectedFormatDetail?.extension == fmt.extension
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFormatDetail = if (isSelected) null else fmt
                            },
                            label = {
                                Text(
                                    text = ".${fmt.extension}",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (fmt.category) {
                                        ToolCategory.IMAGE -> Icons.Default.Image
                                        ToolCategory.PDF -> Icons.Default.PictureAsPdf
                                        ToolCategory.OFFICE_EBOOK -> Icons.Default.Description
                                        ToolCategory.TEXT_ARCHIVE -> Icons.Default.Archive
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = selectedFormatDetail != null) {
                    selectedFormatDetail?.let { fmt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${fmt.name} (.${fmt.extension})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (fmt.extension == "2-PDF") {
                                            onSelectCategory(ToolCategory.PDF, true)
                                        } else {
                                            onSelectCategory(fmt.category, false)
                                        }
                                    }
                                ) {
                                    Text("Open Tool")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSavingsCard(
    totalSavedBytes: Long,
    totalFilesOptimized: Int,
    averageReductionPercent: Int,
    onQuickPickFile: () -> Unit,
    onQuickMergeTwoPdfs: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_savings_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_compression_hero),
                    contentDescription = "SqueezeStudio Multi-Format Compression Illustration",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color(0xCC12382B),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compress,
                            contentDescription = null,
                            tint = Color(0xFF98F5D0),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "On-Device Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatFileSize(totalSavedBytes),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricPill(
                            value = "$totalFilesOptimized Files",
                            containerColor = PastelMintContainer,
                            textColor = PastelMintAccent
                        )
                        MetricPill(
                            value = if (averageReductionPercent > 0) "-$averageReductionPercent%" else "Ready",
                            containerColor = PastelSkyContainer,
                            textColor = PastelSkyAccent
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onQuickPickFile,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_pick_file_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick File to Compress")
                    }
                    OutlinedButton(
                        onClick = onQuickMergeTwoPdfs,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_merge_pdfs_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMerge,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Merge 2 PDFs")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    value: String,
    containerColor: Color,
    textColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun PastelToolDashboardCard(
    title: String,
    badgeText: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.78f),
                    shape = CircleShape
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF141817),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
