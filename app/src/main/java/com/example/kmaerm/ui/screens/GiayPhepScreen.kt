package com.example.kmaerm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.data.model.GiayPhep
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel
import com.example.kmaerm.utils.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiayPhepScreen(
    doanhNghiepId: String,
    viewModel: GiayPhepViewModel = viewModel(),
    onNavigateToDetail: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenDataStore = remember { TokenDataStore(context) }

    val giayPhepList by viewModel.giayPhepList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Biometric state
    var biometricEnabled by remember { mutableStateOf(false) }
    var pendingViewFileGiayPhep by remember { mutableStateOf<GiayPhep?>(null) }

    // Load biometric settings
    LaunchedEffect(Unit) {
        biometricEnabled = tokenDataStore.biometricEnabled.first()
    }

    LaunchedEffect(doanhNghiepId) {
        viewModel.loadGiayPhepList(doanhNghiepId)
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0056B3))
            }
        } else if (giayPhepList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có giấy phép nào",
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(giayPhepList) { giayPhep ->
                    GiayPhepCard(
                        giayPhep = giayPhep,
                        onClick = {
                            // Navigate to detail screen
                            onNavigateToDetail(giayPhep.id, doanhNghiepId)
                        },
                        onViewFile = {
                            // Xem file giấy phép qua API
                            if (!giayPhep.file_duong_dan.isNullOrBlank()) {
                                // Kiểm tra biometric nếu đã bật
                                if (biometricEnabled &&
                                    BiometricHelper.canUseBiometric(context) == BiometricHelper.BiometricStatus.AVAILABLE) {
                                    // Find FragmentActivity
                                    var activity: FragmentActivity? = null
                                    var ctx: android.content.Context = context
                                    while (ctx is android.content.ContextWrapper) {
                                        if (ctx is FragmentActivity) {
                                            activity = ctx
                                            break
                                        }
                                        ctx = ctx.baseContext
                                    }

                                    if (activity != null) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = activity,
                                            title = "Xác thực để xem file",
                                            subtitle = "Xác thực sinh trắc học để xem giấy phép",
                                            onSuccess = {
                                                viewModel.viewFile(context, giayPhep.id, giayPhep.so_giay_phep)
                                            },
                                            onError = { _, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            },
                                            onFailed = {
                                                Toast.makeText(context, "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        // Fallback nếu không tìm được activity
                                        viewModel.viewFile(context, giayPhep.id, giayPhep.so_giay_phep)
                                    }
                                } else {
                                    // Không bật biometric, xem trực tiếp
                                    viewModel.viewFile(context, giayPhep.id, giayPhep.so_giay_phep)
                                }
                            } else {
                                Toast.makeText(context, "Giấy phép chưa có file đính kèm", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GiayPhepCard(
    giayPhep: GiayPhep,
    onClick: () -> Unit = {},
    onViewFile: () -> Unit = {}
) {
    val statusText = when (giayPhep.trang_thai_giay_phep) {
        "HieuLuc" -> "Hiệu lực"
        "SapHetHan" -> "Sắp hết hạn"
        "DaHetHan" -> "Đã hết hạn"
        "ThuHoi" -> "Thu hồi"
        else -> giayPhep.trang_thai_giay_phep
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (_: Exception) {
            try {
                dateString.substring(0, 10).split("-").reversed().joinToString("/")
            } catch (_: Exception) {
                dateString
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0056B3),
                            Color(0xFF4A90E2),
                            Color(0xFF64B5F6)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header - Loại giấy phép và trạng thái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GIẤY PHÉP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = giayPhep.loai_giay_phep,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = when (giayPhep.trang_thai_giay_phep) {
                                            "HieuLuc" -> Color(0xFF4ADE80)
                                            "HetHan", "DaHetHan" -> Color.White
                                            "ThuHoi" -> Color(0xFFEF4444)
                                            "TamDung", "SapHetHan" -> Color(0xFFFBBF24)
                                            else -> Color.White
                                        },
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Số giấy phép
                Column {
                    Text(
                        text = "Số giấy phép",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = giayPhep.so_giay_phep,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Ngày hiệu lực và hết hạn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ngày hiệu lực",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDate(giayPhep.ngay_hieu_luc),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Ngày hết hạn",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDate(giayPhep.ngay_het_han),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                // Hồ sơ liên quan
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Hồ sơ liên quan",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = giayPhep.ho_so?.ma_ho_so ?: "N/A",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Doanh nghiệp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Doanh nghiệp",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = giayPhep.ho_so?.ten_doanh_nghiep_vi ?: "N/A",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Nút xem file giấy phép
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onViewFile() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Xem file giấy phép",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Xem file",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
