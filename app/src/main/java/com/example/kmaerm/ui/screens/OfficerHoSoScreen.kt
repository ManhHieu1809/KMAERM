package com.example.kmaerm.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.HoSo
import com.example.kmaerm.ui.viewmodel.OfficerDoanhNghiepViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerHoSoScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: OfficerDoanhNghiepViewModel = viewModel()
) {
    val context = LocalContext.current
    val doanhNghiepList by viewModel.doanhNghiepList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val total by viewModel.total.collectAsState()

    // Filter states
    val searchTenVi by viewModel.searchTenVi.collectAsState()
    val searchTenEn by viewModel.searchTenEn.collectAsState()
    val searchMaSo by viewModel.searchMaSo.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHoSo by remember { mutableStateOf<HoSo?>(null) }

    // Tạo danh sách hồ sơ từ tất cả doanh nghiệp
    val allHoSoList = remember(doanhNghiepList) {
        doanhNghiepList.flatMap { doanhNghiep ->
            doanhNghiep.ho_sos.map { hoSo ->
                hoSo.copy(ten_doanh_nghiep_vi = doanhNghiep.ten_doanh_nghiep_vi)
            }
        }
    }

    // Filter hồ sơ theo search query
    val filteredHoSoList = remember(allHoSoList, searchQuery) {
        if (searchQuery.isBlank()) {
            allHoSoList
        } else {
            allHoSoList.filter { hoSo ->
                hoSo.ma_ho_so.contains(searchQuery, ignoreCase = true) ||
                hoSo.ten_doanh_nghiep_vi.contains(searchQuery, ignoreCase = true) ||
                hoSo.loai_thu_tuc.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Hiển thị chi tiết hồ sơ hoặc danh sách
    if (selectedHoSo != null) {
        OfficerHoSoDetailView(
            hoSo = selectedHoSo!!,
            onBack = {
                selectedHoSo = null
                // Reload danh sách sau khi cập nhật
                viewModel.loadDoanhNghiepList()
            }
        )
    } else {
        OfficerHoSoListView(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            filteredHoSoList = filteredHoSoList,
            isLoading = isLoading,
            currentPage = currentPage,
            totalPages = totalPages,
            onHoSoClick = { hoSo -> selectedHoSo = hoSo },
            onFilterClick = { showFilterDialog = true },
            onPreviousPage = { viewModel.loadPreviousPage() },
            onNextPage = { viewModel.loadNextPage() }
        )

        // Filter Dialog
        if (showFilterDialog) {
            FilterDialog(
                tenVi = searchTenVi,
                tenEn = searchTenEn,
                maSo = searchMaSo,
                onTenViChange = { viewModel.updateSearchTenVi(it) },
                onTenEnChange = { viewModel.updateSearchTenEn(it) },
                onMaSoChange = { viewModel.updateSearchMaSo(it) },
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
fun OfficerHoSoListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredHoSoList: List<HoSo>,
    isLoading: Boolean,
    currentPage: Int,
    totalPages: Int,
    onHoSoClick: (HoSo) -> Unit,
    onFilterClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header trắng với tiêu đề và search bar
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
                Text(
                    text = "My Profiles",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search profiles...",
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
        if (isLoading && filteredHoSoList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4A90E2))
            }
        } else if (filteredHoSoList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Chưa có hồ sơ nào" else "Không tìm thấy hồ sơ",
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHoSoList) { hoSo ->
                    OfficerHoSoCard(
                        hoSo = hoSo,
                        onClick = { onHoSoClick(hoSo) }
                    )
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
                                onClick = onPreviousPage,
                                enabled = currentPage > 1 && !isLoading
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Previous",
                                    tint = if (currentPage > 1) Color(0xFF4A90E2) else Color(0xFFBDBDBD)
                                )
                            }

                            Text(
                                text = "Trang $currentPage / $totalPages",
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = onNextPage,
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
fun OfficerHoSoCard(
    hoSo: HoSo,
    onClick: () -> Unit
) {
    val statusColor = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> Pair(Color(0xFF166534), Color(0xFFDCFCE7))
        "DangXuLy" -> Pair(Color(0xFFC2410C), Color(0xFFFFEDD5))
        "DaTiepNhan" -> Pair(Color(0xFF854D0E), Color(0xFFFFF7E0))
        "BiTraLai" -> Pair(Color(0xFF991B1B), Color(0xFFFEE2E2))
        "MoiTao" -> Pair(Color(0xFF1E40AF), Color(0xFFDBEAFE))
        else -> Pair(Color(0xFF6B7280), Color(0xFFF3F4F6))
    }

    val statusText = when (hoSo.trang_thai_ho_so) {
        "DaDuyet" -> "Đã duyệt"
        "DangXuLy" -> "Đang xử lý"
        "DaTiepNhan" -> "Đã tiếp nhận"
        "BiTraLai" -> "Bị trả lại"
        "MoiTao" -> "Mới tạo"
        else -> hoSo.trang_thai_ho_so
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (_: Exception) {
            dateString
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hoSo.loai_thu_tuc,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hoSo.ma_ho_so,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.second
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor.first
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

            // Doanh nghiep info
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
                Text(
                    text = hoSo.ten_doanh_nghiep_vi,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Medium
                )
            }

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ngày đăng ký: ${formatDate(hoSo.ngay_dang_ky)}",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    tenVi: String,
    tenEn: String,
    maSo: String,
    onTenViChange: (String) -> Unit,
    onTenEnChange: (String) -> Unit,
    onMaSoChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Lọc hồ sơ", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = tenVi,
                    onValueChange = onTenViChange,
                    label = { Text("Tên doanh nghiệp (VI)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tenEn,
                    onValueChange = onTenEnChange,
                    label = { Text("Tên doanh nghiệp (EN)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0056B3),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = maSo,
                    onValueChange = onMaSoChange,
                    label = { Text("Mã số doanh nghiệp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerHoSoDetailView(
    hoSo: HoSo,
    onBack: () -> Unit
) {
    // Sử dụng OfficerHoSoViewModel để load chi tiết đầy đủ
    val viewModel: com.example.kmaerm.ui.viewmodel.OfficerHoSoViewModel = viewModel()
    val hoSoDetail by viewModel.hoSoDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val context = LocalContext.current

    // Form states
    var ngayTiepNhan by remember { mutableStateOf("") }
    var ngayHenTra by remember { mutableStateOf("") }
    var trangThaiHoSo by remember { mutableStateOf("") }
    var expandedStatusDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Helper function to format current time
    fun getCurrentTimeISO(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // Helper function to format date from DatePicker
    fun formatDateFromMillis(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }

    // Load chi tiết hồ sơ khi màn hình được mở
    LaunchedEffect(hoSo.id) {
        viewModel.loadHoSoDetail(hoSo.id)
    }

    // Populate form when detail is loaded - CẬP NHẬT KHI hoSoDetail THAY ĐỔI
    LaunchedEffect(hoSoDetail?.trang_thai_ho_so) {
        hoSoDetail?.let { detail ->
            ngayTiepNhan = detail.ngay_tiep_nhan ?: ""
            ngayHenTra = detail.ngay_hen_tra ?: ""
            trangThaiHoSo = detail.trang_thai_ho_so ?: ""
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Tạo giấy phép thành công! Hồ sơ đã được chuyển sang trạng thái Đã Duyệt (Approved).", Toast.LENGTH_LONG).show()
            viewModel.clearUpdateSuccess()
            // Reload lại chi tiết hồ sơ sau khi cập nhật
            viewModel.loadHoSoDetail(hoSo.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Process Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4A90E2))
            }
        } else {
            hoSoDetail?.let { detail ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Progress indicator
                    ProfileProgressIndicator(status = detail.trang_thai_ho_so)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
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
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = detail.ma_ho_so ?: "N/A",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (detail.trang_thai_ho_so ?: "") {
                                        "MoiTao" -> Color(0xFFE3F2FD)
                                        "DaTiepNhan" -> Color(0xFFFFF3E0)
                                        "DangXuLy" -> Color(0xFFFFF9C4)
                                        "BiTraLai" -> Color(0xFFFFEBEE)
                                        "DaDuyet" -> Color(0xFFE8F5E9)
                                        else -> Color(0xFFF5F5F5)
                                    }
                                ) {
                                    Text(
                                        text = when (detail.trang_thai_ho_so ?: "") {
                                            "MoiTao" -> "Mới tạo"
                                            "DaTiepNhan" -> "Đã tiếp nhận"
                                            "DangXuLy" -> "Đang xử lý"
                                            "BiTraLai" -> "Bị trả lại"
                                            "DaDuyet" -> "Đã duyệt"
                                            else -> detail.trang_thai_ho_so ?: "Không xác định"
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when (detail.trang_thai_ho_so ?: "") {
                                            "MoiTao" -> Color(0xFF1976D2)
                                            "DaGui" -> Color(0xFFF57C00)
                                            "DangXuLy" -> Color(0xFFF57F17)
                                            "BiTraLai" -> Color(0xFFC62828)
                                            "DaDuyet" -> Color(0xFF2E7D32)
                                            else -> Color(0xFF757575)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Loại thủ tục:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = detail.loai_thu_tuc ?: "N/A",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Doanh nghiệp:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = detail.ten_doanh_nghiep_vi ?: "N/A",
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nếu trạng thái là DangXuLy (Processing) - Hiển thị form tạo giấy phép
                    if (detail.trang_thai_ho_so == "DangXuLy") {
                        // Form tạo giấy phép
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Create License (Tạo Giấy Phép)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Loại giấy phép - Dropdown
                                var selectedLoaiGiayPhep by remember { mutableStateOf("Giấy phép kinh doanh") }
                                var expandedLoaiGiayPhep by remember { mutableStateOf(false) }

                                Text(
                                    text = "Loại giấy phép *",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedLoaiGiayPhep,
                                    onExpandedChange = { expandedLoaiGiayPhep = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedLoaiGiayPhep,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLoaiGiayPhep)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF0056B3),
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedLoaiGiayPhep,
                                        onDismissRequest = { expandedLoaiGiayPhep = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        listOf(
                                            "Giấy phép kinh doanh",
                                            "Giấy phép xuất/nhập khẩu"
                                        ).forEach { loai ->
                                            DropdownMenuItem(
                                                text = { Text(loai, color = Color.Black) },
                                                onClick = {
                                                    selectedLoaiGiayPhep = loai
                                                    expandedLoaiGiayPhep = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Số giấy phép
                                var soGiayPhep by remember { mutableStateOf("") }
                                Text(
                                    text = "Số giấy phép *",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = soGiayPhep,
                                    onValueChange = { soGiayPhep = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Ví dụ: GP-123/2025/BCA") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0056B3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ngày hiệu lực
                                var ngayHieuLuc by remember { mutableStateOf("") }
                                var showDatePickerHieuLuc by remember { mutableStateOf(false) }

                                Text(
                                    text = "Ngày hiệu lực *",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = ngayHieuLuc,
                                    onValueChange = { ngayHieuLuc = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Click calendar to select") },
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePickerHieuLuc = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Select date",
                                                tint = Color(0xFF0056B3)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0056B3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    readOnly = false
                                )

                                if (showDatePickerHieuLuc) {
                                    DatePickerDialog(
                                        onDateSelected = { millis ->
                                            millis?.let {
                                                ngayHieuLuc = formatDateFromMillis(it)
                                            }
                                            showDatePickerHieuLuc = false
                                        },
                                        onDismiss = { showDatePickerHieuLuc = false }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Ngày hết hạn
                                var ngayHetHan by remember { mutableStateOf("") }
                                var showDatePickerHetHan by remember { mutableStateOf(false) }

                                Text(
                                    text = "Ngày hết hạn *",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = ngayHetHan,
                                    onValueChange = { ngayHetHan = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Click calendar to select") },
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePickerHetHan = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Select date",
                                                tint = Color(0xFF0056B3)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0056B3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    readOnly = false
                                )

                                if (showDatePickerHetHan) {
                                    DatePickerDialog(
                                        onDateSelected = { millis ->
                                            millis?.let {
                                                ngayHetHan = formatDateFromMillis(it)
                                            }
                                            showDatePickerHetHan = false
                                        },
                                        onDismiss = { showDatePickerHetHan = false }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Trạng thái giấy phép - Dropdown với 4 loại
                                var selectedTrangThaiGiayPhep by remember { mutableStateOf("HieuLuc") }
                                var expandedTrangThaiGiayPhep by remember { mutableStateOf(false) }

                                Text(
                                    text = "Trạng thái giấy phép *",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedTrangThaiGiayPhep,
                                    onExpandedChange = { expandedTrangThaiGiayPhep = it }
                                ) {
                                    OutlinedTextField(
                                        value = when (selectedTrangThaiGiayPhep) {
                                            "HieuLuc" -> "Hiệu lực"
                                            "SapHetHan" -> "Sắp hết hạn"
                                            "ThuHoi" -> "Thu hồi"
                                            "DaHetHan" -> "Đã hết hạn"
                                            else -> selectedTrangThaiGiayPhep
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTrangThaiGiayPhep)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF0056B3),
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedTrangThaiGiayPhep,
                                        onDismissRequest = { expandedTrangThaiGiayPhep = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        listOf(
                                            "HieuLuc" to "Hiệu lực",
                                            "SapHetHan" to "Sắp hết hạn",
                                            "ThuHoi" to "Thu hồi",
                                            "DaHetHan" to "Đã hết hạn"
                                        ).forEach { (value, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label, color = Color.Black) },
                                                onClick = {
                                                    selectedTrangThaiGiayPhep = value
                                                    expandedTrangThaiGiayPhep = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Create License Button
                                Button(
                                    onClick = {
                                        // Validation
                                        if (soGiayPhep.isBlank() || ngayHieuLuc.isBlank() || ngayHetHan.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Vui lòng điền đầy đủ thông tin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }

                                        // Prepare request
                                        val createLicenseRequest = com.example.kmaerm.data.model.CreateGiayPhepRequest(
                                            ho_so_id = detail.id,
                                            loai_giay_phep = selectedLoaiGiayPhep,
                                            so_giay_phep = soGiayPhep,
                                            ngay_hieu_luc = ngayHieuLuc,
                                            ngay_het_han = ngayHetHan,
                                            trang_thai_giay_phep = selectedTrangThaiGiayPhep
                                        )

                                        // Call API
                                        viewModel.createGiayPhep(createLicenseRequest)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF16A34A)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Create License",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Hiển thị Required Documents cho trạng thái khác
                        Text(
                            text = "Required Documents",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        detail.ho_so_tai_lieus?.forEach { hoSoTaiLieu ->
                            RequiredDocumentCardReadOnly(hoSoTaiLieu = hoSoTaiLieu)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (detail.ho_so_tai_lieus.isNullOrEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Chưa có tài liệu nào",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Update Form cho trạng thái khác DangXuLy
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Update Profile Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Receive Date - Auto fill current time on click
                                Text(
                                    text = "Receive Date",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = ngayTiepNhan,
                                    onValueChange = { ngayTiepNhan = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Click clock icon to set current time") },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            ngayTiepNhan = getCurrentTimeISO()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Set current time",
                                                tint = Color(0xFF0056B3)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0056B3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Return Date (Ngày Hẹn Trả) - DatePicker on calendar icon click
                                Text(
                                    text = "Return Date (Ngày Hẹn Trả)",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = ngayHenTra,
                                    onValueChange = { ngayHenTra = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Click calendar to select date") },
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Select date",
                                                tint = Color(0xFF0056B3)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0056B3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    readOnly = false
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Update Status Dropdown
                                Text(
                                    text = "Update Status",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedStatusDropdown,
                                    onExpandedChange = { expandedStatusDropdown = it }
                                ) {
                                    OutlinedTextField(
                                        value = when (trangThaiHoSo) {
                                            "MoiTao" -> "Mới tạo"
                                            "DaTiepNhan" -> "Đã tiếp nhận"
                                            "DangXuLy" -> "Đang xử lý"
                                            "BiTraLai" -> "Bị trả lại"
                                            "DaDuyet" -> "Đã duyệt"
                                            else -> trangThaiHoSo
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatusDropdown)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF0056B3),
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedTextColor = Color.Black,
                                            unfocusedTextColor = Color.Black,
                                            cursorColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedStatusDropdown,
                                        onDismissRequest = { expandedStatusDropdown = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        listOf(
                                            "MoiTao" to "Mới tạo",
                                            "DaTiepNhan" to "Đã tiếp nhận",
                                            "DangXuLy" to "Đang xử lý",
                                            "BiTraLai" to "Bị trả lại",
                                            "DaDuyet" to "Đã duyệt"
                                        ).forEach { (value, label) ->
                                            DropdownMenuItem(
                                                text = { Text(label, color = Color.Black) },
                                                onClick = {
                                                    trangThaiHoSo = value
                                                    expandedStatusDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Update Profile Button
                                Button(
                                    onClick = {
                                        // Validation
                                        if (trangThaiHoSo.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Vui lòng chọn trạng thái hồ sơ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }

                                        // Prepare request
                                        val updateRequest = com.example.kmaerm.data.model.UpdateHoSoRequest(
                                            ngay_dang_ky = detail.ngay_dang_ky,
                                            ngay_tiep_nhan = if (ngayTiepNhan.isNotBlank()) ngayTiepNhan else null,
                                            ngay_hen_tra = if (ngayHenTra.isNotBlank()) ngayHenTra else null,
                                            trang_thai_ho_so = trangThaiHoSo
                                        )

                                        // Call API
                                        try {
                                            viewModel.updateHoSo(detail.id, updateRequest)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Lỗi: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0056B3)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Update Profile",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // THÊM SPACER LỚN Ở CUỐI ĐỂ TRÁNH BỊ CHE BỞI BOTTOM NAV
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }

    // DatePicker Dialog for Return Date
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    ngayHenTra = formatDateFromMillis(it)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0056B3)
                )
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6B7280))
            }
        },
        text = {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF0056B3),
                    todayContentColor = Color(0xFF0056B3),
                    todayDateBorderColor = Color(0xFF0056B3)
                )
            )
        }
    )
}

@Composable
fun ProfileProgressIndicator(status: String?) {
    val steps = listOf("Registered", "Received", "Processing", "Approved")
    val currentStep = when (status ?: " ") {
        "MoiTao" -> 0
        "DaTiepNhan" -> 1
        "DangXuLy" -> 2
        "DaDuyet" -> 3
        else -> 0
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= currentStep) Color(0xFF4A90E2)
                                else Color(0xFFE0E0E0)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (index == currentStep) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Connecting line (except for last step)
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (index < currentStep) Color(0xFF4A90E2)
                                    else Color(0xFFE0E0E0)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                Text(
                    text = step,
                    fontSize = 11.sp,
                    color = if (index <= currentStep) Color(0xFF4A90E2) else Color(0xFF9E9E9E),
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RequiredDocumentCardReadOnly(hoSoTaiLieu: com.example.kmaerm.data.model.HoSoTaiLieu) {
    // Kiểm tra xem có tài liệu nào đã được upload chưa
    val hasDocuments = !hoSoTaiLieu.tai_lieus.isNullOrEmpty()

    // Màu sắc dựa trên việc đã upload hay chưa
    val cardBackgroundColor = if (hasDocuments) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val cardBorderColor = if (hasDocuments) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val iconColor = if (hasDocuments) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val iconImage = if (hasDocuments) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconImage,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hoSoTaiLieu.loai_tai_lieu.ten,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            if (!hoSoTaiLieu.loai_tai_lieu.mo_ta.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hoSoTaiLieu.loai_tai_lieu.mo_ta,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }

            // Danh sách tài liệu đã upload - CHỈ XEM
            hoSoTaiLieu.tai_lieus?.forEach { taiLieu ->
                Spacer(modifier = Modifier.height(12.dp))
                DocumentItemReadOnly(taiLieu = taiLieu)
            }

            if (hoSoTaiLieu.tai_lieus.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chưa có tài liệu nào được tải lên",
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DocumentItemReadOnly(taiLieu: com.example.kmaerm.data.model.TaiLieu) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Uploaded on ${formatUploadDate(taiLieu.created_at ?: "")}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Icon xem - CHỈ XEM, không có xóa
            IconButton(onClick = {
                try {
                    val fileUrl = taiLieu.duong_dan
                    if (fileUrl.isBlank()) {
                        Toast.makeText(context, "Không tìm thấy đường dẫn file", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }

                    val mimeType = when {
                        fileUrl.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                        fileUrl.endsWith(".jpg", ignoreCase = true) ||
                        fileUrl.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                        fileUrl.endsWith(".png", ignoreCase = true) -> "image/png"
                        fileUrl.endsWith(".gif", ignoreCase = true) -> "image/gif"
                        fileUrl.endsWith(".bmp", ignoreCase = true) -> "image/bmp"
                        fileUrl.endsWith(".webp", ignoreCase = true) -> "image/webp"
                        else -> "*/*"
                    }

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(fileUrl), mimeType)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                        context.startActivity(browserIntent)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Không thể mở tài liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "View document",
                    tint = Color(0xFF4A90E2)
                )
            }
        }
    }
}

fun formatUploadDate(dateString: String): String {
    return try {
        if (dateString.isBlank()) return ""
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        dateString
    }
}
