package com.example.kmaerm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.model.UpdateHoSoRequest
import com.example.kmaerm.ui.viewmodel.OfficerHoSoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessProfileScreen(
    hoSoId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: OfficerHoSoViewModel = viewModel()

    val hoSoDetail by viewModel.hoSoDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    var receiveDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var licenseReference by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("DangXuLy") }
    var selectedLicenseType by remember { mutableStateOf("Giấy phép kinh doanh") }
    var licenseValidFrom by remember { mutableStateOf("") }
    var licenseValidTo by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hoSoId) {
        viewModel.loadHoSoDetail(hoSoId)
    }

    LaunchedEffect(hoSoDetail) {
        hoSoDetail?.let { hoSo ->
            receiveDate = hoSo.ngay_tiep_nhan ?: ""
            returnDate = hoSo.ngay_hen_tra ?: ""
            licenseReference = hoSo.so_giay_phep_theo_ho_so ?: ""
            selectedStatus = hoSo.trang_thai_ho_so
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
            viewModel.clearUpdateSuccess()
            onNavigateBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Dialog xác nhận duyệt và tạo giấy phép
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Tạo giấy phép") },
            text = {
                Column {
                    Text("Hồ sơ sẽ được duyệt và tạo giấy phép mới với thông tin:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Loại: $selectedLicenseType", fontSize = 14.sp)
                    Text("• Số giấy phép: $licenseReference", fontSize = 14.sp)
                    Text("• Hiệu lực: ${formatDisplayDate(licenseValidFrom)}", fontSize = 14.sp)
                    Text("• Hết hạn: ${formatDisplayDate(licenseValidTo)}", fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveDialog = false
                        // Cập nhật hồ sơ trước
                        val updateRequest = UpdateHoSoRequest(
                            ngay_tiep_nhan = if (receiveDate.isNotBlank()) receiveDate else null,
                            ngay_hen_tra = if (returnDate.isNotBlank()) returnDate else null,
                            trang_thai_ho_so = "DaDuyet"
                        )
                        viewModel.updateHoSo(hoSoId, updateRequest)

                        // Tạo giấy phép
                        val createLicenseRequest = com.example.kmaerm.data.model.CreateGiayPhepRequest(
                            ho_so_id = hoSoId,
                            loai_giay_phep = selectedLicenseType,
                            so_giay_phep = licenseReference,
                            ngay_hieu_luc = licenseValidFrom,
                            ngay_het_han = licenseValidTo,
                            trang_thai_giay_phep = "HieuLuc"
                        )
                        viewModel.createGiayPhep(createLicenseRequest)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
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
                        "Process Profile",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (selectedStatus == "DaDuyet") {
                            // Kiểm tra thông tin giấy phép trước khi hiển thị dialog
                            if (licenseReference.isBlank() || licenseValidFrom.isBlank() || licenseValidTo.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Vui lòng điền đầy đủ thông tin giấy phép",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showApproveDialog = true
                            }
                        } else {
                            // Chỉ cập nhật hồ sơ
                            val request = UpdateHoSoRequest(
                                ngay_tiep_nhan = if (receiveDate.isNotBlank()) receiveDate else null,
                                ngay_hen_tra = if (returnDate.isNotBlank()) returnDate else null,
                                trang_thai_ho_so = selectedStatus
                            )
                            viewModel.updateHoSo(hoSoId, request)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0056b3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            if (selectedStatus == "DaDuyet") "Approve & Create License" else "Update Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Stepper
            ProcessStepper(
                currentStep = when (selectedStatus) {
                    "MoiTao", "DaGui" -> 0
                    "DangXuLy" -> 2
                    "BiTraLai" -> 1
                    "DaDuyet" -> 3
                    else -> 0
                },
                modifier = Modifier.padding(16.dp)
            )

            // Form Fields
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Receive Date (Read-only)
                Column {
                    Text(
                        text = "Receive Date",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = formatDisplayDate(receiveDate),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledTextColor = Color(0xFF333333),
                            disabledBorderColor = Color(0xFFE0E0E0)
                        ),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Return Date (Ngay Hen Tra)
                Column {
                    Text(
                        text = "Return Date (Ngay Hen Tra)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = formatDisplayDate(returnDate),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = Color(0xFF6B7280)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // License Reference
                Column {
                    Text(
                        text = "License Reference *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = licenseReference,
                        onValueChange = { licenseReference = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., GP-123/2025") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Update Status
                Column {
                    Text(
                        text = "Update Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = getStatusDisplayText(selectedStatus),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Return (BiTraLai)") },
                                onClick = {
                                    selectedStatus = "BiTraLai"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Processing (DangXuLy)") },
                                onClick = {
                                    selectedStatus = "DangXuLy"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Approve (DaDuyet)") },
                                onClick = {
                                    selectedStatus = "DaDuyet"
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Hiển thị form thêm khi chọn "Approve"
                if (selectedStatus == "DaDuyet") {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    Text(
                        text = "Thông tin giấy phép",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    // Loại giấy phép
                    Column {
                        Text(
                            text = "Loại giấy phép *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        var expandedLicense by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedLicense,
                            onExpandedChange = { expandedLicense = !expandedLicense }
                        ) {
                            OutlinedTextField(
                                value = selectedLicenseType,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (expandedLicense) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedLicense,
                                onDismissRequest = { expandedLicense = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Giấy phép kinh doanh") },
                                    onClick = {
                                        selectedLicenseType = "Giấy phép kinh doanh"
                                        expandedLicense = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Giấy phép xuất/nhập khẩu") },
                                    onClick = {
                                        selectedLicenseType = "Giấy phép xuất/nhập khẩu"
                                        expandedLicense = false
                                    }
                                )
                            }
                        }
                    }

                    // Ngày hiệu lực
                    Column {
                        Text(
                            text = "Ngày hiệu lực *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = licenseValidFrom,
                            onValueChange = { licenseValidFrom = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd'T'HH:mm:ss'Z' (e.g., 2025-11-01T00:00:00Z)") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    // Set ngày hiện tại
                                    licenseValidFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                                        timeZone = TimeZone.getTimeZone("UTC")
                                    }.format(Date())
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Today",
                                        tint = Color(0xFF6B7280)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Ngày hết hạn
                    Column {
                        Text(
                            text = "Ngày hết hạn *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = licenseValidTo,
                            onValueChange = { licenseValidTo = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("yyyy-MM-dd'T'HH:mm:ss'Z' (e.g., 2030-11-01T00:00:00Z)") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    // Set 5 năm sau
                                    val calendar = Calendar.getInstance()
                                    calendar.add(Calendar.YEAR, 5)
                                    licenseValidTo = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                                        timeZone = TimeZone.getTimeZone("UTC")
                                    }.format(calendar.time)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "+5 years",
                                        tint = Color(0xFF6B7280)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Click icon lịch để tự động điền ngày hiện tại (hiệu lực) và +5 năm (hết hạn)",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProcessStepper(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        StepData("Registered", Icons.Default.Check, 0),
        StepData("Received", Icons.Default.Check, 1),
        StepData("Processing", Icons.Default.HourglassTop, 2),
        StepData("Approved", Icons.Default.Lock, 3)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Step indicator with connector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left connector
                    if (index > 0) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            thickness = 2.dp,
                            color = if (currentStep > index) Color(0xFF0056b3) else Color(0xFFE0E0E0)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Step circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentStep >= index) Color(0xFF0056b3) else Color(0xFFF1F5F9)
                            )
                            .then(
                                if (currentStep == index) {
                                    Modifier.border(4.dp, Color(0xFFBFDBFE), CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.label,
                            modifier = Modifier.size(18.dp),
                            tint = if (currentStep >= index) Color.White else Color(0xFF94A3B8)
                        )
                    }

                    // Right connector
                    if (index < steps.size - 1) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            thickness = 2.dp,
                            color = if (currentStep > index) Color(0xFF0056b3) else Color(0xFFE0E0E0)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Step label
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.label,
                    fontSize = 11.sp,
                    fontWeight = if (currentStep == index) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (currentStep == index) Color(0xFF0056b3) else if (currentStep > index) Color(0xFF333333) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

data class StepData(
    val label: String,
    val icon: ImageVector,
    val step: Int
)

fun formatDisplayDate(dateString: String): String {
    if (dateString.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        try {
            // Try simpler format
            dateString.substring(0, 16).replace("T", " ")
        } catch (e: Exception) {
            dateString
        }
    }
}

fun getStatusDisplayText(status: String): String {
    return when (status) {
        "BiTraLai" -> "Return (BiTraLai)"
        "DangXuLy" -> "Processing (DangXuLy)"
        "DaDuyet" -> "Approve (DaDuyet)"
        else -> status
    }
}
