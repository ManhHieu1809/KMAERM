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
import androidx.compose.material.icons.outlined.Fingerprint
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.ui.viewmodel.AccountViewModel
import com.example.kmaerm.utils.BiometricHelper
import kotlinx.coroutines.launch

// Helper function to find FragmentActivity from Context
private fun findFragmentActivity(context: android.content.Context): FragmentActivity? {
    // Check if context itself is already a FragmentActivity
    if (context is FragmentActivity) {
        return context
    }

    // Unwrap ContextWrapper to find the Activity
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun AccountScreen(
    onLogout: () -> Unit = {},
    onNavigateToCompanyInfo: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToParallelDemo: () -> Unit = {},
    viewModel: AccountViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenDataStore = remember { TokenDataStore(context) }

    val doanhNghiep by viewModel.doanhNghiep.collectAsState()
    val biometricEnabled by tokenDataStore.biometricEnabled.collectAsState(initial = false)

    // Re-check biometric status every time the screen is shown
    var biometricStatus by remember { mutableStateOf(BiometricHelper.BiometricStatus.NOT_AVAILABLE) }
    var canUseBiometric by remember { mutableStateOf(false) }
    var showBiometricGuideDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        biometricStatus = BiometricHelper.canUseBiometric(context)
        canUseBiometric = biometricStatus == BiometricHelper.BiometricStatus.AVAILABLE

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Company Avatar and Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color(0xFF0056B3)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Company",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = doanhNghiep?.ten_doanh_nghiep_vi ?: "Đang tải...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = doanhNghiep?.ma_so_doanh_nghiep?.let { "MST: $it" } ?: "",
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                AccountMenuItem(
                    icon = Icons.Default.Apartment,
                    title = "Company Info",
                    onClick = onNavigateToCompanyInfo
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp
                )

                AccountMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = onNavigateToChangePassword
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp
                )

                // Biometric Toggle
                BiometricMenuItem(
                    enabled = biometricEnabled,
                    canUseBiometric = canUseBiometric,
                    biometricStatus = biometricStatus,
                    onShowGuide = { showBiometricGuideDialog = true },
                    onToggle = { enabled ->
                        if (enabled) {
                            val activity = findFragmentActivity(context)

                            if (activity != null) {
                                BiometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    title = "Kích hoạt Sinh trắc học",
                                    subtitle = "Xác thực để bật tính năng đăng nhập nhanh",
                                    onSuccess = {
                                        scope.launch {
                                            tokenDataStore.setBiometricEnabled(true)
                                            tokenDataStore.saveLastBiometricAuth(System.currentTimeMillis())
                                            Toast.makeText(
                                                context,
                                                "Đã bật xác thực sinh trắc học",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onError = { _, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    },
                                    onFailed = {
                                        Toast.makeText(
                                            context,
                                            "Xác thực thất bại",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                println("🔴 [onToggle] Cannot find FragmentActivity!")
                                Toast.makeText(
                                    context,
                                    "Không thể khởi động xác thực sinh trắc học",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            println("🔴 [onToggle] Disabling biometric...")
                            scope.launch {
                                tokenDataStore.setBiometricEnabled(false)
                                Toast.makeText(
                                    context,
                                    "Đã tắt xác thực sinh trắc học",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp
                )

                AccountMenuItem(
                    icon = Icons.Default.Speed,
                    title = "Demo Xử lý Song song",
                    onClick = onNavigateToParallelDemo
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp
                )

                AccountMenuItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Help Center",
                    onClick = { /* TODO: Navigate to help center */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC2626).copy(alpha = 0.1f),
                contentColor = Color(0xFFDC2626)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
    }

    // Biometric Setup Guide Dialog
    if (showBiometricGuideDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Cách bật Sinh trắc học",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bạn chưa đăng ký vân tay hoặc FaceID trên thiết bị này.",
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Để sử dụng tính năng đăng nhập nhanh:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1.", fontWeight = FontWeight.Bold)
                            Text("Vào Settings (Cài đặt) trên điện thoại")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("2.", fontWeight = FontWeight.Bold)
                            Text("Chọn Security (Bảo mật) hoặc Biometrics")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("3.", fontWeight = FontWeight.Bold)
                            Text("Đăng ký Vân tay hoặc Face Unlock")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("4.", fontWeight = FontWeight.Bold)
                            Text("Quay lại app này và bật tính năng")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Try to open device settings
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Không thể mở Settings. Vui lòng mở thủ công.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        showBiometricGuideDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    )
                ) {
                    Text("Mở Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricGuideDialog = false }) {
                    Text("Đóng")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BiometricMenuItem(
    enabled: Boolean,
    canUseBiometric: Boolean,
    biometricStatus: BiometricHelper.BiometricStatus,
    onShowGuide: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Show message when clicked and biometric is not available
                if (!canUseBiometric) {
                    if (biometricStatus == BiometricHelper.BiometricStatus.NONE_ENROLLED) {
                        onShowGuide()
                    } else {
                        val message = when (biometricStatus) {
                            BiometricHelper.BiometricStatus.NO_HARDWARE ->
                                "Thiết bị này không hỗ trợ sinh trắc học"
                            else -> "Sinh trắc học không khả dụng trên thiết bị này"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = null,
            tint = if (canUseBiometric) Color(0xFF6B7280) else Color(0xFFD1D5DB),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Xác thực Sinh trắc học",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (canUseBiometric) Color(0xFF333333) else Color(0xFF9CA3AF)
            )

            if (!canUseBiometric) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when (biometricStatus) {
                            BiometricHelper.BiometricStatus.NONE_ENROLLED ->
                                "Chưa đăng ký vân tay/FaceID - Nhấn để xem hướng dẫn"
                            BiometricHelper.BiometricStatus.NO_HARDWARE ->
                                "Thiết bị không hỗ trợ"
                            else -> "Không khả dụng"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B)
                    )
                }
            } else if (enabled) {
                Text(
                    text = "Đăng nhập nhanh bằng vân tay/FaceID",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981)
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = { newValue ->
                if (canUseBiometric) {
                    onToggle(newValue)
                } else {
                    Toast.makeText(
                        context,
                        BiometricHelper.getBiometricStatusMessage(context),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            enabled = canUseBiometric,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f)
            )
        )
    }
}


