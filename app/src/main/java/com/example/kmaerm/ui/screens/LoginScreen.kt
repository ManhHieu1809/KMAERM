package com.example.kmaerm.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.R
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.ui.theme.White
import com.example.kmaerm.ui.viewmodel.LoginViewModel
import com.example.kmaerm.utils.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Màu sắc theo design
private val RedBackground = Color(0xFF970103)
private val GoldYellow = Color(0xFFD4A84B)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToHome: (String) -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenDataStore = remember { TokenDataStore(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var showEnableBiometricDialog by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        val biometricEnabled = tokenDataStore.biometricEnabled.first()
        val savedRole = tokenDataStore.role.first()
        val savedToken = tokenDataStore.token.first()
        val biometricStatus = BiometricHelper.canUseBiometric(context)

        println("🔐 [LoginScreen] biometricEnabled: $biometricEnabled")
        println("🔐 [LoginScreen] savedRole: $savedRole")
        println("🔐 [LoginScreen] savedToken: ${if (savedToken != null) "exists" else "null"}")
        println("🔐 [LoginScreen] biometricStatus: $biometricStatus")

    }

    // Handle login success - offer to enable biometric
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                val biometricEnabled = tokenDataStore.biometricEnabled.first()
                val biometricStatus = BiometricHelper.canUseBiometric(context)

                // If biometric is available but not enabled, ask user
                if (!biometricEnabled &&
                    biometricStatus == BiometricHelper.BiometricStatus.AVAILABLE &&
                    context is FragmentActivity) {
                    showEnableBiometricDialog = true
                } else {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    val role = (loginState as LoginViewModel.LoginState.Success).role
                    onNavigateToHome(role)
                }
            }
            is LoginViewModel.LoginState.Error -> {
                Toast.makeText(
                    context,
                    (loginState as LoginViewModel.LoginState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(showBiometricPrompt) {
        if (showBiometricPrompt && context is FragmentActivity) {
            val savedRole = tokenDataStore.role.first()
            val savedToken = tokenDataStore.token.first()
            val biometricEnabled = tokenDataStore.biometricEnabled.first()

            if (!biometricEnabled) {
                println("🔐 [LoginScreen] Biometric not enabled, skipping prompt")
                showBiometricPrompt = false
                Toast.makeText(context, "Vui lòng bật xác thực sinh trắc học trong Cài đặt tài khoản", Toast.LENGTH_LONG).show()
                return@LaunchedEffect
            }

            if (savedRole == null || savedToken == null) {
                println("🔐 [LoginScreen] No saved role/token, skipping biometric login")
                showBiometricPrompt = false
                Toast.makeText(context, "Vui lòng đăng nhập bằng email/mật khẩu trước", Toast.LENGTH_LONG).show()
                return@LaunchedEffect
            }

            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = "Đăng nhập nhanh",
                subtitle = "Sử dụng sinh trắc học để đăng nhập",
                onSuccess = {
                    scope.launch {
                        tokenDataStore.saveLastBiometricAuth(System.currentTimeMillis())
                        savedRole?.let { role ->
                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            onNavigateToHome(role)
                        }
                    }
                },
                onError = { code, message ->
                    showBiometricPrompt = false
                    // Don't show error toast for user cancel
                    if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                        code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                },
                onFailed = {
                    // Allow retry, don't close prompt
                }
            )
            showBiometricPrompt = false
        }
    }

    // Enable Biometric Dialog
    if (showEnableBiometricDialog) {
        AlertDialog(
            onDismissRequest = {
                showEnableBiometricDialog = false
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                val role = (loginState as LoginViewModel.LoginState.Success).role
                viewModel.resetLoginState()
                onNavigateToHome(role)
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = GoldYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bật đăng nhập sinh trắc học?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "Bạn có muốn sử dụng vân tay hoặc FaceID để đăng nhập nhanh hơn lần sau không?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.enableBiometric(true)
                            showEnableBiometricDialog = false
                            Toast.makeText(context, "Đã bật đăng nhập sinh trắc học!", Toast.LENGTH_SHORT).show()
                            val role = (loginState as LoginViewModel.LoginState.Success).role
                            viewModel.resetLoginState()
                            onNavigateToHome(role)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldYellow,
                        contentColor = RedBackground
                    )
                ) {
                    Text("Bật", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEnableBiometricDialog = false
                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        val role = (loginState as LoginViewModel.LoginState.Success).role
                        viewModel.resetLoginState()
                        onNavigateToHome(role)
                    }
                ) {
                    Text("Để sau", color = Color.Gray)
                }
            },
            containerColor = RedBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RedBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo tròn
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "KmaERM Logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title - ACADEMY OF
            Text(
                text = "ACADEMY OF",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = White,
                letterSpacing = 2.sp
            )

            // Title - CRYPTOGRAPHY TECHNIQUES
            Text(
                text = "CRYPTOGRAPHY TECHNIQUES",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // KMAERM
            Text(
                text = "KMAERM",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "Email Address",
                        color = White.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldYellow,
                    unfocusedBorderColor = GoldYellow,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = GoldYellow,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "Password",
                        color = White.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldYellow,
                    unfocusedBorderColor = GoldYellow,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = GoldYellow,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(email, password)
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldYellow,
                    disabledContainerColor = GoldYellow.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = RedBackground
                    )
                } else {
                    Text(
                        text = "LOGIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedBackground,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Login Button - chỉ hiển thị khi đã bật trong settings
            if (context is FragmentActivity) {
                val biometricStatus = remember { BiometricHelper.canUseBiometric(context) }
                val biometricEnabled by tokenDataStore.biometricEnabled.collectAsState(initial = false)
                val savedRole by tokenDataStore.role.collectAsState(initial = null)
                val savedToken by tokenDataStore.token.collectAsState(initial = null)


                if (biometricEnabled &&
                    biometricStatus == BiometricHelper.BiometricStatus.AVAILABLE &&
                    savedRole != null &&
                    savedToken != null) {
                    OutlinedButton(
                        onClick = {
                            showBiometricPrompt = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = GoldYellow
                        ),
                        border = BorderStroke(2.dp, GoldYellow),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            tint = GoldYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ĐĂNG NHẬP BẰNG SINH TRẮC HỌC",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Divider with "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = White.copy(alpha = 0.3f)
                )
                Text(
                    text = "  HOẶC  ",
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.5f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = White.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password
            Text(
                text = "Quên mật khẩu?",
                fontSize = 14.sp,
                color = GoldYellow,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onNavigateToForgotPassword()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up
            Text(
                text = "Don't have an account? Sign Up",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

