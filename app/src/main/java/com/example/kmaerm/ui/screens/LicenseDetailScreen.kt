package com.example.kmaerm.ui.screens

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.GiayPhep
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseDetailScreen(
    giayPhep: GiayPhep,
    doanhNghiepId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: GiayPhepViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    val selectedGiayPhep by viewModel.selectedGiayPhep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val currentGiayPhep = selectedGiayPhep ?: giayPhep

    LaunchedEffect(giayPhep) {
        viewModel.setSelectedGiayPhep(giayPhep)
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

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

    LaunchedEffect(selectedFileUri) {
        selectedFileUri?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.uploadFile(currentGiayPhep.id, file)
                selectedFileUri = null
                fileName = ""
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi upload file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa giấy phép này?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGiayPhep(currentGiayPhep.id, doanhNghiepId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Verify Blockchain Dialog
    if (showVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = { Text("Xác nhận Blockchain") },
            text = { Text("Xác nhận giấy phép này đã được đẩy lên blockchain thành công?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyBlockchain(currentGiayPhep.id)
                        showVerifyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "License #${currentGiayPhep.so_giay_phep}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFF0056B3))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC3545))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File Management Section
            Text(
                text = "File Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentGiayPhep.file_duong_dan != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF0D83F2).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF0D83F2)
                                    )
                                }
                                Text(
                                    text = "License_Signed.pdf",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.viewFile(context, currentGiayPhep.id, currentGiayPhep.so_giay_phep)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View",
                                    tint = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D83F2).copy(alpha = 0.1f),
                            contentColor = Color(0xFF0D83F2)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload Signed File", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Blockchain Security Section
            Text(
                text = "Blockchain Security",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            if (currentGiayPhep.trang_thai_blockchain == "DaDongBo") {
                // Verified State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF16A34A).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF16A34A).copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF16A34A).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A)
                                )
                            }
                            Column {
                                Text(
                                    text = "Status",
                                    fontSize = 12.sp,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = "Verified on Blockchain",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        if (currentGiayPhep.transaction_hash != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Transaction Hash",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                    Text(
                                        text = currentGiayPhep.transaction_hash,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF333333),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { /* Verify hash logic */ },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000).copy(alpha = 0.05f),
                                contentColor = Color(0xFF333333)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Verify Hash", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Not Verified State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6B7280).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF6B7280).copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF6B7280).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280)
                                )
                            }
                            Column {
                                Text(
                                    text = "Status",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "Not on Blockchain",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }

                        Text(
                            text = "This license has not been secured on the blockchain yet. Push it to the chain to create a verifiable and tamper-proof record.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )

                        Button(
                            onClick = { viewModel.pushToBlockchain(currentGiayPhep.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D83F2)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Push to Blockchain", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (currentGiayPhep.trang_thai_blockchain == "DangDongBo") {
                            Button(
                                onClick = { showVerifyDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Confirm Blockchain Sync", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
