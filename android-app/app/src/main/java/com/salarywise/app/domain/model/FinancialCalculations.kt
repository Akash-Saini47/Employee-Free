package com.salarywise.app.domain.model

data class DashboardMetrics(
    val monthlySalary: Double,
    val totalExpenses: Double,
    val totalSaved: Double,
    val remainingMoney: Double,
    val savingsRate: Double,
    val budgetUsed: Double,
    val isExpensesOverIncome: Boolean,
    val topWarning: String? = null
)

data class CategoryBudgetStatus(
    val categoryName: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Double,
    val isWarning: Boolean, // >= 80%
    val isExceeded: Boolean // > 100%
)

object FinancialCalculations {

    /**
     * Remaining Money = Income - Total Expenses
     */
    fun calculateRemainingMoney(income: Double, totalExpenses: Double): Double {
        return income - totalExpenses
    }

    /**
     * Savings = Income - Total Expenses
     */
    fun calculateSavings(income: Double, totalExpenses: Double): Double {
        return income - totalExpenses
    }

    /**
     * Savings Rate = (Savings / Income) * 100
     */
    fun calculateSavingsRate(savings: Double, income: Double): Double {
        if (income <= 0.0) return 0.0
        val rate = (savings / income) * 100.0
        return rate.coerceIn(0.0, 100.0)
    }

    /**
     * Budget Remaining = Budget - Actual Expenses
     */
    fun calculateBudgetRemaining(budget: Double, actualExpenses: Double): Double {
        return budget - actualExpenses
    }

    /**
     * Budget Utilization = (Actual Expenses / Budget) * 100
     */
    fun calculateBudgetUtilization(actualExpenses: Double, budget: Double): Double {
        if (budget <= 0.0) return 0.0
        return (actualExpenses / budget) * 100.0
    }

    /**
     * Emergency Fund Coverage = Emergency Fund / Essential Monthly Expenses (in months)
     */
    fun calculateEmergencyFundCoverage(emergencyFund: Double, essentialExpenses: Double): Double {
        if (essentialExpenses <= 0.0) return 0.0
        return emergencyFund / essentialExpenses
    }

    /**
     * Goal Progress = (Current Amount / Target Amount) * 100
     */
    fun calculateGoalProgress(currentAmount: Double, targetAmount: Double): Double {
        if (targetAmount <= 0.0) return 0.0
        val progress = (currentAmount / targetAmount) * 100.0
        return progress.coerceAtLeast(0.0)
    }

    fun computeCategoryStatus(categoryName: String, budget: Double, spent: Double): CategoryBudgetStatus {
        val remaining = budget - spent
        val percentage = if (budget > 0) (spent / budget) * 100.0 else 0.0
        return CategoryBudgetStatus(
            categoryName = categoryName,
            budgetAmount = budget,
            spentAmount = spent,
            remainingAmount = remaining,
            percentageUsed = percentage,
            isWarning = percentage >= 80.0 && percentage <= 100.0,
            isExceeded = percentage > 100.0
        )
    }

    fun computeDashboardMetrics(
        salary: Double,
        expenses: Double,
        totalBudget: Double
    ): DashboardMetrics {
        val remaining = calculateRemainingMoney(salary, expenses)
        val saved = calculateSavings(salary, expenses).coerceAtLeast(0.0)
        val savingsRate = calculateSavingsRate(saved, salary)
        val budgetUsed = calculateBudgetUtilization(expenses, totalBudget)
        val isOver = expenses > salary

        val warning = when {
            isOver -> "Your expenses are higher than your income this month."
            budgetUsed > 100 -> "You have exceeded your total monthly budget."
            budgetUsed >= 80 -> "Warning: You have used ${String.format("%.0f", budgetUsed)}% of your monthly budget."
            else -> null
        }

        return DashboardMetrics(
            monthlySalary = salary,
            totalExpenses = expenses,
            totalSaved = saved,
            remainingMoney = remaining,
            savingsRate = savingsRate,
            budgetUsed = budgetUsed,
            isExpensesOverIncome = isOver,
            topWarning = warning
        )
    }
}
