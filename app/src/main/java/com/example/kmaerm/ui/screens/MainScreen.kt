package com.example.kmaerm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    onNavigateToHoSoDetail: (String) -> Unit = {},
    onNavigateToCompanyInfo: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToParallelDemo: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Trang chủ",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Trang chủ",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0056B3),
                        selectedTextColor = Color(0xFF0056B3),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF0056B3).copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Hồ sơ",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Hồ sơ",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0056B3),
                        selectedTextColor = Color(0xFF0056B3),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF0056B3).copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Giấy phép",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Giấy phép",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0056B3),
                        selectedTextColor = Color(0xFF0056B3),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF0056B3).copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Tài khoản",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Tài khoản",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0056B3),
                        selectedTextColor = Color(0xFF0056B3),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF0056B3).copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigateToTab = { tabIndex -> selectedTab = tabIndex },
                    onNavigateToCompanyInfo = onNavigateToCompanyInfo
                )
                1 -> ProfileScreen(onNavigateToDetail = onNavigateToHoSoDetail)
                2 -> LicenseScreen()
                3 -> AccountScreen(
                    onLogout = onLogout,
                    onNavigateToCompanyInfo = onNavigateToCompanyInfo,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToParallelDemo = onNavigateToParallelDemo
                )
            }
        }
    }
}
