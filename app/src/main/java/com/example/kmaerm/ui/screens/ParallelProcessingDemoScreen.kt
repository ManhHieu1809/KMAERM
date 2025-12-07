package com.example.kmaerm.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.HoSo
import com.example.kmaerm.ui.viewmodel.HoSoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Demo screen showcasing parallel processing capabilities.
 * Demonstrates sequential vs parallel operations with real-time metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParallelProcessingDemoScreen(
    viewModel: HoSoViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get doanhNghiepId from TokenDataStore (where login data is saved)
    val tokenDataStore = remember { com.example.kmaerm.data.datastore.TokenDataStore(context) }
    val doanhNghiepId by tokenDataStore.doanhNghiepId.collectAsState(initial = null)

    // State collections
    val hoSoList by viewModel.hoSoList.collectAsState()
    val parallelLoading by viewModel.parallelLoadingState.collectAsState()
    val batchUploadProgress by viewModel.batchUploadProgress.collectAsState()
    val batchOperationResult by viewModel.batchOperationResult.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Local UI state
    var selectedHoSoIds by remember { mutableStateOf(setOf<String>()) }
    var sequentialTime by remember { mutableStateOf<Long?>(null) }
    var parallelTime by remember { mutableStateOf<Long?>(null) }
    var isComparingResults by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isInitialLoad by remember { mutableStateOf(true) }

    // State for comparing different operations
    var currentOperation by remember { mutableStateOf("") }
    var batchApproveSeqTime by remember { mutableStateOf<Long?>(null) }
    var batchApproveParTime by remember { mutableStateOf<Long?>(null) }
    var downloadSeqTime by remember { mutableStateOf<Long?>(null) }
    var downloadParTime by remember { mutableStateOf<Long?>(null) }

    // Load data on first launch - only after doanhNghiepId is loaded from DataStore
    LaunchedEffect(doanhNghiepId) {
        // Skip the initial null value (DataStore hasn't loaded yet)
        if (doanhNghiepId == null && isInitialLoad) {
            // Wait a bit for DataStore to load
            delay(500)
            isInitialLoad = false
            return@LaunchedEffect
        }

        val currentDoanhNghiepId = doanhNghiepId
        if (!currentDoanhNghiepId.isNullOrEmpty()) {
            viewModel.loadHoSoList(currentDoanhNghiepId)
        } else if (!isInitialLoad) {
            // Only show error after we've confirmed DataStore has loaded
            snackbarHostState.showSnackbar(
                message = "Không tìm thấy ID doanh nghiệp. Vui lòng đăng nhập lại.",
                duration = SnackbarDuration.Long
            )
        }
        isInitialLoad = false
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.clipData?.let { clipData ->
                val files = mutableListOf<File>()
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    // Convert URI to File (simplified - in production use proper content resolver)
                    try {
                        val file = File(uri.path ?: "")
                        if (file.exists()) files.add(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                selectedFiles = files
            }
        }
    }

    // Effects for messages
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(batchOperationResult) {
        batchOperationResult?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearBatchOperationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demo Xử lý Song song") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0056B3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics Card
            item {
                StatisticsCard(
                    totalHoSo = hoSoList.size,
                    selectedCount = selectedHoSoIds.size,
                    isLoading = parallelLoading,
                    progress = if (batchUploadProgress.second > 0) {
                        batchUploadProgress.first.toFloat() / batchUploadProgress.second
                    } else 0f
                )
            }

            // Action Buttons Row
            item {
                ActionButtonsRow(
                    onSequential = {
                        scope.launch {
                            sequentialTime = measureTimeMillis {
                                // Simulate sequential loading
                                delay(hoSoList.size * 100L) // Simulate delay per item
                            }
                        }
                    },
                    onParallel = {
                        val currentId = doanhNghiepId
                        if (!currentId.isNullOrEmpty()) {
                            scope.launch {
                                parallelTime = measureTimeMillis {
                                    viewModel.loadHoSoList(currentId)
                                }
                            }
                        }
                    },
                    onCompare = {
                        isComparingResults = !isComparingResults
                    },
                    isLoading = parallelLoading
                )
            }

            // Performance Metrics
            if (sequentialTime != null || parallelTime != null) {
                item {
                    PerformanceMetricsCard(
                        sequentialTime = sequentialTime,
                        parallelTime = parallelTime,
                        isVisible = isComparingResults
                    )
                }
            }

            // Operations Grid
            item {
                Text(
                    text = "Thao tác Song song",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            item {
                OperationsGridWithComparison(
                    onLoadData = {
                        val currentId = doanhNghiepId
                        if (!currentId.isNullOrEmpty()) {
                            viewModel.loadHoSoList(currentId)
                        }
                    },
                    onUploadFiles = {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        filePickerLauncher.launch(intent)
                    },
                    onBatchApproveSequential = {
                        if (selectedHoSoIds.isNotEmpty()) {
                            scope.launch {
                                currentOperation = "Batch Process"
                                // Mô phỏng tuần tự: 300ms cho mỗi hồ sơ
                                batchApproveSeqTime = measureTimeMillis {
                                    for (i in selectedHoSoIds.indices) {
                                        delay(300)
                                    }
                                }
                            }
                        }
                    },
                    onBatchApproveParallel = {
                        if (selectedHoSoIds.isNotEmpty()) {
                            scope.launch {
                                currentOperation = "Batch Process"
                                // Mô phỏng song song với concurrency = 3
                                batchApproveParTime = measureTimeMillis {
                                    val chunks = selectedHoSoIds.chunked(3)
                                    for (chunk in chunks) {
                                        delay(300)
                                    }
                                }
                            }
                        }
                    },
                    onDownloadSequential = {
                        val selectedHoSo = hoSoList.filter { it.id in selectedHoSoIds }
                        val fileCount = selectedHoSo.flatMap { hoSo ->
                            hoSo.ho_so_tai_lieus?.flatMap { it.tai_lieus ?: emptyList() } ?: emptyList()
                        }.size.coerceAtLeast(selectedHoSoIds.size)

                        scope.launch {
                            currentOperation = "Download"
                            // Mô phỏng tuần tự: 500ms cho mỗi file
                            downloadSeqTime = measureTimeMillis {
                                for (i in 0 until fileCount) {
                                    delay(500)
                                }
                            }
                        }
                    },
                    onDownloadParallel = {
                        val selectedHoSo = hoSoList.filter { it.id in selectedHoSoIds }
                        val fileCount = selectedHoSo.flatMap { hoSo ->
                            hoSo.ho_so_tai_lieus?.flatMap { it.tai_lieus ?: emptyList() } ?: emptyList()
                        }.size.coerceAtLeast(selectedHoSoIds.size)

                        scope.launch {
                            currentOperation = "Download"
                            // Mô phỏng song song với concurrency = 5
                            downloadParTime = measureTimeMillis {
                                val chunks = (0 until fileCount).chunked(5)
                                for (chunk in chunks) {
                                    delay(500)
                                }
                            }
                        }
                    },
                    isLoading = parallelLoading,
                    hasSelection = selectedHoSoIds.isNotEmpty(),
                    selectedCount = selectedHoSoIds.size,
                    batchApproveSeqTime = batchApproveSeqTime,
                    batchApproveParTime = batchApproveParTime,
                    downloadSeqTime = downloadSeqTime,
                    downloadParTime = downloadParTime
                )
            }

            // HoSo List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh sách Hồ sơ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    if (selectedHoSoIds.isNotEmpty()) {
                        TextButton(
                            onClick = { selectedHoSoIds = emptySet() }
                        ) {
                            Text("Bỏ chọn tất cả")
                        }
                    }
                }
            }

            // Selectable HoSo List
            if (hoSoList.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(hoSoList) { hoSo ->
                    SelectableHoSoItem(
                        hoSo = hoSo,
                        isSelected = hoSo.id in selectedHoSoIds,
                        onToggleSelection = {
                            selectedHoSoIds = if (hoSo.id in selectedHoSoIds) {
                                selectedHoSoIds - hoSo.id
                            } else {
                                selectedHoSoIds + hoSo.id
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(
    totalHoSo: Int,
    selectedCount: Int,
    isLoading: Boolean,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Thống kê",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Tổng hồ sơ",
                    value = totalHoSo.toString(),
                    color = Color(0xFF0056B3)
                )
                StatItem(
                    label = "Đã chọn",
                    value = selectedCount.toString(),
                    color = Color(0xFF10B981)
                )
            }

            AnimatedVisibility(visible = isLoading || progress > 0f) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tiến trình xử lý",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    LinearProgressIndicator(
                        progress = { if (isLoading && progress == 0f) 0f else progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun ActionButtonsRow(
    onSequential: () -> Unit,
    onParallel: () -> Unit,
    onCompare: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSequential,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B)
            )
        ) {
            Text("Tuần tự")
        }

        Button(
            onClick = onParallel,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0056B3)
            )
        ) {
            Text("Song song")
        }

        Button(
            onClick = onCompare,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            )
        ) {
            Text("So sánh")
        }
    }
}

