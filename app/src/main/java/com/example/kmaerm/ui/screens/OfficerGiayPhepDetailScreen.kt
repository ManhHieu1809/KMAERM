package com.example.kmaerm.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.kmaerm.data.model.GiayPhep
import com.example.kmaerm.data.model.UpdateGiayPhepRequest
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerGiayPhepDetailScreen(
    giayPhep: GiayPhep,
    onNavigateBack: () -> Unit,
    viewModel: GiayPhepViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val selectedGiayPhep by viewModel.selectedGiayPhep.collectAsState()

    val currentGiayPhep = selectedGiayPhep ?: giayPhep

    var selectedStatus by remember { mutableStateOf(currentGiayPhep.trang_thai_giay_phep ?: "") }
    var expandedStatus by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var showDeleteFileDialog by remember { mutableStateOf(false) }

    // Update selected status when currentGiayPhep changes
    LaunchedEffect(currentGiayPhep.trang_thai_giay_phep) {
        selectedStatus = currentGiayPhep.trang_thai_giay_phep ?: ""
    }

    LaunchedEffect(giayPhep) {
        viewModel.setSelectedGiayPhep(giayPhep)
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "license_${giayPhep.id}.pdf")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                viewModel.uploadFile(giayPhep.id, file)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi đọc file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()

            // Reload giay phep after successful operations to update UI
            if (message.contains("Ký số", ignoreCase = true) ||
                message.contains("Upload", ignoreCase = true) ||
                message.contains("Xóa file", ignoreCase = true)) {
                viewModel.reloadGiayPhep(giayPhep.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "License #${giayPhep.so_giay_phep}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF333333))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F8))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // File Management Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "File Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                val hasFile = currentGiayPhep.file_duong_dan?.isNotEmpty() == true
                val borderColor = if (hasFile) Color(0xFF10B981) else Color(0xFFEF4444)
                val backgroundColor = if (hasFile) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title with checkmark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Giấy phép đã ký",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sử dụng khi doanh nghiệp có thay đổi nội dung trên giấy phép đã được cấp.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 18.sp
                                )
                            }

                            if (hasFile) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Uploaded",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // File Display (if exists)
                        if (hasFile) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
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
                                            text = "${currentGiayPhep.loai_giay_phep} - Gửi Sý.pdf",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E293B)
                                        )
                                        currentGiayPhep.updated_at?.let { updatedAt ->
                                            val formattedDate = try {
                                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                                val date = inputFormat.parse(updatedAt.substring(0, 19))
                                                "Uploaded on ${outputFormat.format(date ?: Date())}"
                                            } catch (e: Exception) {
                                                "Uploaded on ${updatedAt.take(10)}"
                                            }
                                            Text(
                                                text = formattedDate,
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    // View Button
                                    IconButton(
                                        onClick = {
                                            viewModel.viewFile(context, currentGiayPhep.id, currentGiayPhep.so_giay_phep)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "View",
                                            tint = Color(0xFF0056B3),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            showDeleteFileDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Upload Button
                        Button(
                            onClick = { filePickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0056B3)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(14.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uploading...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (hasFile) "Replace File" else "Upload File",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Digital Signature Button (only show if file exists)
                        if (hasFile) {
                            Button(
                                onClick = {
                                    viewModel.signLicense(giayPhep.id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6) // Purple color for signature
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(14.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đang ký số...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Ký số giấy phép",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Blockchain Security Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Blockchain Security",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                when (currentGiayPhep.trang_thai_blockchain) {
                    "TrangThaiBCDaDongBo" -> {
                        // Verified State - Green
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF10B981).copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Status Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Trạng thái",
                                            fontSize = 12.sp,
                                            color = Color(0xFF059669)
                                        )
                                        Text(
                                            text = "Đã đồng bộ Blockchain",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }

                                // Transaction Hash
                                if (currentGiayPhep.transaction_hash != null) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Transaction Hash",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = currentGiayPhep.transaction_hash,
                                                fontSize = 13.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Verify Hash Button
                                Button(
                                    onClick = { showVerifyDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E293B).copy(alpha = 0.05f),
                                        contentColor = Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(14.dp),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        "Verify Hash",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    "DangDongBo" -> {
                        // Syncing State - Blue
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF3B82F6).copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Status Header with animated icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF3B82F6).copy(alpha = 0.2f)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(8.dp).size(24.dp),
                                            color = Color(0xFF3B82F6),
                                            strokeWidth = 3.dp
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Trạng thái",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2563EB)
                                        )
                                        Text(
                                            text = "Đang đồng bộ Blockchain",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D4ED8)
                                        )
                                    }
                                }

                                // Description
                                Text(
                                    text = "Giấy phép đang được đồng bộ lên blockchain. Quá trình này có thể mất vài phút.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 20.sp
                                )

                                // Transaction Hash (if available)
                                if (currentGiayPhep.transaction_hash != null) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Transaction Hash",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = currentGiayPhep.transaction_hash,
                                                fontSize = 13.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Confirm Blockchain Sync Button
                                Button(
                                    onClick = { showVerifyDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(14.dp),
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Xác nhận đã đồng bộ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    "LoiDongBo" -> {
                        // Error State - Red
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFEF4444).copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Status Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Trạng thái",
                                            fontSize = 12.sp,
                                            color = Color(0xFFDC2626)
                                        )
                                        Text(
                                            text = "Lỗi đồng bộ Blockchain",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }

                                // Error Description
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Đã xảy ra lỗi khi đồng bộ lên blockchain. Vui lòng thử lại.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }

                                // Retry Button
                                Button(
                                    onClick = { viewModel.pushToBlockchain(currentGiayPhep.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(14.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Đang thử lại...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Thử lại đồng bộ",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Not Synced State (ChuaDongBo or null) - Gray
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF64748B).copy(alpha = 0.05f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Status Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF64748B).copy(alpha = 0.2f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Trạng thái",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "Chưa đồng bộ Blockchain",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                // Description
                                Text(
                                    text = "Giấy phép này chưa được bảo mật trên blockchain. Đẩy lên blockchain để tạo bản ghi có thể xác minh và chống giả mạo.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 20.sp
                                )

                                // Push to Blockchain Button
                                Button(
                                    onClick = {
                                        try {
                                            viewModel.pushToBlockchain(currentGiayPhep.id)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Lỗi: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0D83F2)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(14.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Đang đồng bộ...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Đẩy lên Blockchain",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Update Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Update Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    // Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = it }
                    ) {
                        OutlinedTextField(
                            value = when (selectedStatus) {
                                "HieuLuc" -> "Hiệu lực"
                                "SapHetHan" -> "Sắp hết hạn"
                                "ThuHoi" -> "Thu hồi"
                                "DaHetHan" -> "Đã hết hạn"
                                else -> selectedStatus
                            },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D83F2),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            listOf(
                                "HieuLuc" to "Hiệu lực",
                                "SapHetHan" to "Sắp hết hạn",
                                "ThuHoi" to "Thu hồi",
                                "DaHetHan" to "Đã hết hạn"
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedStatus = value
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showUpdateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(14.dp),
                        enabled = selectedStatus != currentGiayPhep.trang_thai_giay_phep && !isLoading
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Update Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Verify Blockchain Dialog
    if (showVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = { Text("Verify Blockchain") },
            text = {
                Text(
                    if (currentGiayPhep.trang_thai_blockchain == "TrangThaiBCDaDongBo") {
                        "Xác minh giấy phép này trên blockchain?"
                    } else {
                        "Xác nhận rằng giấy phép này đã được đưa lên blockchain thành công?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyBlockchain(currentGiayPhep.id)
                        showVerifyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Update Status Confirmation Dialog
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update License Status") },
            text = { Text("Bạn có chắc chắn muốn cập nhật trạng thái giấy phép không?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updateRequest = UpdateGiayPhepRequest(
                            loai_giay_phep = currentGiayPhep.loai_giay_phep,
                            so_giay_phep = currentGiayPhep.so_giay_phep,
                            ngay_hieu_luc = currentGiayPhep.ngay_hieu_luc ?: "",
                            ngay_het_han = currentGiayPhep.ngay_het_han ?: "",
                            trang_thai_giay_phep = selectedStatus
                        )
                        viewModel.updateGiayPhep(currentGiayPhep.id, updateRequest)
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0056B3)
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete File Confirmation Dialog
    if (showDeleteFileDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteFileDialog = false },
            title = { Text("Xóa File") },
            text = { Text("Bạn có chắc chắn muốn xóa file này?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFile(currentGiayPhep.id)
                        showDeleteFileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFileDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}
