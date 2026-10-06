package com.salarywise.app.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.ExpenseEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class DateFilterType(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class ExpenseUiState(
    val user: UserEntity? = null,
    val expenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val totalExpenseSum: Double = 0.0,
    val selectedDateFilter: DateFilterType = DateFilterType.THIS_MONTH,
    val selectedCategory: String? = null,
    val selectedPaymentMethod: String? = null,
    val searchQuery: String = "",
    val sortByAmountDesc: Boolean = false,
    val isLoading: Boolean = true
)

class ExpenseViewModel(private val repository: SalaryWiseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                repository.getAllExpensesFlow(user.id).collect { allList ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            expenses = allList,
                            isLoading = false
                        )
                    }
                    applyFilters()
                }
            }
        }
    }

    fun setDateFilter(filter: DateFilterType) {
        _uiState.update { it.copy(selectedDateFilter = filter) }
        applyFilters()
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun setPaymentMethodFilter(method: String?) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun toggleSort() {
        _uiState.update { it.copy(sortByAmountDesc = !it.sortByAmountDesc) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val (start, end) = when (state.selectedDateFilter) {
            DateFilterType.TODAY -> DateUtils.getStartAndEndOfDay()
            DateFilterType.THIS_WEEK -> DateUtils.getStartAndEndOfWeek()
            DateFilterType.THIS_MONTH -> DateUtils.getStartAndEndOfMonth(DateUtils.getCurrentMonthYear())
            DateFilterType.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        }

        var result = state.expenses.filter { it.date in start..end }

        if (!state.selectedCategory.isNullOrBlank()) {
            result = result.filter { it.categoryName.equals(state.selectedCategory, ignoreCase = true) }
        }

        if (!state.selectedPaymentMethod.isNullOrBlank()) {
            result = result.filter { it.paymentMethod.equals(state.selectedPaymentMethod, ignoreCase = true) }
        }

        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            result = result.filter {
                it.description.lowercase().contains(q) || it.categoryName.lowercase().contains(q)
            }
        }

        result = if (state.sortByAmountDesc) {
            result.sortedByDescending { it.amount }
        } else {
            result.sortedByDescending { it.date }
        }

        val total = result.sumOf { it.amount }
        _uiState.update { it.copy(filteredExpenses = result, totalExpenseSum = total) }
    }

    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        paymentMethod: String,
        description: String,
        isRecurring: Boolean
    ) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val expense = ExpenseEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                amount = amount,
                categoryName = category,
                date = date,
                paymentMethod = paymentMethod,
                description = description,
                isRecurring = isRecurring
            )
            repository.saveExpense(expense)
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }
}
