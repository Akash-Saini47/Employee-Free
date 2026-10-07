package com.salarywise.app.data.repository

import android.content.Context
import com.salarywise.app.data.local.SalaryWiseDatabase
import com.salarywise.app.data.local.dao.CategoryTotal
import com.salarywise.app.data.local.entity.*
import com.salarywise.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SalaryWiseRepository(private val db: SalaryWiseDatabase) {

    private val userDao = db.userDao()
    private val salaryDao = db.salaryDao()
    private val budgetDao = db.budgetDao()
    private val expenseDao = db.expenseDao()
    private val recurringDao = db.recurringExpenseDao()
    private val savingsDao = db.savingsDao()
    private val reportDao = db.financialReportDao()

    // --- User & Onboarding ---
    fun getCurrentUserFlow(): Flow<UserEntity?> = userDao.getCurrentUserFlow()
    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()

    suspend fun completeOnboarding(
        name: String,
        monthlySalary: Double,
        salaryDate: Int,
        currentSavings: Double,
        essentialExpenses: Double,
        monthlySavingsTarget: Double,
        emergencyFundTarget: Double
    ): UserEntity = withContext(Dispatchers.IO) {
        db.withTransaction {
        val existingUser = userDao.getCurrentUser()
        if (existingUser?.isOnboardingCompleted == true) {
            throw IllegalStateException("A completed profile already exists.")
        }

        val userId = existingUser?.id ?: UUID.randomUUID().toString()
        if (existingUser != null) {
            // Recover safely from an older/partially-created onboarding record.
            expenseDao.deleteAllExpenses(userId)
            recurringDao.deleteAllRecurringExpenses(userId)
            savingsDao.deleteAllGoals(userId)
            salaryDao.deleteAllSalaries(userId)
            reportDao.deleteAllReports(userId)
            budgetDao.deleteAllBudgets(userId)
        }

        val user = UserEntity(
            id = userId,
            name = name.ifBlank { "User" },
            monthlyInHandSalary = monthlySalary,
            salaryDate = salaryDate.coerceIn(1, 31),
            currentSavings = currentSavings,
            monthlyEssentialExpenses = essentialExpenses,
            monthlySavingsTarget = monthlySavingsTarget,
            emergencyFundTarget = emergencyFundTarget,
            isOnboardingCompleted = true
        )
        if (existingUser == null) {
            userDao.insertUser(user)
        } else {
            userDao.updateUser(user)
        }

        // Automatically create the first monthly financial plan
        val currentMonth = DateUtils.getCurrentMonthYear()

        // 1. Initial Salary Record
        val salaryRecord = SalaryRecordEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            monthYear = currentMonth,
            grossSalary = monthlySalary,
            deductions = 0.0,
            inHandSalary = monthlySalary,
            paymentDate = System.currentTimeMillis(),
            notes = "Initial Onboarding Salary"
        )
        salaryDao.insertSalary(salaryRecord)

        // 2. Initial Budget with Default Categories based on salary and essential expenses
        val budgetId = UUID.randomUUID().toString()
        val initialBudgetTotal = if (monthlySalary > 0) monthlySalary * 0.70 else 25000.0
        val budget = BudgetEntity(
            id = budgetId,
            userId = userId,
            monthYear = currentMonth,
            totalBudget = initialBudgetTotal
        )
        budgetDao.insertBudget(budget)

        // Allocate default budget breakdown
        val defaultCategories = DefaultCategories.list.map { cat ->
            val allocated = when (cat.name) {
                "Rent" -> if (essentialExpenses > 0) essentialExpenses * 0.40 else monthlySalary * 0.25
                "Food" -> if (essentialExpenses > 0) essentialExpenses * 0.25 else monthlySalary * 0.15
                "Groceries" -> if (essentialExpenses > 0) essentialExpenses * 0.15 else monthlySalary * 0.10
                "Transportation" -> monthlySalary * 0.08
                "Utilities" -> monthlySalary * 0.05
                "Bills" -> monthlySalary * 0.05
                "Healthcare" -> monthlySalary * 0.04
                "Shopping" -> monthlySalary * 0.08
                "Entertainment" -> monthlySalary * 0.05
                "Savings" -> monthlySavingsTarget
                else -> 1000.0
            }
            BudgetCategoryEntity(
                id = UUID.randomUUID().toString(),
                budgetId = budgetId,
                userId = userId,
                categoryName = cat.name,
                allocatedAmount = Math.max(allocated, 500.0),
                isCustom = false,
                iconName = cat.iconName
            )
        }
        budgetDao.insertCategories(defaultCategories)

        // 3. Emergency Fund Savings Goal
        val efTarget = if (emergencyFundTarget > 0) emergencyFundTarget else (essentialExpenses * 6).coerceAtLeast(100000.0)
        val emergencyGoal = SavingsGoalEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = "Emergency Fund",
            targetAmount = efTarget,
            currentAmount = currentSavings,
            monthlyContribution = monthlySavingsTarget * 0.5,
            category = "Emergency",
            isEmergencyFund = true
        )
        savingsDao.insertGoal(emergencyGoal)

        user
        }
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun updatePin(userId: String, pin: String?) = withContext(Dispatchers.IO) {
        userDao.updatePin(userId, pin)
    }

    suspend fun updateDarkMode(userId: String, isDark: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateDarkMode(userId, isDark)
    }

    suspend fun updateNotifications(userId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateNotifications(userId, enabled)
    }

    // --- Salary Management ---
    fun getAllSalariesFlow(userId: String): Flow<List<SalaryRecordEntity>> = salaryDao.getAllSalariesFlow(userId)
    fun getSalaryGrowthFlow(userId: String): Flow<List<SalaryRecordEntity>> = salaryDao.getSalaryGrowthFlow(userId)
    fun getSalaryForMonthFlow(userId: String, monthYear: String): Flow<SalaryRecordEntity?> = salaryDao.getSalaryForMonthFlow(userId, monthYear)
    suspend fun getSalaryForMonth(userId: String, monthYear: String): SalaryRecordEntity? = salaryDao.getSalaryForMonth(userId, monthYear)

    suspend fun saveSalary(salary: SalaryRecordEntity) = withContext(Dispatchers.IO) {
        salaryDao.insertSalary(salary)
    }

    suspend fun deleteSalary(id: String) = withContext(Dispatchers.IO) {
        salaryDao.deleteSalaryById(id)
    }

    // --- Budget Management ---
    fun getBudgetForMonthFlow(userId: String, monthYear: String): Flow<BudgetEntity?> = budgetDao.getBudgetForMonthFlow(userId, monthYear)
    suspend fun getBudgetForMonth(userId: String, monthYear: String): BudgetEntity? = budgetDao.getBudgetForMonth(userId, monthYear)

    fun getCategoriesForBudgetFlow(budgetId: String): Flow<List<BudgetCategoryEntity>> = budgetDao.getCategoriesForBudgetFlow(budgetId)
    suspend fun getCategoriesForBudget(budgetId: String): List<BudgetCategoryEntity> = budgetDao.getCategoriesForBudget(budgetId)

    suspend fun saveBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun saveBudgetCategory(category: BudgetCategoryEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertCategory(category)
    }

    suspend fun deleteBudgetCategory(id: String) = withContext(Dispatchers.IO) {
        budgetDao.deleteCategoryById(id)
    }

    // --- Expense Management ---
    fun getAllExpensesFlow(userId: String): Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow(userId)
    fun getExpensesInRangeFlow(userId: String, start: Long, end: Long): Flow<List<ExpenseEntity>> = expenseDao.getExpensesInRangeFlow(userId, start, end)
    suspend fun getExpensesInRange(userId: String, start: Long, end: Long): List<ExpenseEntity> = expenseDao.getExpensesInRange(userId, start, end)
    fun getCategoryTotalsInRangeFlow(userId: String, start: Long, end: Long): Flow<List<CategoryTotal>> = expenseDao.getCategoryTotalsInRangeFlow(userId, start, end)
    suspend fun getCategoryTotalsInRange(userId: String, start: Long, end: Long): List<CategoryTotal> = expenseDao.getCategoryTotalsInRange(userId, start, end)
    suspend fun getTotalExpenseInRange(userId: String, start: Long, end: Long): Double = expenseDao.getTotalExpenseInRange(userId, start, end)

    fun filterExpensesFlow(
        userId: String,
        startDate: Long,
        endDate: Long,
        category: String? = null,
        paymentMethod: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        searchQuery: String? = null
    ): Flow<List<ExpenseEntity>> = expenseDao.filterExpensesFlow(
        userId, startDate, endDate, category, paymentMethod, minAmount, maxAmount, searchQuery
    )

    suspend fun saveExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(id: String) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }

    // --- Recurring Expenses ---
    fun getActiveRecurringExpensesFlow(userId: String): Flow<List<RecurringExpenseEntity>> = recurringDao.getActiveRecurringExpensesFlow(userId)
    fun getAllRecurringExpensesFlow(userId: String): Flow<List<RecurringExpenseEntity>> = recurringDao.getAllRecurringExpensesFlow(userId)
    fun getUpcomingBillsFlow(userId: String, upToTimestamp: Long): Flow<List<RecurringExpenseEntity>> = recurringDao.getUpcomingBillsFlow(userId, upToTimestamp)
    suspend fun getUpcomingBills(userId: String, upToTimestamp: Long): List<RecurringExpenseEntity> = recurringDao.getUpcomingBills(userId, upToTimestamp)

    suspend fun saveRecurringExpense(recurring: RecurringExpenseEntity) = withContext(Dispatchers.IO) {
        recurringDao.insertRecurringExpense(recurring)
    }

    suspend fun deleteRecurringExpense(id: String) = withContext(Dispatchers.IO) {
        recurringDao.deleteRecurringExpenseById(id)
    }

    suspend fun payRecurringBill(recurring: RecurringExpenseEntity, paymentMethod: String = "UPI") = withContext(Dispatchers.IO) {
        db.withTransaction {
        // 1. Log actual expense
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            userId = recurring.userId,
            amount = recurring.amount,
            categoryName = recurring.categoryName,
            date = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            description = "Recurring bill: ${recurring.name}",
            isRecurring = true,
            recurringExpenseId = recurring.id
        )
        expenseDao.insertExpense(expense)

        // 2. Advance due date
        val nextDue = advanceDueDate(recurring.nextDueDate, recurring.frequency)
        recurringDao.updateRecurringExpense(recurring.copy(nextDueDate = nextDue))
        }
    }

    private fun advanceDueDate(currentDue: Long, frequency: String): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentDue }
        val now = System.currentTimeMillis()
        while (cal.timeInMillis <= now) {
            when (frequency.uppercase()) {
                "WEEKLY" -> cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(java.util.Calendar.MONTH, 1)
                "QUARTERLY" -> cal.add(java.util.Calendar.MONTH, 3)
                "YEARLY" -> cal.add(java.util.Calendar.YEAR, 1)
                else -> cal.add(java.util.Calendar.MONTH, 1)
            }
        }
        return cal.timeInMillis
    }

    // --- Savings & Goals ---
    fun getAllGoalsFlow(userId: String): Flow<List<SavingsGoalEntity>> = savingsDao.getAllGoalsFlow(userId)
    suspend fun getAllGoals(userId: String): List<SavingsGoalEntity> = savingsDao.getAllGoals(userId)
    fun getEmergencyFundGoalFlow(userId: String): Flow<SavingsGoalEntity?> = savingsDao.getEmergencyFundGoalFlow(userId)
    suspend fun getEmergencyFundGoal(userId: String): SavingsGoalEntity? = savingsDao.getEmergencyFundGoal(userId)

    suspend fun saveGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsDao.insertGoal(goal)
    }

    suspend fun deleteGoal(id: String) = withContext(Dispatchers.IO) {
        savingsDao.deleteGoalById(id)
    }

    suspend fun addGoalDeposit(goalId: String, userId: String, amount: Double, note: String = "") = withContext(Dispatchers.IO) {
        val contribution = SavingsContributionEntity(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            userId = userId,
            amount = amount,
            date = System.currentTimeMillis(),
            notes = note
        )
        savingsDao.addContributionAndUpdateGoal(contribution)
    }

    // --- Financial Reports ---
    fun getAllReportsFlow(userId: String): Flow<List<FinancialReportEntity>> = reportDao.getAllReportsFlow(userId)
    fun getReportForMonthFlow(userId: String, monthYear: String): Flow<FinancialReportEntity?> = reportDao.getReportForMonthFlow(userId, monthYear)

    suspend fun generateMonthlyReport(userId: String, monthYear: String): FinancialReportEntity = withContext(Dispatchers.IO) {
        val (start, end) = DateUtils.getStartAndEndOfMonth(monthYear)
        val salaryRecord = salaryDao.getSalaryForMonth(userId, monthYear)
        val income = salaryRecord?.inHandSalary ?: 0.0

        val expenses = expenseDao.getExpensesInRange(userId, start, end)
        val totalExpenses = expenses.sumOf { it.amount }
        val totalSavings = (income - totalExpenses).coerceAtLeast(0.0)
        val savingsRate = FinancialCalculations.calculateSavingsRate(totalSavings, income)

        val budget = budgetDao.getBudgetForMonth(userId, monthYear)
        val budgetUsed = if (budget != null && budget.totalBudget > 0) {
            (totalExpenses / budget.totalBudget) * 100.0
        } else 0.0

        val categoryTotals = expenseDao.getCategoryTotalsInRange(userId, start, end)
        val topCat = categoryTotals.maxByOrNull { it.totalAmount }?.categoryName ?: "None"

        val highestExp = expenseDao.getHighestExpenseInRange(userId, start, end)
        val highestExpAmount = highestExp?.amount ?: 0.0
        val highestExpName = highestExp?.description ?: "None"

        // Previous month comparison
        val prevMonth = DateUtils.getPreviousMonthYear(monthYear)
        val (pStart, pEnd) = DateUtils.getStartAndEndOfMonth(prevMonth)
        val prevExpenses = expenseDao.getTotalExpenseInRange(userId, pStart, pEnd)
        val prevSalary = salaryDao.getSalaryForMonth(userId, prevMonth)?.inHandSalary ?: 0.0
        val prevSavings = (prevSalary - prevExpenses).coerceAtLeast(0.0)

        val expChange = if (prevExpenses > 0) ((totalExpenses - prevExpenses) / prevExpenses) * 100.0 else 0.0
        val savChange = if (prevSavings > 0) ((totalSavings - prevSavings) / prevSavings) * 100.0 else 0.0

        val report = FinancialReportEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            monthYear = monthYear,
            income = income,
            totalExpenses = totalExpenses,
            totalSavings = totalSavings,
            savingsRate = savingsRate,
            budgetUsed = budgetUsed,
            topCategory = topCat,
            largestExpenseAmount = highestExpAmount,
            largestExpenseName = highestExpName,
            expenseChangePercent = expChange,
            savingsChangePercent = savChange,
            healthScore = FinancialHealthScoreCalculator.calculateScore(
                savingsRate = savingsRate,
                budgetUtilization = budgetUsed,
                emergencyFundMonths = run {
                    val goal = savingsDao.getEmergencyFundGoal(userId)
                    val essentials = userDao.getCurrentUser()?.monthlyEssentialExpenses ?: 0.0
                    if (essentials > 0) (goal?.currentAmount ?: userDao.getCurrentUser()?.currentSavings ?: 0.0) / essentials else 0.0
                },
                isExpensesOverIncome = totalExpenses > income && income > 0,
                hasEmergencyFundGoal = savingsDao.getEmergencyFundGoal(userId) != null
            ).score,
            summaryText = "Income: ${CurrencyFormatter.formatInr(income)} | Expenses: ${CurrencyFormatter.formatInr(totalExpenses)} | Savings: ${CurrencyFormatter.formatInr(totalSavings)}"
        )
        reportDao.insertReport(report)
        report
    }

    // --- Data Export & Import / Clear ---
    suspend fun exportAllDataJson(userId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val user = userDao.getCurrentUser()
        val salaries = salaryDao.getAllSalariesFlow(userId).firstOrNull() ?: emptyList()
        val budgets = budgetDao.getAllBudgetsFlow(userId).firstOrNull() ?: emptyList()
        val expenses = expenseDao.getAllExpensesFlow(userId).firstOrNull() ?: emptyList()
        val recurring = recurringDao.getAllRecurringExpensesFlow(userId).firstOrNull() ?: emptyList()
        val goals = savingsDao.getAllGoals(userId)

        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("user", JSONObject().apply {
            user?.let {
                put("name", it.name)
                put("salary", it.monthlyInHandSalary)
                put("savings", it.currentSavings)
                put("essentialExpenses", it.monthlyEssentialExpenses)
            }
        })

        root.put("salaries", JSONArray(salaries.map { e -> JSONObject().apply {
            put("monthYear", e.monthYear); put("grossSalary", e.grossSalary); put("deductions", e.deductions); put("inHandSalary", e.inHandSalary); put("paymentDate", e.paymentDate); put("notes", e.notes)
        }}))
        root.put("budgets", JSONArray(budgets.map { b -> JSONObject().apply {
            put("monthYear", b.monthYear); put("totalBudget", b.totalBudget); put("categories", JSONArray(budgetDao.getCategoriesForBudget(b.id).map { c -> JSONObject().apply { put("category", c.categoryName); put("allocatedAmount", c.allocatedAmount); put("isCustom", c.isCustom) } }))
        }}))
        root.put("expenses", JSONArray(expenses.map { e -> JSONObject().apply {
            put("id", e.id); put("amount", e.amount); put("category", e.categoryName); put("date", e.date); put("paymentMethod", e.paymentMethod); put("description", e.description); put("isRecurring", e.isRecurring)
        }}))
        root.put("recurringExpenses", JSONArray(recurring.map { e -> JSONObject().apply {
            put("name", e.name); put("amount", e.amount); put("category", e.categoryName); put("frequency", e.frequency); put("nextDueDate", e.nextDueDate); put("isActive", e.isActive)
        }}))
        root.put("savingsGoals", JSONArray(goals.map { g -> JSONObject().apply {
            put("title", g.title); put("targetAmount", g.targetAmount); put("currentAmount", g.currentAmount); put("monthlyContribution", g.monthlyContribution); put("category", g.category); put("targetDate", g.targetDate); put("isCompleted", g.isCompleted); put("isEmergencyFund", g.isEmergencyFund)
        }}))

        root.toString(2)
    }

    suspend fun deleteAllUserData(userId: String) = withContext(Dispatchers.IO) {
        db.withTransaction {
            expenseDao.deleteAllExpenses(userId)
            recurringDao.deleteAllRecurringExpenses(userId)
            savingsDao.deleteAllGoals(userId)
            salaryDao.deleteAllSalaries(userId)
            budgetDao.deleteAllBudgets(userId)
            reportDao.deleteAllReports(userId)
            userDao.getCurrentUser()?.takeIf { it.id == userId }?.let { user ->
                userDao.updateUser(
                    user.copy(
                        monthlyInHandSalary = 0.0,
                        salaryDate = 1,
                        currentSavings = 0.0,
                        monthlyEssentialExpenses = 0.0,
                        monthlySavingsTarget = 0.0,
                        emergencyFundTarget = 0.0,
                        pinHash = null,
                        isBiometricEnabled = false
                    )
                )
            }
        }
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        userDao.deleteAllUsers()
    }
}
