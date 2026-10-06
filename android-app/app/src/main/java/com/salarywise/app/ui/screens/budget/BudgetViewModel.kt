package com.salarywise.app.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.BudgetCategoryEntity
import com.salarywise.app.data.local.entity.BudgetEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class BudgetUiState(
    val user: UserEntity? = null,
    val currentMonthYear: String = DateUtils.getCurrentMonthYear(),
    val budget: BudgetEntity? = null,
    val categories: List<CategoryBudgetStatus> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val overallPercentage: Double = 0.0,
    val warningCount: Int = 0,
    val exceededCount: Int = 0,
    val isLoading: Boolean = true
)

class BudgetViewModel(private val repository: SalaryWiseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgetData(DateUtils.getCurrentMonthYear())
    }

    fun loadBudgetData(monthYear: String) {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                val budget = repository.getBudgetForMonth(user.id, monthYear)
                val budgetCategories = if (budget != null) repository.getCategoriesForBudget(budget.id) else emptyList()

                val (start, end) = DateUtils.getStartAndEndOfMonth(monthYear)
                val expenses = repository.getExpensesInRange(user.id, start, end)
                val catExpenses = expenses.groupBy { it.categoryName }

                val statuses = budgetCategories.map { cat ->
                    val spent = catExpenses[cat.categoryName]?.sumOf { it.amount } ?: 0.0
                    FinancialCalculations.computeCategoryStatus(cat.categoryName, cat.allocatedAmount, spent)
                }

                val totalB = budgetCategories.sumOf { it.allocatedAmount }
                val totalS = expenses.sumOf { it.amount }
                val remaining = totalB - totalS
                val overallPerc = if (totalB > 0) (totalS / totalB) * 100.0 else 0.0

                _uiState.update {
                    it.copy(
                        user = user,
                        currentMonthYear = monthYear,
                        budget = budget,
                        categories = statuses,
                        totalBudget = totalB,
                        totalSpent = totalS,
                        totalRemaining = remaining,
                        overallPercentage = overallPerc,
                        warningCount = statuses.count { it.isWarning },
                        exceededCount = statuses.count { it.isExceeded },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addCustomCategory(categoryName: String, allocatedAmount: Double) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            var budget = _uiState.value.budget
            if (budget == null) {
                budget = BudgetEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    monthYear = _uiState.value.currentMonthYear,
                    totalBudget = allocatedAmount
                )
                repository.saveBudget(budget)
            }
            val cat = BudgetCategoryEntity(
                id = UUID.randomUUID().toString(),
                budgetId = budget.id,
                userId = user.id,
                categoryName = categoryName,
                allocatedAmount = allocatedAmount,
                isCustom = true
            )
            repository.saveBudgetCategory(cat)
            loadBudgetData(_uiState.value.currentMonthYear)
        }
    }

    fun updateCategoryBudget(categoryName: String, newAmount: Double) {
        viewModelScope.launch {
            val budget = _uiState.value.budget ?: return@launch
            val existing = repository.getCategoriesForBudget(budget.id).firstOrNull { it.categoryName == categoryName }
            if (existing != null) {
                repository.saveBudgetCategory(existing.copy(allocatedAmount = newAmount))
                loadBudgetData(_uiState.value.currentMonthYear)
            }
        }
    }
}
