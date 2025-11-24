package com.example.kmaerm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kmaerm.R
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel
import kotlinx.coroutines.flow.first

data class License(
    val id: Int,
    val type: String,
    val name: String,
    val licenseNumber: String,
    val expiryDate: String,
    val icon: ImageVector,
    val qrCodeResId: Int = R.drawable.logo_kmaerm
)

@Composable
fun LicenseScreen(
    viewModel: GiayPhepViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenDataStore = remember { TokenDataStore(context) }
    var doanhNghiepId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        doanhNghiepId = tokenDataStore.doanhNghiepId.first()
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        doanhNghiepId == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Không tìm thấy thông tin doanh nghiệp",
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
        else -> {
            GiayPhepScreen(
                doanhNghiepId = doanhNghiepId!!,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun LicenseCard(license: License) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0056B3),
                            Color(0xFF4A90E2),
                            Color(0xFFD4AF37)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with type and icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = license.type,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = license.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = license.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom section with info and QR code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // License details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "License No.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = license.licenseNumber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = "Expiry Date",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = license.expiryDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    // QR Code and Download button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // QR Code placeholder
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = license.qrCodeResId),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }

                        // Download button
                        TextButton(
                            onClick = { /* TODO: Download PDF */ },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Download PDF",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
