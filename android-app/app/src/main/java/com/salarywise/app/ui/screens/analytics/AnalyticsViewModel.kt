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
                val (start, end) = when (range) {
                    AnalyticsTimeRange.THIS_MONTH -> DateUtils.getStartAndEndOfMonth(currentMonth)
                    AnalyticsTimeRange.LAST_MONTH -> DateUtils.getStartAndEndOfMonth(DateUtils.getPreviousMonthYear(currentMonth))
                    AnalyticsTimeRange.LAST_3_MONTHS -> DateUtils.getRangeForMonthsAgo(3)
                    AnalyticsTimeRange.LAST_6_MONTHS -> DateUtils.getRangeForMonthsAgo(6)
                    AnalyticsTimeRange.THIS_YEAR -> DateUtils.getStartAndEndOfCurrentYear()
                }

                val expenses = repository.getExpensesInRange(user.id, start, end)
                val totalExp = expenses.sumOf { it.amount }
                val highestExp = expenses.maxByOrNull { it.amount }

                val salaryRecord = repository.getSalaryForMonth(user.id, currentMonth)
                val monthsCount = when (range) {
                    AnalyticsTimeRange.THIS_MONTH, AnalyticsTimeRange.LAST_MONTH -> 1
                    AnalyticsTimeRange.LAST_3_MONTHS -> 3
                    AnalyticsTimeRange.LAST_6_MONTHS -> 6
                    AnalyticsTimeRange.THIS_YEAR -> 12
                }
                val income = (salaryRecord?.inHandSalary ?: user.monthlyInHandSalary) * monthsCount
                val savings = (income - totalExp).coerceAtLeast(0.0)
                val sRate = FinancialCalculations.calculateSavingsRate(savings, income)

                val budget = repository.getBudgetForMonth(user.id, currentMonth)
                val totalBudget = (budget?.totalBudget ?: (user.monthlyInHandSalary * 0.70)) * monthsCount
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
                        monthlySalaries = allSalaries.sortedBy { s -> s.monthYear },
                        isLoading = false
                    )
                }
            }
        }
    }
}
