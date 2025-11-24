package com.example.kmaerm.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.HoSoTaiLieu
import com.example.kmaerm.ui.viewmodel.HoSoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoSoDetailScreen(
    hoSoId: String,
    onNavigateBack: () -> Unit,
    viewModel: HoSoViewModel = viewModel()
) {
    val context = LocalContext.current
    val hoSo by viewModel.selectedHoSo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(hoSoId) {
        viewModel.loadHoSoDetail(hoSoId)
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại",tint = Color(0xFF333333) )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && hoSo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0056B3))
            }
        } else {
            hoSo?.let { detail ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HoSoInfoCard(detail)
                    }

                    item {
                        Text(
                            text = "Required Documents",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    detail.ho_so_tai_lieus?.forEach { hoSoTaiLieu ->
                        item {
                            DocumentCard(
                                hoSoId = hoSoId,
                                hoSoTaiLieu = hoSoTaiLieu,
                                isUploading = uploadProgress.containsKey("upload-${hoSoTaiLieu.id}"),
                                viewModel = viewModel
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HoSoInfoCard(hoSo: com.example.kmaerm.data.model.HoSo) {
    val statusText = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> "Đã duyệt"
        "ChoDuyet" -> "Chờ duyệt"
        "TuChoi" -> "Từ chối"
        "MoiTao" -> "Mới tạo"
        else -> hoSo.trang_thai_ho_so
    }

    val statusColor = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> Color(0xFF16A34A)
        "ChoDuyet" -> Color(0xFFF59E0B)
        "MoiTao" -> Color(0xFF3B82F6)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profile ID",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = hoSo.ma_ho_so,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Loại thủ tục:", hoSo.loai_thu_tuc)
            InfoRow("Doanh nghiệp:", hoSo.ten_doanh_nghiep_vi)
        }
    }
}

@Composable
fun DocumentCard(
    hoSoId: String,
    hoSoTaiLieu: HoSoTaiLieu,
    isUploading: Boolean,
    viewModel: HoSoViewModel
) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = c.getString(nameIndex)
                    }
                }
            }
        }
    }

    val hasUploadedFiles = !hoSoTaiLieu.tai_lieus.isNullOrEmpty()
    val borderColor = if (hasUploadedFiles) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasUploadedFiles) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hoSoTaiLieu.loai_tai_lieu.ten,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    if (hoSoTaiLieu.loai_tai_lieu.mo_ta != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hoSoTaiLieu.loai_tai_lieu.mo_ta,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                if (hasUploadedFiles) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Uploaded",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (!hoSoTaiLieu.tai_lieus.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                hoSoTaiLieu.tai_lieus.forEach { taiLieu ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = taiLieu.tieu_de,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                                taiLieu.created_at?.let {
                                    val formattedDate = try {
                                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        val date = inputFormat.parse(it.substring(0, 19))
                                        "Uploaded on ${outputFormat.format(date ?: Date())}"
                                    } catch (e: Exception) {
                                        it
                                    }
                                    Text(
                                        text = formattedDate,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.downloadTaiLieu(context, taiLieu.id, taiLieu.tieu_de)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Xem",
                                    tint = Color(0xFF0056B3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showDeleteDialog = taiLieu.id }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { filePickerLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0056B3)
                ),
                enabled = !isUploading
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUploading) "Uploading..." else if (hasUploadedFiles) "Upload thêm" else "Upload File"
                )
            }

            selectedFileUri?.let { uri ->
                LaunchedEffect(uri) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = File(context.cacheDir, fileName)
                        inputStream?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        viewModel.uploadTaiLieu(hoSoTaiLieu.id, file, fileName)
                        selectedFileUri = null
                        fileName = ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { taiLieuId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa tài liệu này?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTaiLieu(taiLieuId, hoSoId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
    }
}
