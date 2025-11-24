package com.example.kmaerm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLicenseScreen(
    giayPhep: GiayPhep,
    doanhNghiepId: String,
    onNavigateBack: () -> Unit,
    viewModel: GiayPhepViewModel = viewModel()
) {
    val context = LocalContext.current

    var licenseType by remember { mutableStateOf(giayPhep.loai_giay_phep) }
    var licenseNumber by remember { mutableStateOf(giayPhep.so_giay_phep) }
    var effectiveDate by remember { mutableStateOf(giayPhep.ngay_hieu_luc ?: "") }
    var expiryDate by remember { mutableStateOf(giayPhep.ngay_het_han ?: "") }
    var selectedStatus by remember { mutableStateOf(giayPhep.trang_thai_giay_phep) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

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
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit License",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Ref ID (Read-only)
                Column {
                    Text(
                        text = "Profile Ref ID",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = giayPhep.ho_so.ma_ho_so,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F7F8),
                            disabledContainerColor = Color(0xFFF5F7F8),
                            disabledTextColor = Color(0xFF333333)
                        ),
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // License Type
                Column {
                    Text(
                        text = "License Type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var expandedType by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = licenseType,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = {
                                Icon(
                                    if (expandedType) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F7F8)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Giấy phép kinh doanh") },
                                onClick = {
                                    licenseType = "Giấy phép kinh doanh"
                                    expandedType = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Giấy phép xuất/nhập khẩu") },
                                onClick = {
                                    licenseType = "Giấy phép xuất/nhập khẩu"
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // License Number
                Column {
                    Text(
                        text = "License Number",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F7F8)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Dates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Effective Date
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Effective Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = formatDateForInput(effectiveDate),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF6B7280))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F7F8)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Expiry Date
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expiry Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = formatDateForInput(expiryDate),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF6B7280))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F7F8)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // License Status
                Column {
                    Text(
                        text = "License Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusOption(
                            text = "Active (HieuLuc)",
                            isSelected = selectedStatus == "HieuLuc",
                            onClick = { selectedStatus = "HieuLuc" },
                            color = Color(0xFF28A745),
                            modifier = Modifier.weight(1f)
                        )
                        StatusOption(
                            text = "Revoked (ThuHoi)",
                            isSelected = selectedStatus == "ThuHoi",
                            onClick = { selectedStatus = "ThuHoi" },
                            color = Color(0xFFDC3545),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusOption(
                            text = "Expiring Soon (SapHetHan)",
                            isSelected = selectedStatus == "SapHetHan",
                            onClick = { selectedStatus = "SapHetHan" },
                            color = Color(0xFFFFC107),
                            modifier = Modifier.weight(1f)
                        )
                        StatusOption(
                            text = "Expired (DaHetHan)",
                            isSelected = selectedStatus == "DaHetHan",
                            onClick = { selectedStatus = "DaHetHan" },
                            color = Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF333333), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val request = UpdateGiayPhepRequest(
                                loai_giay_phep = licenseType,
                                so_giay_phep = licenseNumber,
                                ngay_hieu_luc = effectiveDate,
                                ngay_het_han = expiryDate,
                                trang_thai_giay_phep = selectedStatus
                            )
                            viewModel.updateGiayPhep(giayPhep.id, request)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0056B2)
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
                            Text("Save License", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) color else Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

fun formatDateForInput(dateString: String): String {
    if (dateString.isBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

