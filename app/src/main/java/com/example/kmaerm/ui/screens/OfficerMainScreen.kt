package com.example.kmaerm.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.ui.viewmodel.OfficerDoanhNghiepViewModel
import kotlinx.coroutines.launch

@Composable
fun OfficerMainScreen(
    onLogout: () -> Unit,
    onNavigateToProcessProfile: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Trang chủ", "Hồ sơ", "Giấy phép", "Tài khoản")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Description,
        Icons.Default.VerifiedUser,
        Icons.Default.AccountCircle
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontSize = 12.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4A90E2),
                            selectedTextColor = Color(0xFF4A90E2),
                            indicatorColor = Color(0xFFE3F2FD),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> OfficerHomeTab(
                modifier = Modifier.padding(paddingValues),
                onNavigateToTab = { tabIndex -> selectedTab = tabIndex }
            )
            1 -> Box(modifier = Modifier.padding(paddingValues)) {
                com.example.kmaerm.ui.screens.OfficerHoSoScreen(
                    onNavigateToDetail = onNavigateToProcessProfile
                )
            }
            2 -> Box(modifier = Modifier.padding(paddingValues)) {
                OfficerGiayPhepScreen()
            }
            3 -> OfficerAccountTab(
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun OfficerHomeTab(
    modifier: Modifier = Modifier,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val tokenDataStore = remember { com.example.kmaerm.data.datastore.TokenDataStore(context) }
    val fullName by tokenDataStore.fullName.collectAsState(initial = "")
    val email by tokenDataStore.email.collectAsState(initial = "")

    val viewModel: OfficerDoanhNghiepViewModel = viewModel()
    val doanhNghiepList by viewModel.doanhNghiepList.collectAsState()

    // Tính toán thống kê từ dữ liệu thực
    val allHoSoList = remember(doanhNghiepList) {
        doanhNghiepList.flatMap { it.ho_sos }
    }

    val choXuLyCount = allHoSoList.count {
        it.trang_thai_ho_so == "MoiTao" || it.trang_thai_ho_so == "DaTiepNhan"
    }
    val dangXuLyCount = allHoSoList.count { it.trang_thai_ho_so == "DangXuLy" }
    val daDuyetCount = allHoSoList.count { it.trang_thai_ho_so == "DaDuyet" }
    val biTraLaiCount = allHoSoList.count { it.trang_thai_ho_so == "BiTraLai" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Xin chào,",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = if (!fullName.isNullOrEmpty()) fullName!! else "Cán bộ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFFE0E0E0))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF4A90E2)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!email.isNullOrEmpty()) email!! else "N/A",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Title
        Text(
            text = "Thống kê hồ sơ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Cards - Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Chờ xử lý",
                count = choXuLyCount.toString(),
                icon = Icons.Default.Schedule,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Đang xử lý",
                count = dangXuLyCount.toString(),
                icon = Icons.Default.Update,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics Cards - Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Đã duyệt",
                count = daDuyetCount.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Bị trả lại",
                count = biTraLaiCount.toString(),
                icon = Icons.Default.Cancel,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Thao tác nhanh",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                QuickActionItem(
                    icon = Icons.Default.Description,
                    title = "Xem danh sách hồ sơ",
                    description = "Quản lý tất cả hồ sơ",
                    iconColor = Color(0xFF4A90E2),
                    onClick = { onNavigateToTab(1) }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
                QuickActionItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Xem giấy phép",
                    description = "Danh sách giấy phép đã cấp",
                    iconColor = Color(0xFF4CAF50),
                    onClick = { onNavigateToTab(2) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun OfficerAccountTab(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenDataStore = remember { com.example.kmaerm.data.datastore.TokenDataStore(context) }

    // State để lưu thông tin user từ TokenDataStore
    val fullName by tokenDataStore.fullName.collectAsState(initial = "")
    val email by tokenDataStore.email.collectAsState(initial = "")
    val roleName by tokenDataStore.role.collectAsState(initial = "")
    val userId by tokenDataStore.userId.collectAsState(initial = "")

    var showChangePasswordScreen by remember { mutableStateOf(false) }
    var showProfileInfoScreen by remember { mutableStateOf(false) }

    // Hiển thị màn hình Change Password
    if (showChangePasswordScreen) {
        ChangePasswordScreen(
            onNavigateBack = { showChangePasswordScreen = false }
        )
        return
    }

    // Hiển thị màn hình Profile Info
    if (showProfileInfoScreen) {
        ProfileInfoScreen(
            onNavigateBack = { showProfileInfoScreen = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4A90E2).copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF4A90E2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!fullName.isNullOrEmpty()) fullName!! else "Cán bộ BCA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = when (roleName) {
                            "CanBo", "CAN_BO" -> "Cán bộ"
                            "DoanhNghiep", "DOANH_NGHIEP" -> "Doanh nghiệp"
                            else -> if (!roleName.isNullOrEmpty()) roleName!! else "User"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFFE0E0E0))

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (!email.isNullOrEmpty()) email!! else "N/A",
                        fontSize = 15.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Settings
        Text(
            text = "Cài đặt tài khoản",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Change Password
                AccountMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    description = "Thay đổi mật khẩu đăng nhập",
                    iconColor = Color(0xFF2196F3),
                    onClick = { showChangePasswordScreen = true }
                )

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Profile Info
                AccountMenuItem(
                    icon = Icons.Default.Person,
                    title = "Thông tin cá nhân",
                    description = "Xem và chỉnh sửa thông tin",
                    iconColor = Color(0xFF4CAF50),
                    onClick = { showProfileInfoScreen = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        Button(
            onClick = {
                scope.launch {
                    tokenDataStore.clearToken()
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC3545)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(12.dp))

    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
