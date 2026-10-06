package com.salarywise.app.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.FinancialReportEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.DateUtils
import com.salarywise.app.ui.theme.AlertRose
import com.salarywise.app.ui.theme.PrimaryLight
import com.salarywise.app.ui.theme.SuccessGreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MonthlyReportViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _report = MutableStateFlow<FinancialReportEntity?>(null)
    val report: StateFlow<FinancialReportEntity?> = _report.asStateFlow()

    init {
        generateReport(DateUtils.getCurrentMonthYear())
    }

    fun generateReport(monthYear: String) {
        viewModelScope.launch {
            val user = repository.getCurrentUser() ?: return@launch
            val rep = repository.generateMonthlyReport(user.id, monthYear)
            _report.value = rep
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    viewModel: MonthlyReportViewModel,
    onNavigateBack: () -> Unit
) {
    val reportState by viewModel.report.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Financial Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val report = reportState
        if (report == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Hero Summary Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "MONTHLY SUMMARY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = DateUtils.formatMonthYear(report.monthYear),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Income", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyFormatter.formatInr(report.income), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Column {
                                    Text("Expenses", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyFormatter.formatInr(report.totalExpenses), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = AlertRose)
                                }
                                Column {
                                    Text("Savings", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyFormatter.formatInr(report.totalSavings), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Savings Rate: ${String.format("%.1f", report.savingsRate)}%", fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                                Text("Budget Used: ${String.format("%.0f", report.budgetUsed)}%", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Month-over-Month Comparison
                item {
                    Text(
                        text = "Compared with Previous Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Expenses Trend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                val isExpDown = report.expenseChangePercent <= 0
                                Text(
                                    text = if (isExpDown) "↓ ${String.format("%.1f", Math.abs(report.expenseChangePercent))}%" else "↑ ${String.format("%.1f", report.expenseChangePercent)}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpDown) SuccessGreen else AlertRose
                                )
                            }
                            Divider(modifier = Modifier.height(40.dp).width(1.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Savings Trend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                val isSavUp = report.savingsChangePercent >= 0
                                Text(
                                    text = if (isSavUp) "↑ ${String.format("%.1f", report.savingsChangePercent)}%" else "↓ ${String.format("%.1f", Math.abs(report.savingsChangePercent))}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSavUp) SuccessGreen else AlertRose
                                )
                            }
                        }
                    }
                }

                // Top Spending Insights
                item {
                    Text(
                        text = "Key Expense Highlights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportHighlightRow(
                            title = "Top Spending Category",
                            value = report.topCategory,
                            subtitle = "Consumes largest share of monthly budget"
                        )
                        ReportHighlightRow(
                            title = "Largest Single Expense",
                            value = "${CurrencyFormatter.formatInr(report.largestExpenseAmount)} (${report.largestExpenseName})",
                            subtitle = "Highest individual transaction this month"
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
private fun ReportHighlightRow(title: String, value: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
