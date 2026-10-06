package com.salarywise.app.ui.screens.financialfreedom

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
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.DateUtils
import com.salarywise.app.domain.model.FinancialFreedomCalculator
import com.salarywise.app.domain.model.FinancialFreedomMetrics
import com.salarywise.app.ui.components.FinancialProgressBar
import com.salarywise.app.ui.theme.PrimaryLight
import com.salarywise.app.ui.theme.SuccessGreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinancialFreedomViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _metrics = MutableStateFlow<FinancialFreedomMetrics?>(null)
    val metrics: StateFlow<FinancialFreedomMetrics?> = _metrics.asStateFlow()

    init {
        loadMetrics()
    }

    private fun loadMetrics() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) return@collect
                val efGoal = repository.getEmergencyFundGoal(user.id)
                val efAmount = efGoal?.currentAmount ?: user.currentSavings
                val essential = if (user.monthlyEssentialExpenses > 0) user.monthlyEssentialExpenses else (user.monthlyInHandSalary * 0.5)

                val recurring = repository.recurringDao.getActiveRecurringExpensesFlow(user.id).firstOrNull() ?: emptyList()
                val totalDebtEmi = recurring.filter { it.categoryName.equals("EMI", true) || it.name.contains("EMI", true) || it.name.contains("Loan", true) }.sumOf { it.amount }

                val calculated = FinancialFreedomCalculator.calculate(
                    emergencyFund = efAmount,
                    essentialExpenses = essential,
                    savingsRate = if (user.monthlyInHandSalary > 0) (user.monthlySavingsTarget / user.monthlyInHandSalary) * 100.0 else 0.0,
                    totalSavings = efAmount,
                    monthlySalary = user.monthlyInHandSalary,
                    debtEmi = totalDebtEmi
                )
                _metrics.value = calculated
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialFreedomScreen(
    viewModel: FinancialFreedomViewModel,
    onNavigateBack: () -> Unit
) {
    val metricsState by viewModel.metrics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Stability & Freedom", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val m = metricsState
        if (m == null) {
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

                // Hero Emergency Coverage Indicator
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Stability Stage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = m.stabilityStage,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${String.format("%.1f", m.monthsCovered)} Months",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Emergency Coverage",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Target is 6 months
                            val targetProgress = ((m.monthsCovered / 6.0) * 100.0).coerceIn(0.0, 100.0)
                            FinancialProgressBar(percentage = targetProgress, height = 10)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = m.explanationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Core Stability Metrics Grid
                item {
                    Text(
                        text = "Financial Resilience Indicators",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Emergency Fund",
                                value = CurrencyFormatter.formatInr(m.emergencyFundAmount),
                                subtitle = "Liquid savings buffer",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Essential Expenses",
                                value = CurrencyFormatter.formatInr(m.essentialMonthlyExpenses),
                                subtitle = "Monthly living costs",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "Savings Rate",
                                value = "${String.format("%.1f", m.savingsRate)}%",
                                subtitle = "Target: >= 30%",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Monthly Debt/EMI",
                                value = CurrencyFormatter.formatInr(m.totalDebtEmi),
                                subtitle = "DTI: ${String.format("%.0f", m.debtToIncomeRatio)}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Strategic Recommendation
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Actionable Path Forward",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = m.recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Philosophy & Disclaimer Notice
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Important Notice",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SalaryWise does not promise or guarantee financial freedom or investment returns. The metrics above represent financial stability milestones to help you foster disciplined budgeting, debt containment, and resilient emergency savings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
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
                .padding(14.dp)
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