@Composable
fun PerformanceMetricsCard(
    sequentialTime: Long?,
    parallelTime: Long?,
    isVisible: Boolean
) {
    AnimatedVisibility(visible = isVisible) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kết quả Đo lường",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                sequentialTime?.let {
                    MetricRow(
                        label = "Tuần tự",
                        value = "${it}ms",
                        color = Color(0xFFF59E0B)
                    )
                }

                parallelTime?.let {
                    MetricRow(
                        label = "Song song",
                        value = "${it}ms",
                        color = Color(0xFF10B981)
                    )
                }

                if (sequentialTime != null && parallelTime != null && parallelTime > 0) {
                    val speedup = sequentialTime.toFloat() / parallelTime
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tăng tốc",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "${String.format("%.2f", speedup)}x",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun OperationsGrid(
    onLoadData: () -> Unit,
    onUploadFiles: () -> Unit,
    onBatchApprove: () -> Unit,
    onDownloadAll: () -> Unit,
    isLoading: Boolean,
    hasSelection: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OperationButton(
                icon = Icons.Default.CloudDownload,
                label = "Load Data",
                onClick = onLoadData,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                color = Color(0xFF0056B3)
            )
            OperationButton(
                icon = Icons.Default.Upload,
                label = "Upload Files",
                onClick = onUploadFiles,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                color = Color(0xFF10B981)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OperationButton(
                icon = Icons.Default.CheckCircle,
                label = "Batch Approve",
                onClick = onBatchApprove,
                enabled = !isLoading && hasSelection,
                modifier = Modifier.weight(1f),
                color = Color(0xFFF59E0B)
            )
            OperationButton(
                icon = Icons.Default.Download,
                label = "Download PDFs",
                onClick = onDownloadAll,
                enabled = !isLoading && hasSelection,
                modifier = Modifier.weight(1f),
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun OperationsGridWithComparison(
    onLoadData: () -> Unit,
    onUploadFiles: () -> Unit,
    onBatchApproveSequential: () -> Unit,
    onBatchApproveParallel: () -> Unit,
    onDownloadSequential: () -> Unit,
    onDownloadParallel: () -> Unit,
    isLoading: Boolean,
    hasSelection: Boolean,
    selectedCount: Int,
    batchApproveSeqTime: Long?,
    batchApproveParTime: Long?,
    downloadSeqTime: Long?,
    downloadParTime: Long?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Load Data & Upload Files
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OperationButton(
                icon = Icons.Default.CloudDownload,
                label = "Load Data",
                onClick = onLoadData,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                color = Color(0xFF0056B3)
            )
            OperationButton(
                icon = Icons.Default.Upload,
                label = "Upload Files",
                onClick = onUploadFiles,
                enabled = !isLoading,
                modifier = Modifier.weight(1f),
                color = Color(0xFF10B981)
            )
        }

        // Batch Approve Section with Sequential vs Parallel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batch Process ($selectedCount hồ sơ)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBatchApproveSequential,
                        enabled = !isLoading && hasSelection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tuần tự", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onBatchApproveParallel,
                        enabled = !isLoading && hasSelection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Song song", fontSize = 12.sp)
                    }
                }

                // Show times if available
                if (batchApproveSeqTime != null || batchApproveParTime != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        batchApproveSeqTime?.let {
                            Text("Tuần tự: ${it}ms", fontSize = 11.sp, color = Color(0xFFFF9800))
                        }
                        batchApproveParTime?.let {
                            Text("Song song: ${it}ms", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                        if (batchApproveSeqTime != null && batchApproveParTime != null && batchApproveParTime > 0) {
                            val speedup = batchApproveSeqTime.toFloat() / batchApproveParTime
                            Text(
                                "⚡ ${String.format(java.util.Locale.US, "%.1f", speedup)}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }

        // Download PDFs Section with Sequential vs Parallel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download PDFs",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownloadSequential,
                        enabled = !isLoading && hasSelection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tuần tự", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onDownloadParallel,
                        enabled = !isLoading && hasSelection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Song song", fontSize = 12.sp)
                    }
                }

                // Show times if available
                if (downloadSeqTime != null || downloadParTime != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        downloadSeqTime?.let {
                            Text("Tuần tự: ${it}ms", fontSize = 11.sp, color = Color(0xFFFF9800))
                        }
                        downloadParTime?.let {
                            Text("Song song: ${it}ms", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                        if (downloadSeqTime != null && downloadParTime != null && downloadParTime > 0) {
                            val speedup = downloadSeqTime.toFloat() / downloadParTime
                            Text(
                                "⚡ ${String.format(java.util.Locale.US, "%.1f", speedup)}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperationButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    color: Color
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) color.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.1f),
            contentColor = if (enabled) color else Color.Gray
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SelectableHoSoItem(
    hoSo: HoSo,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF0056B3),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF0056B3)
                    )
                )

                Column {
                    Text(
                        text = hoSo.ma_ho_so,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = hoSo.ten_doanh_nghiep_vi,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Loại: ${hoSo.loai_thu_tuc}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            StatusBadge(status = hoSo.trang_thai_ho_so)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "DaDangKy" -> Color(0xFF0056B3) to "Đã đăng ký"
        "DangXuLy" -> Color(0xFFF59E0B) to "Đang xử lý"
        "DaDuyet" -> Color(0xFF10B981) to "Đã duyệt"
        "TuChoi" -> Color(0xFFEF4444) to "Từ chối"
        else -> Color(0xFF666666) to status
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFCCCCCC)
            )
            Text(
                text = "Chưa có dữ liệu",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "Nhấn 'Load Data' hoặc 'Song song' để tải dữ liệu",
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

