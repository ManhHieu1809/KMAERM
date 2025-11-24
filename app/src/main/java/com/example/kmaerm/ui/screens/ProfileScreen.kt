package com.example.kmaerm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.data.model.HoSo
import com.example.kmaerm.data.model.ThuTuc
import com.example.kmaerm.ui.viewmodel.HoSoViewModel
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: HoSoViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }
    var searchQuery by remember { mutableStateOf("") }
    var doanhNghiepId by remember { mutableStateOf<String?>(null) }

    val hoSoList by viewModel.hoSoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val thuTucList by viewModel.thuTucList.collectAsState()

    // Load doanhNghiepId từ DataStore
    LaunchedEffect(Unit) {
        doanhNghiepId = tokenDataStore.doanhNghiepId.first()
    }

    // Load data khi có doanhNghiepId
    LaunchedEffect(doanhNghiepId) {
        doanhNghiepId?.let { id ->
            try {
                viewModel.loadHoSoList(id)
            } catch (_: Exception) {
                // Xử lý lỗi nếu có
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = Color(0xFF0056B3),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Profile",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "My Profiles",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar and Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Search profiles...",
                                color = Color(0xFF6B7280)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF6B7280)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0056B3),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedTextColor = Color(0xFF333333),
                            unfocusedTextColor = Color(0xFF333333)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Filter Button
                    OutlinedButton(
                        onClick = { /* TODO: Open filter */ },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF333333)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Content
            if (isLoading && hoSoList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0056B3))
                }
            } else if (hoSoList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có hồ sơ nào",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else {
                // Profiles List
                val filteredList = if (searchQuery.isBlank()) {
                    hoSoList
                } else {
                    hoSoList.filter {
                        it.loai_thu_tuc.contains(searchQuery, ignoreCase = true) ||
                        it.ma_ho_so.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { hoSo ->
                        HoSoCard(
                            hoSo = hoSo,
                            onClick = { onNavigateToDetail(hoSo.id) }
                        )
                    }

                    // Add spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Create Dialog
        if (showCreateDialog && doanhNghiepId != null) {
            CreateHoSoDialog(
                thuTucList = thuTucList,
                onDismiss = { viewModel.hideCreateDialog() },
                onCreate = { loaiThuTuc ->
                    viewModel.createHoSo(doanhNghiepId!!, loaiThuTuc)
                }
            )
        }
    }
}

@Composable
fun HoSoCard(
    hoSo: HoSo,
    onClick: () -> Unit
) {
    // Map trạng thái tiếng Việt
    val statusColor = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> Pair(Color(0xFF166534), Color(0xFFDCFCE7))
        "ChoDuyet" -> Pair(Color(0xFFC2410C), Color(0xFFFFEDD5))
        "TuChoi" -> Pair(Color(0xFF991B1B), Color(0xFFFEE2E2))
        "MoiTao" -> Pair(Color(0xFF1E40AF), Color(0xFFDBEAFE))
        else -> Pair(Color(0xFF6B7280), Color(0xFFF3F4F6))
    }

    val statusText = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> "Đã duyệt"
        "ChoDuyet" -> "Chờ duyệt"
        "TuChoi" -> "Từ chối"
        "MoiTao" -> "Mới tạo"
        else -> hoSo.trang_thai_ho_so
    }

    // Format ngày đăng ký
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(hoSo.ngay_dang_ky)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        hoSo.ngay_dang_ky
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = hoSo.loai_thu_tuc,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusColor.second
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor.first
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHoSoDialog(
    thuTucList: List<ThuTuc>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var selectedThuTuc by remember { mutableStateOf<ThuTuc?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tạo Hồ Sơ Mới", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dropdown chọn loại thủ tục
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedThuTuc?.ten_thu_tuc ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại thủ tục") },
                        placeholder = { Text("Chọn loại thủ tục") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0056B3),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        thuTucList.forEach { thuTuc ->
                            DropdownMenuItem(
                                text = { Text(thuTuc.ten_thu_tuc) },
                                onClick = {
                                    selectedThuTuc = thuTuc
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedThuTuc != null) {
                    Text(
                        text = "Tài liệu cần chuẩn bị:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    selectedThuTuc?.tai_lieus?.forEach { taiLieu ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = Color(0xFF0056B3))
                            Column {
                                Text(
                                    text = taiLieu.ten,
                                    fontSize = 13.sp,
                                    color = Color(0xFF333333)
                                )
                                if (taiLieu.mo_ta != null) {
                                    Text(
                                        text = taiLieu.mo_ta,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedThuTuc?.let {
                        onCreate(it.ten_thu_tuc)
                    }
                },
                enabled = selectedThuTuc != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0056B3)
                )
            ) {
                Text("Tạo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF6B7280))
            }
        }
    )
}
