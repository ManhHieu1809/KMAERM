package com.example.kmaerm.ui.screens

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.R
import com.example.kmaerm.ui.viewmodel.ForgotPasswordViewModel

private val RedBackground = Color(0xFF970103)
private val GoldYellow = Color(0xFFD4A84B)
private val White = Color.White

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val canResendOTP by viewModel.canResendOTP.collectAsState()
    val attemptCount by viewModel.attemptCount.collectAsState()

    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Determine current step based on state
    val currentStep = when (state) {
        is ForgotPasswordViewModel.ForgotPasswordState.Idle,
        is ForgotPasswordViewModel.ForgotPasswordState.SendingOTP,
        is ForgotPasswordViewModel.ForgotPasswordState.Error -> 1
        is ForgotPasswordViewModel.ForgotPasswordState.OTPSent,
        is ForgotPasswordViewModel.ForgotPasswordState.VerifyingOTP -> 2
        is ForgotPasswordViewModel.ForgotPasswordState.OTPVerified,
        is ForgotPasswordViewModel.ForgotPasswordState.ResettingPassword -> 3
        is ForgotPasswordViewModel.ForgotPasswordState.Success -> 4
    }

    // FLAG_SECURE for OTP step (prevent screenshot)
    val activity = context as? Activity
    DisposableEffect(currentStep) {
        if (currentStep == 2) {
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Handle state changes
    LaunchedEffect(state) {
        when (val s = state) {
            is ForgotPasswordViewModel.ForgotPasswordState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
            }
            is ForgotPasswordViewModel.ForgotPasswordState.Success -> {
                Toast.makeText(context, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                onNavigateToLogin()
            }
            else -> {}
        }
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
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "KmaERM Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Quên Mật Khẩu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoldYellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step indicator
            StepIndicator(currentStep = currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            // Content based on step
            when (currentStep) {
                1 -> Step1EnterEmail(
                    email = email,
                    onEmailChange = { email = it },
                    isLoading = state is ForgotPasswordViewModel.ForgotPasswordState.SendingOTP,
                    onSendOTP = { viewModel.sendOTP(email) }
                )
                2 -> Step2VerifyOTP(
                    otp = otp,
                    onOTPChange = { otp = it },
                    remainingTime = remainingTime,
                    canResend = canResendOTP,
                    attemptCount = attemptCount,
                    isLoading = state is ForgotPasswordViewModel.ForgotPasswordState.VerifyingOTP,
                    onVerifyOTP = { viewModel.verifyOTP(otp) },
                    onResendOTP = { viewModel.sendOTP(email) }
                )
                3 -> Step3ResetPassword(
                    newPassword = newPassword,
                    onNewPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    isLoading = state is ForgotPasswordViewModel.ForgotPasswordState.ResettingPassword,
                    onResetPassword = { viewModel.resetPassword(newPassword, confirmPassword) }
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..3) {
            Surface(
                modifier = Modifier.size(if (step == currentStep) 12.dp else 8.dp),
                shape = CircleShape,
                color = if (step <= currentStep) GoldYellow else White.copy(alpha = 0.3f)
            ) {}
        }
    }
}

@Composable
fun Step1EnterEmail(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    onSendOTP: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nhập email của bạn để nhận mã OTP",
            fontSize = 14.sp,
            color = White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email", color = White) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = White
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSendOTP,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldYellow,
                contentColor = RedBackground
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && email.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = RedBackground
                )
            } else {
                Text(
                    text = "Gửi Mã OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun Step2VerifyOTP(
    otp: String,
    onOTPChange: (String) -> Unit,
    remainingTime: Int,
    canResend: Boolean,
    attemptCount: Int,
    isLoading: Boolean,
    onVerifyOTP: () -> Unit,
    onResendOTP: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nhập mã OTP đã được gửi đến email của bạn",
            fontSize = 14.sp,
            color = White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Countdown timer
        if (remainingTime > 0) {
            Text(
                text = "Mã hết hạn sau: ${formatTime(remainingTime)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldYellow
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOTPChange(it) },
            label = { Text("Mã OTP (6 chữ số)", color = White) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "OTP Icon",
                    tint = White
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        // Attempt count warning
        if (attemptCount > 0) {
            Text(
                text = "Còn ${ForgotPasswordViewModel.MAX_OTP_ATTEMPTS - attemptCount} lần thử",
                fontSize = 13.sp,
                color = Color(0xFFFF9800),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onVerifyOTP,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldYellow,
                contentColor = RedBackground
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && otp.length == 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = RedBackground
                )
            } else {
                Text(
                    text = "Xác Thực OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend button
        TextButton(
            onClick = onResendOTP,
            enabled = canResend && !isLoading
        ) {
            Text(
                text = if (canResend) "Gửi lại mã OTP" else "Gửi lại sau ${remainingTime}s",
                color = if (canResend) GoldYellow else White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun Step3ResetPassword(
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    onResetPassword: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nhập mật khẩu mới của bạn",
            fontSize = 14.sp,
            color = White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("Mật khẩu mới", color = White) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = White
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Xác nhận mật khẩu", color = White) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = White
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mật khẩu phải có ít nhất 6 ký tự",
            fontSize = 12.sp,
            color = White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onResetPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldYellow,
                contentColor = RedBackground
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = RedBackground
                )
            } else {
                Text(
                    text = "Đặt Lại Mật Khẩu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

