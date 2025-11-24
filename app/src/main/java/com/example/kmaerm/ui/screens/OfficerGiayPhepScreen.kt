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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.GiayPhep
import com.example.kmaerm.ui.viewmodel.OfficerGiayPhepViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerGiayPhepScreen(
    viewModel: OfficerGiayPhepViewModel = viewModel()
) {
    val context = LocalContext.current
    val giayPhepList by viewModel.giayPhepList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val total by viewModel.total.collectAsState()

    // Filter states
    val searchMaHoSo by viewModel.searchMaHoSo.collectAsState()
    val searchSoGiayPhep by viewModel.searchSoGiayPhep.collectAsState()
    val searchLoaiGiayPhep by viewModel.searchLoaiGiayPhep.collectAsState()
    val searchNgayHetHanFrom by viewModel.searchNgayHetHanFrom.collectAsState()
    val searchNgayHetHanTo by viewModel.searchNgayHetHanTo.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGiayPhep by remember { mutableStateOf<GiayPhep?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Hiển thị màn hình sửa giấy phép hoặc danh sách
    if (selectedGiayPhep != null) {
        OfficerGiayPhepDetailScreen(
            giayPhep = selectedGiayPhep!!,
            onNavigateBack = {
                selectedGiayPhep = null
                // Reload danh sách sau khi cập nhật
                viewModel.loadAllGiayPhep()
            }
        )
    } else {
        OfficerGiayPhepListView(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            giayPhepList = giayPhepList,
            isLoading = isLoading,
            currentPage = currentPage,
            totalPages = totalPages,
            total = total,
            onGiayPhepClick = { giayPhep -> selectedGiayPhep = giayPhep },
            onFilterClick = { showFilterDialog = true },
            onPreviousPage = { viewModel.loadPreviousPage() },
            onNextPage = { viewModel.loadNextPage() }
        )

        // Filter Dialog
        if (showFilterDialog) {
            OfficerGiayPhepFilterDialog(
                maHoSo = searchMaHoSo,
                soGiayPhep = searchSoGiayPhep,
                loaiGiayPhep = searchLoaiGiayPhep,
                ngayHetHanFrom = searchNgayHetHanFrom,
                ngayHetHanTo = searchNgayHetHanTo,
                onMaHoSoChange = { viewModel.updateSearchMaHoSo(it) },
                onSoGiayPhepChange = { viewModel.updateSearchSoGiayPhep(it) },
                onLoaiGiayPhepChange = { viewModel.updateSearchLoaiGiayPhep(it) },
                onNgayHetHanFromChange = { viewModel.updateSearchNgayHetHanFrom(it) },
                onNgayHetHanToChange = { viewModel.updateSearchNgayHetHanTo(it) },
                onDismiss = { showFilterDialog = false },
                onApply = {
                    viewModel.applyFilters()
                    showFilterDialog = false
                },
                onClear = {
                    viewModel.clearFilters()
                    showFilterDialog = false
                }
            )
        }
    }
}

@Composable
fun OfficerGiayPhepListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    giayPhepList: List<GiayPhep>,
    isLoading: Boolean,
    currentPage: Int,
    totalPages: Int,
    total: Int,
    onGiayPhepClick: (GiayPhep) -> Unit,
    onFilterClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "License Management",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "$total licenses",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0056B3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search licenses...",
                            color = Color(0xFF9E9E9E)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9E9E9E)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        } else {
                            IconButton(onClick = onFilterClick) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = Color(0xFF333333)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Content
        if (isLoading && giayPhepList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4A90E2))
            }
        } else if (giayPhepList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
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
            // Filter giấy phép theo search query
            val filteredList = remember(giayPhepList, searchQuery) {
                if (searchQuery.isBlank()) {
                    giayPhepList
                } else {
                    giayPhepList.filter { giayPhep ->
                        giayPhep.so_giay_phep.contains(searchQuery, ignoreCase = true) ||
                        giayPhep.loai_giay_phep.contains(searchQuery, ignoreCase = true) ||
                        (giayPhep.ho_so?.ma_ho_so?.contains(searchQuery, ignoreCase = true) == true)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { giayPhep ->
                    OfficerGiayPhepCard(giayPhep = giayPhep, onClick = { onGiayPhepClick(giayPhep) })
                }

                // Pagination
                item {
                    if (totalPages > 1 && searchQuery.isBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onPreviousPage() },
                                enabled = currentPage > 1 && !isLoading
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Previous",
                                    tint = if (currentPage > 1) Color(0xFF4A90E2) else Color(0xFFBDBDBD)
                                )
                            }

                            Text(
                                text = "Page $currentPage / $totalPages",
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { onNextPage() },
                                enabled = currentPage < totalPages && !isLoading
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = if (currentPage < totalPages) Color(0xFF4A90E2) else Color(0xFFBDBDBD)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun OfficerGiayPhepCard(giayPhep: GiayPhep, onClick: () -> Unit) {
    val statusText = when (giayPhep.trang_thai_giay_phep) {
        "HieuLuc" -> "Hiệu lực"
        "SapHetHan" -> "Sắp hết hạn"
        "DaHetHan" -> "Đã hết hạn"
        "ThuHoi" -> "Thu hồi"
        else -> giayPhep.trang_thai_giay_phep
    }

    val statusColor = when (giayPhep.trang_thai_giay_phep) {
        "HieuLuc" -> Pair(Color(0xFF4ADE80), Color(0xFFDCFCE7))
        "SapHetHan" -> Pair(Color(0xFFFBBF24), Color(0xFFFFF7E0))
        "DaHetHan" -> Pair(Color(0xFFEF4444), Color(0xFFFEE2E2))
        "ThuHoi" -> Pair(Color(0xFF991B1B), Color(0xFFFEE2E2))
        else -> Pair(Color(0xFF6B7280), Color(0xFFF3F4F6))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = giayPhep.loai_giay_phep,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = giayPhep.so_giay_phep,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.second
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor.first, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor.first
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Hồ sơ liên quan
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF0056B3)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Hồ sơ liên quan",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = giayPhep.ho_so?.ma_ho_so ?: "N/A",
                        fontSize = 13.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Doanh nghiệp
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF0056B3)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Doanh nghiệp",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = giayPhep.ho_so?.ten_doanh_nghiep_vi ?: "N/A",
                        fontSize = 13.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ngày hiệu lực và hết hạn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ngày hiệu lực",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4ADE80)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(giayPhep.ngay_hieu_luc),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ngày hết hạn",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(giayPhep.ngay_het_han),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerGiayPhepFilterDialog(
    maHoSo: String,
    soGiayPhep: String,
    loaiGiayPhep: String,
    ngayHetHanFrom: String,
    ngayHetHanTo: String,
    onMaHoSoChange: (String) -> Unit,
    onSoGiayPhepChange: (String) -> Unit,
    onLoaiGiayPhepChange: (String) -> Unit,
    onNgayHetHanFromChange: (String) -> Unit,
    onNgayHetHanToChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    var showDatePickerFrom by remember { mutableStateOf(false) }
    var showDatePickerTo by remember { mutableStateOf(false) }
    var expandedLoaiGiayPhep by remember { mutableStateOf(false) }

    fun formatDateFromMillis(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Lọc giấy phép", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mã hồ sơ
                OutlinedTextField(
                    value = maHoSo,
                    onValueChange = onMaHoSoChange,
                    label = { Text("Mã hồ sơ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Số giấy phép
                OutlinedTextField(
                    value = soGiayPhep,
                    onValueChange = onSoGiayPhepChange,
                    label = { Text("Số giấy phép") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Loại giấy phép - Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedLoaiGiayPhep,
                    onExpandedChange = { expandedLoaiGiayPhep = it }
                ) {
                    OutlinedTextField(
                        value = loaiGiayPhep.ifBlank { "Tất cả" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại giấy phép") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLoaiGiayPhep)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0056B3),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLoaiGiayPhep,
                        onDismissRequest = { expandedLoaiGiayPhep = false }
                    ) {
                        listOf(
                            "",
                            "Giấy phép kinh doanh",
                            "Giấy phép xuất/nhập khẩu"
                        ).forEach { loai ->
                            DropdownMenuItem(
                                text = { Text(if (loai.isBlank()) "Tất cả" else loai) },
                                onClick = {
                                    onLoaiGiayPhepChange(loai)
                                    expandedLoaiGiayPhep = false
                                }
                            )
                        }
                    }
                }

                // Ngày hết hạn từ
                OutlinedTextField(
                    value = ngayHetHanFrom,
                    onValueChange = {},
                    label = { Text("Ngày hết hạn từ") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerFrom = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Ngày hết hạn đến
                OutlinedTextField(
                    value = ngayHetHanTo,
                    onValueChange = {},
                    label = { Text("Ngày hết hạn đến") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerTo = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onClear) {
                    Text("Xóa lọc", color = Color(0xFF6B7280))
                }
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0056B3)
                    )
                ) {
                    Text("Áp dụng")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF6B7280))
            }
        }
    )

    // DatePicker for From Date
    if (showDatePickerFrom) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerFrom = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onNgayHetHanFromChange(formatDateFromMillis(it))
                        }
                        showDatePickerFrom = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFrom = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DatePicker for To Date
    if (showDatePickerTo) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerTo = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onNgayHetHanToChange(formatDateFromMillis(it))
                        }
                        showDatePickerTo = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerTo = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
