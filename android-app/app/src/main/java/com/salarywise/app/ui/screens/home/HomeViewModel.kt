package com.salarywise.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.*
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: UserEntity? = null,
    val currentMonthYear: String = DateUtils.getCurrentMonthYear(),
    val salary: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSaved: Double = 0.0,
    val remainingMoney: Double = 0.0,
    val savingsRate: Double = 0.0,
    val budgetUsed: Double = 0.0,
    val upcomingBills: List<RecurringExpenseEntity> = emptyList(),
    val budgetWarnings: List<CategoryBudgetStatus> = emptyList(),
    val emergencyFundGoal: SavingsGoalEntity? = null,
    val topGoals: List<SavingsGoalEntity> = emptyList(),
    val keyInsight: FinancialInsight? = null,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(private val repository: SalaryWiseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val currentMonth = DateUtils.getCurrentMonthYear()
            val (startOfMonth, endOfMonth) = DateUtils.getStartAndEndOfMonth(currentMonth)

            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                // 1. Get salary for current month (or default salary)
                val salaryRecord = repository.getSalaryForMonth(user.id, currentMonth)
                val monthlySalary = salaryRecord?.inHandSalary ?: user.monthlyInHandSalary

                // 2. Get expenses for current month
                val expenses = repository.getExpensesInRange(user.id, startOfMonth, endOfMonth)
                val totalExpenses = expenses.sumOf { it.amount }

                // 3. Current month budget
                val budget = repository.getBudgetForMonth(user.id, currentMonth)
                val totalBudget = budget?.totalBudget ?: (monthlySalary * 0.70)
                val budgetCats = if (budget != null) repository.getCategoriesForBudget(budget.id) else emptyList()

                // Compute category statuses
                val catExpenses = expenses.groupBy { it.categoryName }
                val catStatuses = budgetCats.map { cat ->
                    val spent = catExpenses[cat.categoryName]?.sumOf { it.amount } ?: 0.0
                    FinancialCalculations.computeCategoryStatus(cat.categoryName, cat.allocatedAmount, spent)
                }
                val warnings = catStatuses.filter { it.isWarning || it.isExceeded }

                // 4. Calculations
                val metrics = FinancialCalculations.computeDashboardMetrics(
                    salary = monthlySalary,
                    expenses = totalExpenses,
                    totalBudget = totalBudget
                )

                // 5. Upcoming bills
                val now = System.currentTimeMillis()
                val twoWeeksAhead = now + (14L * 24 * 60 * 60 * 1000)
                val upcoming = repository.getUpcomingBills(user.id, twoWeeksAhead)

                // 6. Savings Goals & Emergency Fund
                val allGoals = repository.getAllGoals(user.id)
                val efGoal = allGoals.firstOrNull { it.isEmergencyFund }
                val otherGoals = allGoals.filter { !it.isEmergencyFund }.take(2)

                // 7. Smart Insights
                val prevMonth = DateUtils.getPreviousMonthYear(currentMonth)
                val (pStart, pEnd) = DateUtils.getStartAndEndOfMonth(prevMonth)
                val prevExpenses = repository.expenseDao.getTotalExpenseInRange(user.id, pStart, pEnd)
                val prevSalary = repository.getSalaryForMonth(user.id, prevMonth)?.inHandSalary ?: monthlySalary
                val prevSavings = (prevSalary - prevExpenses).coerceAtLeast(0.0)

                val emergencyMonths = if (user.monthlyEssentialExpenses > 0) {
                    (efGoal?.currentAmount ?: user.currentSavings) / user.monthlyEssentialExpenses
                } else 0.0

                val insights = SmartInsightsEngine.generateInsights(
                    currentSalary = monthlySalary,
                    currentExpenses = totalExpenses,
                    prevExpenses = prevExpenses,
                    currentSavings = metrics.totalSaved,
                    prevSavings = prevSavings,
                    categoryBudgets = catStatuses,
                    highestDiscretionaryCategory = catStatuses.filter { !it.categoryName.equals("Rent", true) && !it.categoryName.equals("Groceries", true) }.maxByOrNull { it.spentAmount }?.categoryName,
                    emergencyMonths = emergencyMonths
                )

                _uiState.update {
                    it.copy(
                        user = user,
                        currentMonthYear = currentMonth,
                        salary = monthlySalary,
                        totalExpenses = totalExpenses,
                        totalSaved = metrics.totalSaved,
                        remainingMoney = metrics.remainingMoney,
                        savingsRate = metrics.savingsRate,
                        budgetUsed = metrics.budgetUsed,
                        upcomingBills = upcoming,
                        budgetWarnings = warnings,
                        emergencyFundGoal = efGoal,
                        topGoals = otherGoals,
                        keyInsight = insights.firstOrNull(),
                        recentExpenses = expenses.take(5),
                        isLoading = false
                    )
                }
            }
        }
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
                id = java.util.UUID.randomUUID().toString(),
                userId = user.id,
                amount = amount,
                categoryName = category,
                date = date,
                paymentMethod = paymentMethod,
                description = description,
                isRecurring = isRecurring
            )
            repository.saveExpense(expense)
            if (isRecurring) {
                val recurring = RecurringExpenseEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = user.id,
                    name = description,
                    amount = amount,
                    categoryName = category,
                    nextDueDate = date + (30L * 24 * 60 * 60 * 1000)
                )
                repository.saveRecurringExpense(recurring)
            }
            loadDashboardData()
        }
    }

    fun payBill(bill: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.payRecurringBill(bill)
            loadDashboardData()
        }
    }
}
