package com.example.kmaerm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.ComparisonUiModel
import com.example.kmaerm.data.model.DemoResult
import com.example.kmaerm.ui.viewmodel.DemoComparisonViewModel
import java.util.Locale

private val PrimaryBlue = Color(0xFF0056B3)
private val SequentialRed = Color(0xFFDC3545)
private val ParallelGreen = Color(0xFF28A745)
private val BackgroundGray = Color(0xFFF5F5F5)
private val WarningYellow = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoComparisonScreen(
    viewModel: DemoComparisonViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demo So sánh Hiệu năng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderCard()
            ScenarioInfoCard()
            ActionButtonsSection(
                uiState = uiState,
                onSequential = { viewModel.runSequentialOnly() },
                onParallel = { viewModel.runParallelOnly() },
                onCompare = { viewModel.runComparison() }
            )

            when (val state = uiState) {
                is DemoComparisonViewModel.UiState.Idle -> IdleContent()
                is DemoComparisonViewModel.UiState.Loading -> LoadingContent(loadingType = state.type)
                is DemoComparisonViewModel.UiState.Success -> SuccessContent(data = state.data)
                is DemoComparisonViewModel.UiState.Error -> ErrorContent(message = state.message)
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("So sánh Xử lý Tuần tự vs Song song", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ScenarioInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kịch bản Demo", color = Color.DarkGray, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "50 doanh nghiệp nộp hồ sơ cùng lúc",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tuần tự", fontWeight = FontWeight.Bold, color = SequentialRed)
                    Text("Single Core", fontSize = 12.sp, color = Color.Gray)
                    Text("Blocking I/O", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Song song", fontWeight = FontWeight.Bold, color = ParallelGreen)
                    Text("Multi-Core", fontSize = 12.sp, color = Color.Gray)
                    Text("10 Workers", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    uiState: DemoComparisonViewModel.UiState,
    onSequential: () -> Unit,
    onParallel: () -> Unit,
    onCompare: () -> Unit
) {
    val isLoading = uiState is DemoComparisonViewModel.UiState.Loading

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Chọn phương thức xử lý:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSequential,
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SequentialRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tuần tự", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onParallel,
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ParallelGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Speed, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Song song", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onCompare,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Compare, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("So sánh cả hai", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun IdleContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.TouchApp, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chọn phương thức để bắt đầu", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LoadingContent(loadingType: DemoComparisonViewModel.LoadingType) {
    val (title, subtitle) = when (loadingType) {
        DemoComparisonViewModel.LoadingType.SEQUENTIAL -> "Đang gọi API tuần tự..." to "🔄 /demo/sequential"
        DemoComparisonViewModel.LoadingType.PARALLEL -> "Đang gọi API song song..." to "🔄 /demo/parallel"
        DemoComparisonViewModel.LoadingType.BOTH -> "Đang gọi 2 API song song..." to "🔄 /demo/sequential\n🔄 /demo/parallel"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp), color = PrimaryBlue, strokeWidth = 4.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
            if (loadingType == DemoComparisonViewModel.LoadingType.BOTH) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Coroutine async/await đang chạy...", fontSize = 12.sp, color = PrimaryBlue)
            }
        }
    }
}

@Composable
private fun SuccessContent(data: ComparisonUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        data.sequentialResult?.let { ResultCard(" Xử lý Tuần tự", it, SequentialRed) }
        data.parallelResult?.let { ResultCard(" Xử lý Song song", it, ParallelGreen) }
        if (data.sequentialResult != null && data.parallelResult != null) {
            ComparisonCard(data = data)
        }
    }
}

@Composable
private fun ResultCard(title: String, result: DemoResult, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
                StatusBadge(status = result.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(result.mode.replace("_", " "), fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem("Tổng thời gian", "${result.metrics.totalTimeMs}ms", Icons.Default.Timer)
                MetricItem("Latency TB", result.metrics.avgLatency, Icons.Default.Speed)
                MetricItem("Throughput", result.metrics.throughput, Icons.AutoMirrored.Filled.TrendingUp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Infrastructure:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            InfraRow("CPU", result.infrastructure.cpuUtilization)
            InfraRow("I/O", result.infrastructure.ioStrategy)
            InfraRow("Worker Pool", result.infrastructure.workerPool)

            result.optimizationGain?.let { gain ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = ParallelGreen.copy(alpha = 0.1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(" ${gain.speedUp}", fontWeight = FontWeight.Bold, color = ParallelGreen)
                        Text(gain.conclusion, fontSize = 12.sp, color = ParallelGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("${result.message}", fontSize = 13.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

@Composable
private fun InfraRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "SUCCESS" -> ParallelGreen to Color.White
        "SLOW" -> WarningYellow to Color.Black
        else -> SequentialRed to Color.White
    }
    Surface(shape = RoundedCornerShape(12.dp), color = bgColor) {
        Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun MetricItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun ComparisonCard(data: ComparisonUiModel) {
    val seqTime = data.sequentialResult?.metrics?.totalTimeMs ?: 0
    val parTime = data.parallelResult?.metrics?.totalTimeMs ?: 1
    val maxTime = maxOf(seqTime, parTime)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("So sánh Trực quan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            data.speedupRatio?.let { HighlightRow("Tỉ lệ tăng tốc", String.format(Locale.US, "%.2fx", it), it > 1.5) }
            data.timeSaved?.let { HighlightRow("Thời gian tiết kiệm", "${it}ms", it > 0) }
            data.efficiencyGain?.let { HighlightRow("Hiệu suất tăng", String.format(Locale.US, "%.1f%%", it), it > 50) }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Biểu đồ so sánh thời gian:", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tuần tự", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                Box(modifier = Modifier.weight(1f).height(24.dp).background(Color.LightGray, RoundedCornerShape(4.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(seqTime.toFloat() / maxTime).background(SequentialRed, RoundedCornerShape(4.dp)))
                }
                Text("${seqTime}ms", fontSize = 12.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Song song", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                Box(modifier = Modifier.weight(1f).height(24.dp).background(Color.LightGray, RoundedCornerShape(4.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(parTime.toFloat() / maxTime).background(ParallelGreen, RoundedCornerShape(4.dp)))
                }
                Text("${parTime}ms", fontSize = 12.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = ParallelGreen.copy(alpha = 0.1f)) {
                Text("Xử lý song song nhanh hơn ${String.format(Locale.US, "%.1f", data.speedupRatio ?: 1.0)}x!", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = ParallelGreen, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun HighlightRow(label: String, value: String, highlight: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (highlight) ParallelGreen else Color.DarkGray)
    }
}

@Composable
private fun ErrorContent(message: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SequentialRed.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Error, null, tint = SequentialRed, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Lỗi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SequentialRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}