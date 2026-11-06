package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CompressionDatabase
import com.example.data.CompressionRecord
import com.example.data.CompressionRepository
import com.example.data.CompressionResult
import com.example.data.DualPdfMergeState
import com.example.data.QualityPreset
import com.example.data.SelectedFileInfo
import com.example.data.StudioOperationMode
import com.example.data.ToolCategory
import com.example.data.formatFileSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

enum class AppDestination {
    DASHBOARD,
    STUDIO,
    PDF_MERGER,
    HISTORY
}

data class StudioUiState(
    val currentDestination: AppDestination = AppDestination.DASHBOARD,
    val selectedCategory: ToolCategory = ToolCategory.IMAGE,
    val operationMode: StudioOperationMode = StudioOperationMode.COMPRESS,
    val activeFile: SelectedFileInfo? = null,
    val dualPdfState: DualPdfMergeState = DualPdfMergeState(),
    val selectedPreset: QualityPreset = QualityPreset.BALANCED,
    val compressionPercent: Int = QualityPreset.BALANCED.compressionPercent,
    val resolutionScalePercent: Int = QualityPreset.BALANCED.resolutionScalePercent,
    val targetFormatOverride: String? = null,
    val isProcessing: Boolean = false,
    val latestResult: CompressionResult? = null,
    val historyCategoryFilter: String = "ALL",
    val statusBannerMessage: String? = null,
    val lastDownloadedPath: String? = null,
    val isDarkMode: Boolean? = null
) {
    val estimatedOutputBytes: Long
        get() {
            val baseBytes = if (currentDestination == AppDestination.PDF_MERGER) {
                dualPdfState.totalOriginalBytes
            } else {
                activeFile?.sizeBytes ?: 0L
            }
            if (baseBytes <= 0L) return 0L
            val compressionFactor = (100 - (compressionPercent * 0.82)).coerceIn(12.0, 92.0) / 100.0
            val scaleFactor = (resolutionScalePercent.coerceIn(25, 100) / 100.0)
            val combinedRatio = (compressionFactor * (0.45 + 0.55 * scaleFactor)).coerceIn(0.10, 0.92)
            return (baseBytes * combinedRatio).roundToLong().coerceAtLeast(512L)
        }

    val estimatedSavedPercent: Int
        get() {
            val baseBytes = if (currentDestination == AppDestination.PDF_MERGER) {
                dualPdfState.totalOriginalBytes
            } else {
                activeFile?.sizeBytes ?: 0L
            }
            if (baseBytes <= 0L) return compressionPercent
            return (((baseBytes - estimatedOutputBytes).toDouble() / baseBytes.toDouble()) * 100.0)
                .toInt()
                .coerceIn(8, 90)
        }
}

