package com.example.kmaerm.ui.screens

import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.R
import com.example.kmaerm.ui.theme.White
import com.example.kmaerm.ui.viewmodel.LoginViewModel

// Màu sắc theo design
private val RedBackground = Color(0xFF970103)
private val GoldYellow = Color(0xFFD4A84B)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToHome: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                val role = (loginState as LoginViewModel.LoginState.Success).role
                onNavigateToHome(role)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Forgot Password
            Text(
                text = "Forgot Password?",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
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
