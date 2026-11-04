package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CompressionRecord
import com.example.data.CompressionResult
import com.example.data.FormatCatalog
import com.example.data.QualityPreset
import com.example.data.SelectedFileInfo
import com.example.data.StudioOperationMode
import com.example.data.ToolCategory
import com.example.data.formatFileSize
import com.example.ui.theme.AmberBalancedAccent
import com.example.ui.theme.CoralSqueezeAccent
import com.example.ui.theme.EmeraldSavingsBg
import com.example.ui.theme.EmeraldSavingsText
import com.example.ui.theme.PastelLavenderAccent
import com.example.ui.theme.PastelLavenderContainer
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintBorder
import com.example.ui.theme.PastelMintContainer
import com.example.ui.theme.PastelPeachAccent
import com.example.ui.theme.PastelPeachContainer
import com.example.ui.theme.PastelSkyAccent
import com.example.ui.theme.PastelSkyBorder
import com.example.ui.theme.PastelSkyContainer
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioAndPdfMergeScreen(
    uiState: StudioUiState,
    isDualPdfMode: Boolean,
    onSwitchCategory: (ToolCategory, Boolean) -> Unit,
    onOperationModeChange: (StudioOperationMode) -> Unit,
    onPickSingleFile: (ToolCategory) -> Unit,
    onPickMergePdfSlot: (Int) -> Unit,
    onSwapMergePdfs: () -> Unit,
    onClearMergePdfSlot: (Int) -> Unit,
    onUpdateMergedFileName: (String) -> Unit,
    onLoadSampleFile: (ToolCategory, String) -> Unit,
    onLoadTwoSamplePdfs: () -> Unit,
    onSelectPreset: (QualityPreset) -> Unit,
    onCompressionPercentChange: (Int) -> Unit,
    onResolutionScaleChange: (Int) -> Unit,
    onTargetFormatChange: (String?) -> Unit,
    onExecuteSingleCompress: () -> Unit,
    onExecuteDualPdfMergeAndCompress: () -> Unit,
    onDownloadResult: (CompressionRecord) -> Unit,
    onShareResult: (CompressionRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isDualPdfMode) "pdf_merger_screen" else "compression_studio_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Mode Selector: Compress vs Convert (Only for Single File Studio)
        if (!isDualPdfMode) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { onOperationModeChange(StudioOperationMode.COMPRESS) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.operationMode == StudioOperationMode.COMPRESS)
                                    MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (uiState.operationMode == StudioOperationMode.COMPRESS)
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_compress_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compress", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOperationModeChange(StudioOperationMode.CONVERT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.operationMode == StudioOperationMode.CONVERT)
                                    MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (uiState.operationMode == StudioOperationMode.CONVERT)
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_convert_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Convert Format", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. Mode & Category Selector Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolCategory.entries.forEach { category ->
                    val selected = !isDualPdfMode && uiState.selectedCategory == category
                    FilterChip(
                        selected = selected,
                        onClick = { onSwitchCategory(category, false) },
                        label = { Text(category.shortTitle) },
                        modifier = Modifier.testTag("studio_category_chip_${category.name.lowercase()}")
                    )
                }
                FilterChip(
                    selected = isDualPdfMode,
                    onClick = { onSwitchCategory(ToolCategory.PDF, true) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMerge,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Merge 2 PDFs + Compress", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PastelSkyContainer,
                        selectedLabelColor = PastelSkyAccent
                    ),
                    modifier = Modifier.testTag("studio_dual_pdf_mode_chip")
                )
            }
        }

        // 2. Source File Section (Either Dual-PDF Merge Slots OR Single File Card)
        if (isDualPdfMode) {
            item {
                DualPdfMergeSlotsCard(
                    uiState = uiState,
                    onPickSlot = onPickMergePdfSlot,
                    onSwapPdfs = onSwapMergePdfs,
                    onClearSlot = onClearMergePdfSlot,
                    onUpdateMergedName = onUpdateMergedFileName,
                    onLoadTwoSamplePdfs = onLoadTwoSamplePdfs
                )
            }
        } else {
            item {
                SingleFileSourceCard(
                    category = uiState.selectedCategory,
                    activeFile = uiState.activeFile,
                    onPickFile = { onPickSingleFile(uiState.selectedCategory) },
                    onLoadSample = onLoadSampleFile
                )
            }
        }

        // 3. Interactive Compression Percentage Slider & Quality Presets Card
        item {
            CompressionControlsCard(
                uiState = uiState,
                isDualPdfMode = isDualPdfMode,
                onSelectPreset = onSelectPreset,
                onCompressionPercentChange = onCompressionPercentChange,
                onResolutionScaleChange = onResolutionScaleChange,
                onTargetFormatChange = onTargetFormatChange,
                onCompressAction = if (isDualPdfMode) onExecuteDualPdfMergeAndCompress else onExecuteSingleCompress
            )
        }

        // 4. Post-Compression Result: Before & After Preview + Download & Share
        item {
            AnimatedVisibility(visible = uiState.latestResult != null) {
                uiState.latestResult?.let { result ->
                    BeforeAndAfterResultCard(
                        result = result,
                        lastDownloadedPath = uiState.lastDownloadedPath,
                        onDownload = { onDownloadResult(result.record) },
                        onShare = { onShareResult(result.record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DualPdfMergeSlotsCard(
    uiState: StudioUiState,
    onPickSlot: (Int) -> Unit,
    onSwapPdfs: () -> Unit,
    onClearSlot: (Int) -> Unit,
    onUpdateMergedName: (String) -> Unit,
    onLoadTwoSamplePdfs: () -> Unit
) {
    val mergeState = uiState.dualPdfState

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dual_pdf_slots_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSkyContainer),
        border = BorderStroke(1.5.dp, PastelSkyBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = PastelSkyAccent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallMerge,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Merge Two PDFs & Compress",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0A2E46),
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onLoadTwoSamplePdfs,
                    modifier = Modifier.testTag("load_two_sample_pdfs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load 2 Samples")
                }
            }

            // PDF Slot #1
            PdfMergeSlotItem(
                slotNumber = 1,
                slotLabel = "PDF #1",
                fileInfo = mergeState.primaryPdf,
                onPick = { onPickSlot(1) },
                onClear = { onClearSlot(1) }
            )

            // Swap Order Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSwapPdfs,
                    enabled = mergeState.primaryPdf != null || mergeState.secondaryPdf != null,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, PastelSkyAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.85f),
                        contentColor = PastelSkyAccent
                    ),
                    modifier = Modifier.testTag("swap_pdfs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap PDF order",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Swap PDF #1 & PDF #2 Order")
                }
            }

            // PDF Slot #2
            PdfMergeSlotItem(
                slotNumber = 2,
                slotLabel = "PDF #2",
                fileInfo = mergeState.secondaryPdf,
                onPick = { onPickSlot(2) },
                onClear = { onClearSlot(2) }
            )

            // Optional Extra PDFs
            mergeState.extraPdfs.forEachIndexed { idx, extraPdf ->
                PdfMergeSlotItem(
                    slotNumber = idx + 3,
                    slotLabel = "PDF #${idx + 3}",
                    fileInfo = extraPdf,
                    onPick = { onPickSlot(idx + 3) },
                    onClear = { onClearSlot(idx + 3) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onPickSlot(3) },
                    modifier = Modifier.testTag("add_extra_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Another PDF")
                }

                if (mergeState.allSelectedPdfs.isNotEmpty()) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${mergeState.allSelectedPdfs.size} PDFs • ${mergeState.totalPageCount} Pages • ${formatFileSize(mergeState.totalOriginalBytes)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PastelSkyAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = mergeState.mergedFileName,
                onValueChange = onUpdateMergedName,
                label = { Text("Output Merged PDF Filename") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .testTag("merged_pdf_filename_input")
            )
        }
    }
}

@Composable
private fun PdfMergeSlotItem(
    slotNumber: Int,
    slotLabel: String,
    fileInfo: SelectedFileInfo?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, PastelSkyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pdf_slot_$slotNumber")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (fileInfo?.previewBitmap != null) {
                Image(
                    bitmap = fileInfo.previewBitmap.asImageBitmap(),
                    contentDescription = fileInfo.displayName,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    color = PastelSkyContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = PastelSkyAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Text(
                text = fileInfo?.displayName ?: slotLabel,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF141817),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (fileInfo != null) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.testTag("clear_pdf_slot_$slotNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove PDF #$slotNumber",
                        tint = Color(0xFF5A625E)
                    )
                }
            }

            Button(
                onClick = onPick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PastelSkyAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pick_pdf_slot_${slotNumber}_button")
            ) {
                Text(if (fileInfo == null) "Select PDF #$slotNumber" else "Change")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SingleFileSourceCard(
    category: ToolCategory,
    activeFile: SelectedFileInfo?,
    onPickFile: () -> Unit,
    onLoadSample: (ToolCategory, String) -> Unit
) {
    val (bgColor, accentColor, borderColor) = when (category) {
        ToolCategory.IMAGE -> Triple(PastelMintContainer, PastelMintAccent, PastelMintBorder)
        ToolCategory.PDF -> Triple(PastelSkyContainer, PastelSkyAccent, PastelSkyBorder)
        ToolCategory.OFFICE_EBOOK -> Triple(PastelLavenderContainer, PastelLavenderAccent, Color(0xFFD1B6F7))
        ToolCategory.TEXT_ARCHIVE -> Triple(PastelPeachContainer, PastelPeachAccent, Color(0xFFF9C3A4))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("single_file_source_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF141817),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onPickFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("studio_pick_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (activeFile == null) "Pick File" else "Change File")
                }
            }

            if (activeFile != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            activeFile.previewBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = activeFile.displayName,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = activeFile.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF141817),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = formatFileSize(activeFile.sizeBytes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    activeFile.smartRecommendation?.let { rec ->
                        Surface(
                            color = EmeraldSavingsBg,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("smart_suggestion_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EmeraldSavingsText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Smart Suggestion: Convert to ${rec.recommendedFormat} (${rec.recommendedPreset.title})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldSavingsText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val formats = FormatCatalog.byCategory(category)
                        .filter { it.extension != "2-PDF" }
                        .take(6)
                    formats.forEach { fmt ->
                        OutlinedButton(
                            onClick = { onLoadSample(category, fmt.extension) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.85f),
                                contentColor = accentColor
                            ),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Text("Try .${fmt.extension}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompressionControlsCard(
    uiState: StudioUiState,
    isDualPdfMode: Boolean,
    onSelectPreset: (QualityPreset) -> Unit,
    onCompressionPercentChange: (Int) -> Unit,
    onResolutionScaleChange: (Int) -> Unit,
    onTargetFormatChange: (String?) -> Unit,
    onCompressAction: () -> Unit
) {
    val hasValidInput = if (isDualPdfMode) {
        uiState.dualPdfState.isReadyToMerge
    } else {
        uiState.activeFile != null
    }
    val originalBytes = if (isDualPdfMode) {
        uiState.dualPdfState.totalOriginalBytes
    } else {
        uiState.activeFile?.sizeBytes ?: 0L
    }

    val sliderAccentColor = when {
        uiState.compressionPercent >= 70 -> CoralSqueezeAccent
        uiState.compressionPercent >= 45 -> AmberBalancedAccent
        else -> PastelMintAccent
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compression_controls_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Desired Size & Compression Dial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = sliderAccentColor.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${uiState.compressionPercent}% Squeeze",
                        style = MaterialTheme.typography.labelLarge,
                        color = sliderAccentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 1. Quality Preset Chips (Headline Only)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityPreset.entries.forEach { preset ->
                    val selected = uiState.selectedPreset == preset
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("preset_chip_${preset.name.lowercase()}")
                    )
                }
            }

            // 2. Compression Percentage Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Compression Intensity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.compressionPercent}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = uiState.compressionPercent.toFloat(),
                    onValueChange = { onCompressionPercentChange(it.roundToInt()) },
                    valueRange = 10f..95f,
                    colors = SliderDefaults.colors(
                        thumbColor = sliderAccentColor,
                        activeTrackColor = sliderAccentColor
                    ),
                    modifier = Modifier.testTag("compression_percentage_slider")
                )
            }

            // 3. Resolution / DPI Scale Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dimension / Page DPI Scaling",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.resolutionScalePercent}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = uiState.resolutionScalePercent.toFloat(),
                    onValueChange = { onResolutionScaleChange(it.roundToInt()) },
                    valueRange = 25f..100f,
                    modifier = Modifier.testTag("resolution_scale_slider")
                )
            }

            // 4. Output Format Conversion (Active in Convert mode or optional in Compress mode)
            if (!isDualPdfMode) {
                val availableTargets = uiState.activeFile?.let {
                    FormatCatalog.getAvailableConversionTargets(it.extension)
                } ?: listOf("PNG", "JPEG", "WEBP", "PDF", "ZIP")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.operationMode == StudioOperationMode.CONVERT)
                                "Target Output Format" else "Output Format",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.activeFile?.smartRecommendation?.let { rec ->
                            Surface(
                                color = EmeraldSavingsBg,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Auto: ${rec.recommendedFormat}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSavingsText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = uiState.targetFormatOverride == null,
                            onClick = { onTargetFormatChange(null) },
                            label = { Text("Original / Auto", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("target_format_auto")
                        )
                        availableTargets.forEach { fmt ->
                            val isRecommended = uiState.activeFile?.smartRecommendation?.recommendedFormat.equals(fmt, ignoreCase = true)
                            FilterChip(
                                selected = uiState.targetFormatOverride.equals(fmt, ignoreCase = true),
                                onClick = { onTargetFormatChange(fmt) },
                                label = {
                                    Text(
                                        text = if (isRecommended) "$fmt ★" else fmt,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.testTag("target_format_${fmt.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 5. Live Target File Size Estimator Bar
            if (originalBytes > 0L) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatFileSize(originalBytes)} → ~${formatFileSize(uiState.estimatedOutputBytes)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = EmeraldSavingsBg,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "Est. -${uiState.estimatedSavedPercent}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldSavingsText,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 6. Primary Action Button
            Button(
                onClick = onCompressAction,
                enabled = hasValidInput && !uiState.isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("execute_compression_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Compressing...")
                } else {
                    Icon(
                        imageVector = if (isDualPdfMode) Icons.AutoMirrored.Filled.CallMerge else Icons.Default.Compress,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isDualPdfMode -> "Merge ${uiState.dualPdfState.allSelectedPdfs.size.coerceAtLeast(2)} PDFs & Compress Now"
                            uiState.operationMode == StudioOperationMode.CONVERT -> "Convert & Optimize File Now"
                            else -> "Compress File Now"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BeforeAndAfterResultCard(
    result: CompressionResult,
    lastDownloadedPath: String?,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    val record = result.record
    var showAfterPreview by remember { mutableStateOf(true) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compression_result_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Savings Pill (Headline Only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSavingsText,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (record.isMergedPdf) "PDFs Merged & Compressed" else "Compression Complete",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = EmeraldSavingsBg,
                    shape = CircleShape
                ) {
                    Text(
                        text = "-${record.savedPercentage}% Saved",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldSavingsText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Before & After Visual Preview Box
            if (result.beforePreviewBitmap != null || result.afterPreviewBitmap != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Preview",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = !showAfterPreview,
                                    onClick = { showAfterPreview = false },
                                    label = { Text("Before") }
                                )
                                FilterChip(
                                    selected = showAfterPreview,
                                    onClick = { showAfterPreview = true },
                                    label = { Text("After") }
                                )
                            }
                        }

                        if (record.isMergedPdf && result.beforePreviewBitmap != null && result.secondPdfPreviewBitmap != null && !showAfterPreview) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PreviewThumbnailBox(
                                    bitmap = result.beforePreviewBitmap,
                                    badge = "PDF #1",
                                    modifier = Modifier.weight(1f)
                                )
                                PreviewThumbnailBox(
                                    bitmap = result.secondPdfPreviewBitmap,
                                    badge = "PDF #2",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            val activeBmp = if (showAfterPreview) {
                                result.afterPreviewBitmap ?: result.beforePreviewBitmap
                            } else {
                                result.beforePreviewBitmap ?: result.afterPreviewBitmap
                            }
                            activeBmp?.let { bmp ->
                                PreviewThumbnailBox(
                                    bitmap = bmp,
                                    badge = if (showAfterPreview) "After" else "Before",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Size Comparison Metrics Bar (Headline Only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SizeComparisonTile(
                    sizeText = formatFileSize(record.originalSizeBytes),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SizeComparisonTile(
                    sizeText = formatFileSize(record.compressedSizeBytes),
                    containerColor = PastelMintContainer,
                    textColor = PastelMintAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Visual Reduction Progress Bar
            val ratio = if (record.originalSizeBytes > 0) {
                (record.compressedSizeBytes.toFloat() / record.originalSizeBytes.toFloat()).coerceIn(0.05f, 1f)
            } else 0.5f
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = PastelMintAccent,
                trackColor = PastelMintContainer
            )

            if (lastDownloadedPath != null) {
                Surface(
                    color = EmeraldSavingsBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSavingsText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = lastDownloadedPath,
                            style = MaterialTheme.typography.titleSmall,
                            color = EmeraldSavingsText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Download & Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("download_compressed_file_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelMintAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download File", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(50.dp)
                        .testTag("share_compressed_file_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun PreviewThumbnailBox(
    bitmap: Bitmap,
    badge: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = badge,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Surface(
            color = Color(0xCC141817),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SizeComparisonTile(
    sizeText: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = sizeText,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
