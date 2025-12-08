package com.example.kmaerm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.ui.viewmodel.AccountViewModel
import com.example.kmaerm.ui.viewmodel.HoSoViewModel
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel
import kotlinx.coroutines.flow.first

@Composable
fun HomeScreen(
    viewModel: AccountViewModel = viewModel(),
    hoSoViewModel: HoSoViewModel = viewModel(),
    giayPhepViewModel: GiayPhepViewModel = viewModel(),
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToCompanyInfo: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }
    val doanhNghiep by viewModel.doanhNghiep.collectAsState()
    val hoSoList by hoSoViewModel.hoSoList.collectAsState()
    val giayPhepList by giayPhepViewModel.giayPhepList.collectAsState()

    var doanhNghiepId by remember { mutableStateOf<String?>(null) }

    // Load doanhNghiepId và dữ liệu
    LaunchedEffect(Unit) {
        doanhNghiepId = tokenDataStore.doanhNghiepId.first()
    }

    LaunchedEffect(doanhNghiepId) {
        doanhNghiepId?.let { id ->
            hoSoViewModel.loadHoSoList(id)
            giayPhepViewModel.loadGiayPhepList(id)
        }
    }

    // Tính toán thống kê từ dữ liệu thực
    val totalHoSo = hoSoList.size
    val moiTaoHoSo = hoSoList.count { it.trang_thai_ho_so == "MoiTao" }
    val pendingHoSo = hoSoList.count {
        it.trang_thai_ho_so == "DaTiepNhan" || it.trang_thai_ho_so == "DangXuLy"
    }
    val approvedHoSo = hoSoList.count { it.trang_thai_ho_so == "DaDuyet" }
    val rejectedHoSo = hoSoList.count { it.trang_thai_ho_so == "BiTraLai" }

    val totalGiayPhep = giayPhepList.size
    // Sắp hết hạn
    val soonExpireGiayPhep = giayPhepList.count { it.trang_thai_giay_phep == "SapHetHan" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hello, ${doanhNghiep?.ten_viet_tat ?: "Company"}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { /* Handle notification */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF333333)
                    )
                }
                // Notification badge
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 32.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }

        // Hero Card - Thống kê hồ sơ
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    Text(
                        text = "Tổng hồ sơ: $totalHoSo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đang xử lý: $pendingHoSo",
                        fontSize = 16.sp,
                        color = Color(0xFF0056B3),
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Folder",
                    modifier = Modifier
                        .size(60.dp)
                        .weight(1f),
                    tint = Color(0xFF0056B3).copy(alpha = 0.8f)
                )
            }
        }

        // Thống kê chi tiết
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mới tạo
            StatCard(
                count = moiTaoHoSo,
                label = "Mới tạo",
                color = Color(0xFF9E9E9E),
                modifier = Modifier.weight(1f)
            )

            // Đã duyệt
            StatCard(
                count = approvedHoSo,
                label = "Đã duyệt",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            // Chờ xử lý
            StatCard(
                count = pendingHoSo,
                label = "Chờ xử lý",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            // Bị trả lại
            StatCard(
                count = rejectedHoSo,
                label = "Trả lại",
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Grid
        Text(
            text = "Thao tác nhanh",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Xem hồ sơ
            ActionCard(
                icon = Icons.Default.FolderOpen,
                label = "Hồ sơ",
                backgroundColor = Color(0xFF0056B3),
                iconTint = Color.White,
                textColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )

            // Xem giấy phép
            ActionCard(
                icon = Icons.Default.VerifiedUser,
                label = "Giấy phép",
                backgroundColor = Color(0xFF4CAF50),
                iconTint = Color.White,
                textColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(2) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tài khoản
            ActionCard(
                icon = Icons.Default.AccountCircle,
                label = "Tài khoản",
                backgroundColor = Color(0xFFF5F5F5),
                iconTint = Color(0xFF333333),
                textColor = Color(0xFF333333),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(3) }
            )

            // Thông tin DN
            ActionCard(
                icon = Icons.Default.Business,
                label = "Thông tin DN",
                backgroundColor = Color(0xFFF5F5F5),
                iconTint = Color(0xFF333333),
                textColor = Color(0xFF333333),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCompanyInfo
            )
        }

        // Thống kê giấy phép
        Text(
            text = "Giấy phép",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tổng giấy phép",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "$totalGiayPhep",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF4CAF50).copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${giayPhepList.count { it.trang_thai_giay_phep == "HieuLuc" }}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Hiệu lực",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$soonExpireGiayPhep",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "Sắp hết hạn",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${giayPhepList.count { it.trang_thai_giay_phep == "DaHetHan" || it.trang_thai_giay_phep == "ThuHoi" }}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            text = "Hết hạn",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Hoạt động gần đây
        Text(
            text = "Hồ sơ gần đây",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
        )

        // Activity Items
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                if (hoSoList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có hồ sơ nào",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    hoSoList.take(4).forEachIndexed { index, hoSo ->
                        val (icon, iconColor) = when (hoSo.trang_thai_ho_so) {
                            "DaDuyet" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                            "BiTraLai" -> Icons.Default.Cancel to Color(0xFFF44336)
                            "DangXuLy" -> Icons.Default.PendingActions to Color(0xFFFF9800)
                            "DaTiepNhan" -> Icons.Default.AccessTime to Color(0xFF2196F3)
                            else -> Icons.Default.Description to Color(0xFF9E9E9E)
                        }

                        val statusText = when (hoSo.trang_thai_ho_so) {
                            "MoiTao" -> "Mới tạo"
                            "DaTiepNhan" -> "Đã tiếp nhận"
                            "DangXuLy" -> "Đang xử lý"
                            "DaDuyet" -> "Đã duyệt"
                            "BiTraLai" -> "Bị trả lại"
                            else -> hoSo.trang_thai_ho_so
                        }

                        ActivityItem(
                            icon = icon,
                            iconColor = iconColor,
                            title = hoSo.ma_ho_so,
                            subtitle = "Trạng thái: $statusText",
                            time = hoSo.ngay_dang_ky.take(10)
                        )

                        if (index < minOf(hoSoList.size - 1, 3)) {
                            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
    }
}

@Composable
fun StatCard(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = time,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}
