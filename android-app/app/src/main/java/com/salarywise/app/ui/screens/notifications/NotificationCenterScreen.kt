package com.salarywise.app.ui.screens.notifications

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salarywise.app.ui.theme.AlertRose
import com.salarywise.app.ui.theme.PrimaryLight
import com.salarywise.app.ui.theme.SuccessGreen
import com.salarywise.app.ui.theme.WarningAmber

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String // SALARY, BILL, BUDGET_WARN, BUDGET_EXCEED, SAVINGS, REPORT
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit
) {
    val sampleNotifications = remember {
        listOf(
            AppNotification("1", "Salary Received! 🎉", "Your monthly salary of ₹40,000 has been credited to your account.", "Today, 09:30 AM", "SALARY"),
            AppNotification("2", "Upcoming Bill: Electricity", "Electricity bill of ₹1,850 is due in 3 days.", "Yesterday, 06:15 PM", "BILL"),
            AppNotification("3", "Budget Warning: Shopping", "You have utilized 84% of your monthly shopping budget.", "2 days ago", "BUDGET_WARN"),
            AppNotification("4", "Savings Reminder", "Don't forget to transfer your monthly ₹5,000 deposit to your Emergency Fund!", "3 days ago", "SAVINGS"),
            AppNotification("5", "Monthly Financial Report Ready", "Your previous monthly financial summary is ready for review.", "5 days ago", "REPORT")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            items(sampleNotifications, key = { it.id }) { notif ->
                val icon = when (notif.type) {
                    "SALARY" -> Icons.Default.AccountBalanceWallet
                    "BILL" -> Icons.Default.ReceiptLong
                    "BUDGET_WARN" -> Icons.Default.Warning
                    "BUDGET_EXCEED" -> Icons.Default.Error
                    "SAVINGS" -> Icons.Default.Savings
                    else -> Icons.Default.Assessment
                }
                val iconColor = when (notif.type) {
                    "SALARY", "SAVINGS" -> SuccessGreen
                    "BUDGET_WARN" -> WarningAmber
                    "BUDGET_EXCEED" -> AlertRose
                    else -> PrimaryLight
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
