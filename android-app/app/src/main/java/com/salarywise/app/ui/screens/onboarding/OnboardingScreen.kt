package com.salarywise.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.salarywise.app.ui.theme.PrimaryLight

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        salary: Double,
        salaryDate: Int,
        savings: Double,
        essentialExpenses: Double,
        savingsTarget: Double,
        emergencyFundTarget: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("40000") }
    var salaryDate by remember { mutableStateOf("1") }
    var currentSavings by remember { mutableStateOf("15000") }
    var essentialExpenses by remember { mutableStateOf("20000") }
    var monthlySavingsTarget by remember { mutableStateOf("12000") }
    var emergencyFundTarget by remember { mutableStateOf("120000") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Banner
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to SalaryWise",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track, plan, and grow your salary. Let's set up your first monthly financial plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "1. Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Divider()

                    Text(
                        text = "2. Monthly In-Hand Salary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = monthlySalary,
                        onValueChange = { monthlySalary = it },
                        label = { Text("In-Hand Salary (₹)") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = salaryDate,
                        onValueChange = { salaryDate = it },
                        label = { Text("Salary Credit Day (1 - 31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Divider()

                    Text(
                        text = "3. Financial Baseline & Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = currentSavings,
                        onValueChange = { currentSavings = it },
                        label = { Text("Current Savings (₹) (Optional)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = essentialExpenses,
                        onValueChange = { essentialExpenses = it },
                        label = { Text("Monthly Essential Expenses (₹) (Optional)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = monthlySavingsTarget,
                        onValueChange = { monthlySavingsTarget = it },
                        label = { Text("Monthly Savings Target (₹) (Optional)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = emergencyFundTarget,
                        onValueChange = { emergencyFundTarget = it },
                        label = { Text("Emergency Fund Target (₹) (Optional)") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val salaryVal = monthlySalary.toDoubleOrNull() ?: 40000.0
                    val dateVal = salaryDate.toIntOrNull() ?: 1
                    val savingsVal = currentSavings.toDoubleOrNull() ?: 0.0
                    val essentialVal = essentialExpenses.toDoubleOrNull() ?: (salaryVal * 0.5)
                    val targetVal = monthlySavingsTarget.toDoubleOrNull() ?: (salaryVal * 0.25)
                    val efTargetVal = emergencyFundTarget.toDoubleOrNull() ?: (essentialVal * 6)

                    onComplete(
                        name.ifBlank { "Employee" },
                        salaryVal,
                        dateVal,
                        savingsVal,
                        essentialVal,
                        targetVal,
                        efTargetVal
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isCreating) "Creating Profile..." else "Create My Financial Plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
