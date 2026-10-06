package com.salarywise.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.DateUtils
import com.salarywise.app.ui.components.FinancialProgressBar
import com.salarywise.app.ui.theme.AlertRose
import com.salarywise.app.ui.theme.PrimaryLight
import com.salarywise.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Analytics", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Range Filter Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalyticsTimeRange.values().forEach { range ->
                        FilterChip(
                            selected = state.selectedRange == range,
                            onClick = { viewModel.setRange(range) },
                            label = { Text(range.label) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Key Statistics Cards
            item {
                Text(
                    text = "Key Financial Indicators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "Avg Monthly Expense",
                            value = CurrencyFormatter.formatInr(state.averageMonthlyExpense),
                            color = AlertRose,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "Avg Monthly Savings",
                            value = CurrencyFormatter.formatInr(state.averageMonthlySavings),
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "Savings Rate",
                            value = "${String.format("%.1f", state.savingsRate)}%",
                            color = PrimaryLight,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "Budget Utilization",
                            value = "${String.format("%.0f", state.budgetUtilization)}%",
                            color = if (state.budgetUtilization > 100) AlertRose else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            title = "Top Category",
                            value = state.highestSpendingCategory,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            title = "Largest Expense",
                            value = state.highestExpense?.let { CurrencyFormatter.formatInr(it.amount) } ?: "₹0",
                            color = AlertRose,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Chart 1: Income vs Expenses
            item {
                Text(
                    text = "1. Income vs Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        val maxVal = Math.max(state.totalIncome, state.totalExpense).coerceAtLeast(1.0)

                        // Income Bar
                        BarComparisonRow(
                            label = "Total Income",
                            amount = CurrencyFormatter.formatInr(state.totalIncome),
                            ratio = (state.totalIncome / maxVal).toFloat(),
                            barColor = SuccessGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Expenses Bar
                        BarComparisonRow(
                            label = "Total Expenses",
                            amount = CurrencyFormatter.formatInr(state.totalExpense),
                            ratio = (state.totalExpense / maxVal).toFloat(),
                            barColor = AlertRose
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Savings Bar
                        BarComparisonRow(
                            label = "Net Saved",
                            amount = CurrencyFormatter.formatInr(state.totalSavings),
                            ratio = (state.totalSavings / maxVal).toFloat(),
                            barColor = PrimaryLight
                        )
                    }
                }
            }

            // Chart 2: Category Breakdown
            item {
                Text(
                    text = "2. Category-Wise Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.categoryBreakdown.isEmpty()) {
                            Text("No expenses in this period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val total = state.totalExpense.coerceAtLeast(1.0)
                            state.categoryBreakdown.forEach { cat ->
                                val pct = (cat.totalAmount / total) * 100.0
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${cat.categoryName} (${cat.expenseCount})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${CurrencyFormatter.formatInr(cat.totalAmount)} (${String.format("%.1f", pct)}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FinancialProgressBar(percentage = pct, height = 6)
                                }
                            }
                        }
                    }
                }
            }

            // Chart 3: Salary Growth
            item {
                Text(
                    text = "3. Salary Growth Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val maxS = state.monthlySalaries.maxOfOrNull { it.inHandSalary }?.coerceAtLeast(1.0) ?: 1.0
                        state.monthlySalaries.forEach { s ->
                            BarComparisonRow(
                                label = DateUtils.formatMonthYear(s.monthYear),
                                amount = CurrencyFormatter.formatInr(s.inHandSalary),
                                ratio = (s.inHandSalary / maxS).toFloat(),
                                barColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BarComparisonRow(
    label: String,
    amount: String,
    ratio: Float,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = ratio.coerceIn(0.01f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
