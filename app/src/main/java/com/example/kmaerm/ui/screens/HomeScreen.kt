package com.example.kmaerm.ui.screens

import androidx.compose.foundation.background
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
import kotlinx.coroutines.flow.first

@Composable
fun HomeScreen(
    viewModel: AccountViewModel = viewModel()
) {
    val doanhNghiep by viewModel.doanhNghiep.collectAsState()

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

        // Hero Card
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
                        text = "Total Profiles: 5",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pending: 2",
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

        // Action Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Create New Profile - Primary Blue
            ActionCard(
                icon = Icons.Default.PersonAdd,
                label = "Create New Profile",
                backgroundColor = Color(0xFF0056B3),
                iconTint = Color.White,
                textColor = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Search Profiles
            ActionCard(
                icon = Icons.Default.Search,
                label = "Search Profiles",
                backgroundColor = Color(0xFFF5F5F5),
                iconTint = Color(0xFF333333),
                textColor = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Manage Users
            ActionCard(
                icon = Icons.Default.Group,
                label = "Manage Users",
                backgroundColor = Color(0xFFF5F5F5),
                iconTint = Color(0xFF333333),
                textColor = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )

            // View Reports
            ActionCard(
                icon = Icons.Default.BarChart,
                label = "View Reports",
                backgroundColor = Color(0xFFF5F5F5),
                iconTint = Color(0xFF333333),
                textColor = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Activity Section
        Text(
            text = "Recent Activity",
            fontSize = 20.sp,
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
                ActivityItem(
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    title = "Profile for 'ABC Corp' approved",
                    subtitle = "Status: Approved",
                    time = "2h ago"
                )

                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

                ActivityItem(
                    icon = Icons.Default.PersonAdd,
                    iconColor = Color(0xFF2196F3),
                    title = "New user 'John Doe' was added",
                    subtitle = "Action: User added",
                    time = "1d ago"
                )

                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

                ActivityItem(
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFF9C27B0),
                    title = "Report generated successfully",
                    subtitle = "Type: Monthly Financials",
                    time = "3d ago"
                )

                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)

                ActivityItem(
                    icon = Icons.Default.PendingActions,
                    iconColor = Color(0xFFFF9800),
                    title = "Profile for 'XYZ Solutions' pending",
                    subtitle = "Status: Awaiting review",
                    time = "5d ago"
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
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
