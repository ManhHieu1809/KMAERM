package com.example.kmaerm.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.ui.viewmodel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyInfoScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AccountViewModel = viewModel()
) {
    val doanhNghiep by viewModel.doanhNghiep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thông tin doanh nghiệp",
                        fontWeight = FontWeight.Bold
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0056B3))
            }
        } else if (doanhNghiep != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thông tin chung
                InfoSection(title = "Thông tin chung") {
                    InfoItem(
                        icon = Icons.Default.Business,
                        label = "Tên doanh nghiệp (VI)",
                        value = doanhNghiep!!.ten_doanh_nghiep_vi
                    )
                    InfoItem(
                        icon = Icons.Default.Language,
                        label = "Tên doanh nghiệp (EN)",
                        value = doanhNghiep!!.ten_doanh_nghiep_en
                    )
                    InfoItem(
                        icon = Icons.Default.Label,
                        label = "Tên viết tắt",
                        value = doanhNghiep!!.ten_viet_tat
                    )
                }

                // Thông tin đăng ký
                InfoSection(title = "Thông tin đăng ký") {
                    InfoItem(
                        icon = Icons.Default.Badge,
                        label = "Mã số doanh nghiệp",
                        value = doanhNghiep!!.ma_so_doanh_nghiep
                    )
                    InfoItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Ngày cấp MSDN lần đầu",
                        value = formatDate(doanhNghiep!!.ngay_cap_msdn_lan_dau)
                    )
                    InfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Nơi cấp MSDN",
                        value = doanhNghiep!!.noi_cap_msdn
                    )
                    InfoItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Vốn điều lệ",
                        value = doanhNghiep!!.von_dieu_le
                    )
                }

                // Thông tin liên hệ
                InfoSection(title = "Thông tin liên hệ") {
                    InfoItem(
                        icon = Icons.Default.LocationCity,
                        label = "Địa chỉ",
                        value = doanhNghiep!!.dia_chi
                    )
                    InfoItem(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = doanhNghiep!!.sdt
                    )
                    InfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = doanhNghiep!!.email
                    )
                    doanhNghiep!!.website?.let { website ->
                        InfoItem(
                            icon = Icons.Default.Language,
                            label = "Website",
                            value = website
                        )
                    }
                }

                // Người đại diện
                InfoSection(title = "Người đại diện pháp luật") {
                    InfoItem(
                        icon = Icons.Default.Person,
                        label = "Họ và tên",
                        value = doanhNghiep!!.nguoi_dai_dien
                    )
                    InfoItem(
                        icon = Icons.Default.Work,
                        label = "Chức vụ",
                        value = doanhNghiep!!.chuc_vu
                    )
                    InfoItem(
                        icon = Icons.Default.CreditCard,
                        label = "Loại định danh",
                        value = doanhNghiep!!.loai_dinh_danh
                    )
                    InfoItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Ngày cấp",
                        value = formatDate(doanhNghiep!!.ngay_cap_dinh_danh)
                    )
                    InfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Nơi cấp",
                        value = doanhNghiep!!.noi_cap_dinh_danh
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không tìm thấy thông tin doanh nghiệp",
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0056B3),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}

