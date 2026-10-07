package com.salarywise.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.dao.CategoryTotal
import com.salarywise.app.data.local.entity.ExpenseEntity
import com.salarywise.app.data.local.entity.SalaryRecordEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.DateUtils
import com.salarywise.app.domain.model.FinancialCalculations
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AnalyticsTimeRange(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year")
}

data class AnalyticsUiState(
    val user: UserEntity? = null,
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.THIS_MONTH,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val budgetUtilization: Double = 0.0,
    val averageMonthlyExpense: Double = 0.0,
    val averageMonthlySavings: Double = 0.0,
    val highestExpense: ExpenseEntity? = null,
    val highestSpendingCategory: String = "None",
    val lowestSpendingCategory: String = "None",
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val monthlySalaries: List<SalaryRecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AnalyticsViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics(AnalyticsTimeRange.THIS_MONTH)
    }

    fun setRange(range: AnalyticsTimeRange) {
        _uiState.update { it.copy(selectedRange = range) }
        loadAnalytics(range)
    }

    private fun loadAnalytics(range: AnalyticsTimeRange) {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                val currentMonth = DateUtils.getCurrentMonthYear()
                val monthYears = when (range) {
                    AnalyticsTimeRange.THIS_MONTH -> listOf(currentMonth)
                    AnalyticsTimeRange.LAST_MONTH -> listOf(DateUtils.getPreviousMonthYear(currentMonth))
                    AnalyticsTimeRange.LAST_3_MONTHS -> (0 until 3).map { offset ->
                        var month = currentMonth
                        repeat(offset) { month = DateUtils.getPreviousMonthYear(month) }
                        month
                    }.reversed()
                    AnalyticsTimeRange.LAST_6_MONTHS -> (0 until 6).map { offset ->
                        var month = currentMonth
                        repeat(offset) { month = DateUtils.getPreviousMonthYear(month) }
                        month
                    }.reversed()
                    AnalyticsTimeRange.THIS_YEAR -> {
                        val current = currentMonth.substringAfter('-').toInt()
                        (1..current).map { month -> String.format("%04d-%02d", currentMonth.substringBefore('-').toInt(), month) }
                    }
                }
                val (start, end) = if (range == AnalyticsTimeRange.THIS_YEAR) {
                    val first = DateUtils.getStartAndEndOfMonth(monthYears.first()).first
                    val last = DateUtils.getStartAndEndOfMonth(currentMonth).second
                    first to last
                } else {
                    DateUtils.getStartAndEndOfMonth(monthYears.first()).first to DateUtils.getStartAndEndOfMonth(monthYears.last()).second
                }

                val expenses = repository.getExpensesInRange(user.id, start, end)
                val totalExp = expenses.sumOf { it.amount }
                val highestExp = expenses.maxByOrNull { it.amount }
                val monthsCount = monthYears.size

                val income = monthYears.sumOf { month ->
                    repository.getSalaryForMonth(user.id, month)?.inHandSalary ?: user.monthlyInHandSalary
                }
                val savings = (income - totalExp).coerceAtLeast(0.0)
                val sRate = FinancialCalculations.calculateSavingsRate(savings, income)

                val totalBudget = monthYears.sumOf { month ->
                    val monthSalary = repository.getSalaryForMonth(user.id, month)?.inHandSalary ?: user.monthlyInHandSalary
                    repository.getBudgetForMonth(user.id, month)?.totalBudget ?: (monthSalary * 0.70)
                }
                val bUtil = FinancialCalculations.calculateBudgetUtilization(totalExp, totalBudget)

                val catTotals = repository.getCategoryTotalsInRange(user.id, start, end)
                val highestCat = catTotals.maxByOrNull { it.totalAmount }?.categoryName ?: "None"
                val lowestCat = catTotals.minByOrNull { it.totalAmount }?.categoryName ?: "None"

                val allSalaries = repository.getAllSalariesFlow(user.id).firstOrNull() ?: emptyList()

                _uiState.update {
                    it.copy(
                        user = user,
                        selectedRange = range,
                        totalIncome = income,
                        totalExpense = totalExp,
                        totalSavings = savings,
                        savingsRate = sRate,
                        budgetUtilization = bUtil,
                        averageMonthlyExpense = totalExp / monthsCount,
                        averageMonthlySavings = savings / monthsCount,
                        highestExpense = highestExp,
                        highestSpendingCategory = highestCat,
                        lowestSpendingCategory = lowestCat,
                        categoryBreakdown = catTotals,
                        monthlySalaries = allSalaries.filter { it.monthYear in monthYears }.sortedBy { s -> s.monthYear },
                        isLoading = false
                    )
                }
            }
        }
    }
}