class CompressionViewModel(
    private val repository: CompressionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<CompressionRecord>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun navigateTo(destination: AppDestination) {
        _uiState.update { state ->
            state.copy(
                currentDestination = destination,
                selectedCategory = if (destination == AppDestination.PDF_MERGER) ToolCategory.PDF else state.selectedCategory,
                dualPdfState = state.dualPdfState.copy(
                    isMergeModeEnabled = destination == AppDestination.PDF_MERGER
                )
            )
        }
    }

    fun selectCategoryAndOpenStudio(category: ToolCategory, openDualPdfMerge: Boolean = false) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                currentDestination = if (openDualPdfMerge) AppDestination.PDF_MERGER else AppDestination.STUDIO,
                targetFormatOverride = null,
                dualPdfState = state.dualPdfState.copy(isMergeModeEnabled = openDualPdfMerge)
            )
        }
    }

    fun applyPreset(preset: QualityPreset) {
        _uiState.update { state ->
            state.copy(
                selectedPreset = preset,
                compressionPercent = preset.compressionPercent,
                resolutionScalePercent = preset.resolutionScalePercent
            )
        }
    }

    fun updateCompressionPercent(percent: Int) {
        val clamped = percent.coerceIn(10, 95)
        _uiState.update { state ->
            state.copy(
                selectedPreset = QualityPreset.CUSTOM,
                compressionPercent = clamped
            )
        }
    }

    fun updateResolutionScale(scalePercent: Int) {
        val clamped = scalePercent.coerceIn(25, 100)
        _uiState.update { state ->
            state.copy(
                selectedPreset = QualityPreset.CUSTOM,
                resolutionScalePercent = clamped
            )
        }
    }

    fun updateTargetFormatOverride(format: String?) {
        _uiState.update { it.copy(targetFormatOverride = format) }
    }

    fun updateMergedPdfFileName(name: String) {
        _uiState.update { state ->
            state.copy(dualPdfState = state.dualPdfState.copy(mergedFileName = name))
        }
    }

    fun setOperationMode(mode: StudioOperationMode) {
        _uiState.update { state ->
            val updatedTargetFormat = if (mode == StudioOperationMode.CONVERT) {
                state.targetFormatOverride ?: state.activeFile?.smartRecommendation?.recommendedFormat
            } else {
                state.targetFormatOverride
            }
            state.copy(
                operationMode = mode,
                targetFormatOverride = updatedTargetFormat
            )
        }
    }

    fun onSingleFileUriPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, latestResult = null) }
            runCatching {
                repository.loadFromContentUri(uri)
            }.onSuccess { info ->
                _uiState.update { state ->
                    val rec = info.smartRecommendation
                    val suggestedFormat = rec?.recommendedFormat
                    val suggestedPreset = rec?.recommendedPreset ?: QualityPreset.BALANCED
                    state.copy(
                        isProcessing = false,
                        activeFile = info,
                        selectedCategory = info.category,
                        currentDestination = AppDestination.STUDIO,
                        targetFormatOverride = suggestedFormat,
                        selectedPreset = suggestedPreset,
                        compressionPercent = suggestedPreset.compressionPercent,
                        resolutionScalePercent = suggestedPreset.resolutionScalePercent,
                        latestResult = null,
                        statusBannerMessage = if (rec != null) "Auto-Detected .${info.extension}: ${rec.rationale}" else null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        statusBannerMessage = "Could not load file: ${err.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    /**
     * Handles picking PDF #1, PDF #2, or an extra PDF for the Dual-PDF Merge & Compress feature.
     */
    fun onMergePdfSlotPicked(slotIndex: Int, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, latestResult = null) }
            runCatching {
                repository.loadFromContentUri(uri)
            }.onSuccess { info ->
                _uiState.update { state ->
                    val currentMerge = state.dualPdfState
                    val updatedMerge = when (slotIndex) {
                        1 -> currentMerge.copy(isMergeModeEnabled = true, primaryPdf = info)
                        2 -> currentMerge.copy(isMergeModeEnabled = true, secondaryPdf = info)
                        else -> currentMerge.copy(
                            isMergeModeEnabled = true,
                            extraPdfs = currentMerge.extraPdfs + info
                        )
                    }
                    state.copy(
                        isProcessing = false,
                        selectedCategory = ToolCategory.PDF,
                        currentDestination = AppDestination.PDF_MERGER,
                        dualPdfState = updatedMerge,
                        latestResult = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        statusBannerMessage = "Failed to load PDF: ${err.localizedMessage ?: "Error"}"
                    )
                }
            }
        }
    }

    fun swapPrimaryAndSecondaryPdfs() {
        _uiState.update { state ->
            val current = state.dualPdfState
            state.copy(
                dualPdfState = current.copy(
                    primaryPdf = current.secondaryPdf,
                    secondaryPdf = current.primaryPdf
                ),
                latestResult = null
            )
        }
    }

    fun clearMergePdfSlot(slotIndex: Int) {
        _uiState.update { state ->
            val current = state.dualPdfState
            val updated = when (slotIndex) {
                1 -> current.copy(primaryPdf = null)
                2 -> current.copy(secondaryPdf = null)
                else -> current.copy(extraPdfs = emptyList())
            }
            state.copy(dualPdfState = updated, latestResult = null)
        }
    }

    fun executeSingleFileCompression() {
        val current = _uiState.value
        val source = current.activeFile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, lastDownloadedPath = null) }
            runCatching {
                repository.compressSingleFile(
                    source = source,
                    compressionPercent = current.compressionPercent,
                    resolutionScalePercent = current.resolutionScalePercent,
                    targetOutputFormatOverride = current.targetFormatOverride
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        latestResult = result,
                        statusBannerMessage = "Compressed ${result.record.originalFileName} (-${result.record.savedPercentage}% saved)!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        statusBannerMessage = "Compression failed: ${err.localizedMessage ?: "Error"}"
                    )
                }
            }
        }
    }

    fun executeDualPdfMergeAndCompress() {
        val current = _uiState.value
        val pdfList = current.dualPdfState.allSelectedPdfs
        if (pdfList.size < 2) {
            _uiState.update {
                it.copy(statusBannerMessage = "Please select both PDF #1 and PDF #2 to merge and compress.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, lastDownloadedPath = null) }
            runCatching {
                repository.mergeAndCompressPdfs(
                    pdfs = pdfList,
                    compressionPercent = current.compressionPercent,
                    resolutionScalePercent = current.resolutionScalePercent,
                    customOutputName = current.dualPdfState.mergedFileName
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        latestResult = result,
                        statusBannerMessage = "Merged ${pdfList.size} PDFs & compressed to ${formatFileSize(result.record.compressedSizeBytes)} (-${result.record.savedPercentage}%)!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        statusBannerMessage = "PDF Merge failed: ${err.localizedMessage ?: "Error"}"
                    )
                }
            }
        }
    }

    fun downloadRecordToDevice(record: CompressionRecord) {
        viewModelScope.launch {
            val result = repository.downloadToDevice(record)
            result.onSuccess { savedPath ->
                _uiState.update {
                    it.copy(
                        lastDownloadedPath = savedPath,
                        statusBannerMessage = "Downloaded to device: $savedPath"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        statusBannerMessage = "Download error: ${err.localizedMessage ?: "Unable to save"}"
                    )
                }
            }
        }
    }

    fun shareRecord(record: CompressionRecord) {
        val res = repository.shareCompressedFile(record)
        res.onFailure { err ->
            _uiState.update {
                it.copy(statusBannerMessage = "Unable to open Share sheet: ${err.localizedMessage}")
            }
        }
    }

    fun setHistoryCategoryFilter(filter: String) {
        _uiState.update { it.copy(historyCategoryFilter = filter) }
    }

    fun deleteHistoryRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _uiState.update { it.copy(statusBannerMessage = "Compression history cleared.") }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { state ->
            val next = when (state.isDarkMode) {
                null -> true
                true -> false
                false -> null
            }
            state.copy(isDarkMode = next)
        }
    }

    fun dismissStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = CompressionDatabase.getInstance(context)
            val repo = CompressionRepository(context.applicationContext, db.compressionDao())
            return CompressionViewModel(repo) as T
        }
    }
}
