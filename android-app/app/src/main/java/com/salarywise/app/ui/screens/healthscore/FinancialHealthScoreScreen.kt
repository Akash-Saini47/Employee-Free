package com.salarywise.app.ui.screens.healthscore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.salarywise.app.domain.model.FinancialCalculations
import com.salarywise.app.domain.model.FinancialHealthScoreCalculator
import com.salarywise.app.domain.model.FinancialHealthScoreResult
import com.salarywise.app.domain.model.HealthScoreFactor
import com.salarywise.app.ui.theme.AlertRose
import com.salarywise.app.ui.theme.SuccessGreen
import com.salarywise.app.ui.theme.WarningAmber
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinancialHealthScoreViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _scoreResult = MutableStateFlow<FinancialHealthScoreResult?>(null)
    val scoreResult: StateFlow<FinancialHealthScoreResult?> = _scoreResult.asStateFlow()

    init {
        computeScore()
    }

    private fun computeScore() {
        viewModelScope.launch {
            val user = repository.getCurrentUser() ?: return@launch
            val month = com.salarywise.app.domain.model.DateUtils.getCurrentMonthYear()
            val (start, end) = com.salarywise.app.domain.model.DateUtils.getStartAndEndOfMonth(month)

            val salaryRecord = repository.getSalaryForMonth(user.id, month)
            val salary = salaryRecord?.inHandSalary ?: user.monthlyInHandSalary

            val expenses = repository.getExpensesInRange(user.id, start, end)
            val totalExp = expenses.sumOf { it.amount }

            val savings = (salary - totalExp).coerceAtLeast(0.0)
            val sRate = FinancialCalculations.calculateSavingsRate(savings, salary)

            val budget = repository.getBudgetForMonth(user.id, month)
            val totalBudget = budget?.totalBudget ?: (salary * 0.70)
            val bUtil = FinancialCalculations.calculateBudgetUtilization(totalExp, totalBudget)

            val efGoal = repository.getEmergencyFundGoal(user.id)
            val efAmt = efGoal?.currentAmount ?: user.currentSavings
            val essential = if (user.monthlyEssentialExpenses > 0) user.monthlyEssentialExpenses else (salary * 0.5)
            val efMonths = if (essential > 0) efAmt / essential else 0.0

            val catExpenses = expenses.groupBy { it.categoryName }
            val shoppingSpent = catExpenses["Shopping"]?.sumOf { it.amount } ?: 0.0
            val spike = if (shoppingSpent > (salary * 0.20)) "Shopping" else null

            val result = FinancialHealthScoreCalculator.calculateScore(
                savingsRate = sRate,
                budgetUtilization = bUtil,
                emergencyFundMonths = efMonths,
                isExpensesOverIncome = totalExp > salary && salary > 0,
                hasEmergencyFundGoal = efGoal != null,
                discretionarySpikeCategory = spike
            )
            _scoreResult.value = result
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialHealthScoreScreen(
    viewModel: FinancialHealthScoreViewModel,
    onNavigateBack: () -> Unit
) {
    val resultState by viewModel.scoreResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Health Score", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val res = resultState
        if (res == null) {
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

                // Score Gauge Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Financial Health Score",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${res.score}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "/ 100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Rating: ${res.rating}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Why is your score X? Breakdown
                item {
                    Text(
                        text = "Why your score is ${res.score} / 100",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Positive Factors
                items(res.positiveFactors) { f ->
                    FactorCard(factor = f)
                }

                // Negative Factors
                items(res.negativeFactors) { f ->
                    FactorCard(factor = f)
                }

                // Professional Advice Disclaimer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = res.disclaimer,
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
private fun FactorCard(factor: HealthScoreFactor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (factor.isPositive) SuccessGreen else AlertRose,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${if (factor.isPositive) "+ " else "- "}${factor.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (factor.isPositive) SuccessGreen else AlertRose
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = factor.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
