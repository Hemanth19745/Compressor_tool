package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.StudioOperationMode
import com.example.data.ToolCategory
import com.example.ui.AppDestination
import com.example.ui.CompressionViewModel
import com.example.ui.DashboardScreen
import com.example.ui.HistoryScreen
import com.example.ui.StudioAndPdfMergeScreen
import com.example.ui.theme.PastelMintAccent
import com.example.ui.theme.PastelMintContainer
import com.example.ui.theme.SqueezeStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: CompressionViewModel = viewModel(
                factory = CompressionViewModel.Factory(context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val useDark = uiState.isDarkMode ?: systemDark

            SqueezeStudioTheme(darkTheme = useDark) {
                SqueezeStudioApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SqueezeStudioApp(
    viewModel: CompressionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track which PDF slot is currently being requested (1, 2, or 3+)
    var activePdfMergeSlot by remember { mutableIntStateOf(1) }

    // Zero-permission Photo Picker for images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onSingleFileUriPicked(uri)
        }
    }

    // Universal Document Picker (SAF OpenDocument) for PDFs, Office, E-Books, Text, Archives, or any file
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onSingleFileUriPicked(uri)
        }
    }

    // Dedicated PDF Picker for Dual-PDF Merge slots
    val mergePdfSlotPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onMergePdfSlotPicked(activePdfMergeSlot, uri)
        }
    }

    // Handle Android Back button when on secondary screens
    BackHandler(enabled = uiState.currentDestination != AppDestination.DASHBOARD) {
        viewModel.navigateTo(AppDestination.DASHBOARD)
    }

    // Show status messages in Snackbar
    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusBanner()
        }
    }

    val launchSingleFilePickerForCategory: (ToolCategory) -> Unit = { category ->
        when (category) {
            ToolCategory.IMAGE -> {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            ToolCategory.PDF -> {
                documentPickerLauncher.launch(arrayOf("application/pdf"))
            }
            ToolCategory.OFFICE_EBOOK -> {
                documentPickerLauncher.launch(
                    arrayOf(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.oasis.opendocument.text",
                        "application/vnd.oasis.opendocument.spreadsheet",
                        "application/vnd.oasis.opendocument.presentation",
                        "application/epub+zip",
                        "*/*"
                    )
                )
            }
            ToolCategory.TEXT_ARCHIVE -> {
                documentPickerLauncher.launch(arrayOf("*/*"))
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = PastelMintContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Compress,
                                        contentDescription = null,
                                        tint = PastelMintAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "SqueezeStudio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("toggle_theme_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle color theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = uiState.currentDestination == AppDestination.DASHBOARD,
                            onClick = { viewModel.navigateTo(AppDestination.DASHBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentDestination == AppDestination.DASHBOARD)
                                        Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard"
                                )
                            },
                            label = { Text("Dashboard") },
                            modifier = Modifier.testTag("nav_item_dashboard")
                        )
                        NavigationBarItem(
                            selected = uiState.currentDestination == AppDestination.STUDIO,
                            onClick = { viewModel.navigateTo(AppDestination.STUDIO) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentDestination == AppDestination.STUDIO)
                                        Icons.Filled.Tune else Icons.Outlined.Tune,
                                    contentDescription = "Compression Studio"
                                )
                            },
                            label = { Text("Studio") },
                            modifier = Modifier.testTag("nav_item_studio")
                        )
                        NavigationBarItem(
                            selected = uiState.currentDestination == AppDestination.PDF_MERGER,
                            onClick = { viewModel.navigateTo(AppDestination.PDF_MERGER) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentDestination == AppDestination.PDF_MERGER)
                                        Icons.AutoMirrored.Filled.CallMerge else Icons.Outlined.CallMerge,
                                    contentDescription = "Merge 2 PDFs"
                                )
                            },
                            label = { Text("Merge 2 PDFs") },
                            modifier = Modifier.testTag("nav_item_pdf_merger")
                        )
                        NavigationBarItem(
                            selected = uiState.currentDestination == AppDestination.HISTORY,
                            onClick = { viewModel.navigateTo(AppDestination.HISTORY) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (historyRecords.isNotEmpty()) {
                                            Badge { Text("${historyRecords.size}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.currentDestination == AppDestination.HISTORY)
                                            Icons.Filled.History else Icons.Outlined.History,
                                        contentDescription = "History"
                                    )
                                }
                            },
                            label = { Text("History") },
                            modifier = Modifier.testTag("nav_item_history")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail {
                        NavigationRailItem(
                            selected = uiState.currentDestination == AppDestination.DASHBOARD,
                            onClick = { viewModel.navigateTo(AppDestination.DASHBOARD) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentDestination == AppDestination.STUDIO,
                            onClick = { viewModel.navigateTo(AppDestination.STUDIO) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Studio") },
                            label = { Text("Studio") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentDestination == AppDestination.PDF_MERGER,
                            onClick = { viewModel.navigateTo(AppDestination.PDF_MERGER) },
                            icon = { Icon(Icons.AutoMirrored.Filled.CallMerge, contentDescription = "Merge 2 PDFs") },
                            label = { Text("Merge 2 PDFs") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentDestination == AppDestination.HISTORY,
                            onClick = { viewModel.navigateTo(AppDestination.HISTORY) },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History") }
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)

                    when (uiState.currentDestination) {
                        AppDestination.DASHBOARD -> {
                            DashboardScreen(
                                historyRecords = historyRecords,
                                onSelectCategory = { cat, mergePdfs ->
                                    viewModel.selectCategoryAndOpenStudio(cat, mergePdfs)
                                },
                                onPickAnyFile = {
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                },
                                onLoadSample = { cat, ext ->
                                    viewModel.loadSampleIntoStudio(cat, ext)
                                },
                                onLoadTwoSamplePdfs = {
                                    viewModel.loadTwoSamplePdfsForMerge()
                                },
                                modifier = contentModifier
                            )
                        }

                        AppDestination.STUDIO, AppDestination.PDF_MERGER -> {
                            StudioAndPdfMergeScreen(
                                uiState = uiState,
                                isDualPdfMode = uiState.currentDestination == AppDestination.PDF_MERGER,
                                onSwitchCategory = { cat, mergeMode ->
                                    viewModel.selectCategoryAndOpenStudio(cat, mergeMode)
                                },
                                onOperationModeChange = { viewModel.setOperationMode(it) },
                                onPickSingleFile = launchSingleFilePickerForCategory,
                                onPickMergePdfSlot = { slot ->
                                    activePdfMergeSlot = slot
                                    mergePdfSlotPickerLauncher.launch(arrayOf("application/pdf"))
                                },
                                onSwapMergePdfs = { viewModel.swapPrimaryAndSecondaryPdfs() },
                                onClearMergePdfSlot = { slot -> viewModel.clearMergePdfSlot(slot) },
                                onUpdateMergedFileName = { viewModel.updateMergedPdfFileName(it) },
                                onLoadSampleFile = { cat, ext ->
                                    viewModel.loadSampleIntoStudio(cat, ext)
                                },
                                onLoadTwoSamplePdfs = {
                                    viewModel.loadTwoSamplePdfsForMerge()
                                },
                                onSelectPreset = { viewModel.applyPreset(it) },
                                onCompressionPercentChange = { viewModel.updateCompressionPercent(it) },
                                onResolutionScaleChange = { viewModel.updateResolutionScale(it) },
                                onTargetFormatChange = { viewModel.updateTargetFormatOverride(it) },
                                onExecuteSingleCompress = { viewModel.executeSingleFileCompression() },
                                onExecuteDualPdfMergeAndCompress = { viewModel.executeDualPdfMergeAndCompress() },
                                onDownloadResult = { viewModel.downloadRecordToDevice(it) },
                                onShareResult = { viewModel.shareRecord(it) },
                                modifier = contentModifier
                            )
                        }

                        AppDestination.HISTORY -> {
                            HistoryScreen(
                                records = historyRecords,
                                selectedFilter = uiState.historyCategoryFilter,
                                onSelectFilter = { viewModel.setHistoryCategoryFilter(it) },
                                onDownloadRecord = { viewModel.downloadRecordToDevice(it) },
                                onShareRecord = { viewModel.shareRecord(it) },
                                onDeleteRecord = { viewModel.deleteHistoryRecord(it) },
                                onClearAll = { viewModel.clearAllHistory() },
                                onNavigateToDashboard = { viewModel.navigateTo(AppDestination.DASHBOARD) },
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }
        }
    }
}
