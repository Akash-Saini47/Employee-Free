package com.salarywise.app.domain.model

enum class InsightType {
    INFO, WARNING, SUCCESS, ALERT
}

data class FinancialInsight(
    val id: String,
    val title: String,
    val message: String,
    val type: InsightType,
    val actionText: String? = null,
    val category: String? = null
)

object SmartInsightsEngine {

    fun generateInsights(
        currentSalary: Double,
        currentExpenses: Double,
        prevExpenses: Double,
        currentSavings: Double,
        prevSavings: Double,
        categoryBudgets: List<CategoryBudgetStatus>,
        highestDiscretionaryCategory: String?,
        emergencyMonths: Double
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()

        // 1. Deficit check
        if (currentExpenses > currentSalary && currentSalary > 0) {
            insights.add(
                FinancialInsight(
                    id = "insight_deficit",
                    title = "Overspending Alert",
                    message = "Your expenses are higher than your income this month (${CurrencyFormatter.formatInr(currentExpenses)} vs ${CurrencyFormatter.formatInr(currentSalary)}).",
                    type = InsightType.ALERT
                )
            )
        }

        // 2. Savings growth check
        if (prevSavings > 0 && currentSavings > prevSavings) {
            val increasePercent = ((currentSavings - prevSavings) / prevSavings) * 100.0
            insights.add(
                FinancialInsight(
                    id = "insight_savings_growth",
                    title = "Savings Boosted!",
                    message = "Your savings increased from ${CurrencyFormatter.formatInr(prevSavings)} to ${CurrencyFormatter.formatInr(currentSavings)} (+${String.format("%.1f", increasePercent)}%).",
                    type = InsightType.SUCCESS
                )
            )
        }

        // 3. Category budget near limit or exceeded
        for (cb in categoryBudgets) {
            if (cb.isExceeded) {
                insights.add(
                    FinancialInsight(
                        id = "budget_exceeded_${cb.categoryName}",
                        title = "${cb.categoryName} Budget Exceeded",
                        message = "You have exceeded your ${cb.categoryName} budget (${CurrencyFormatter.formatInr(cb.spentAmount)} of ${CurrencyFormatter.formatInr(cb.budgetAmount)}).",
                        type = InsightType.ALERT,
                        category = cb.categoryName
                    )
                )
            } else if (cb.isWarning) {
                insights.add(
                    FinancialInsight(
                        id = "budget_warning_${cb.categoryName}",
                        title = "${cb.categoryName} Near Limit",
                        message = "You have used ${String.format("%.0f", cb.percentageUsed)}% of your ${cb.categoryName} budget.",
                        type = InsightType.WARNING,
                        category = cb.categoryName
                    )
                )
            }
        }

        // 4. Highest discretionary spending
        if (!highestDiscretionaryCategory.isNullOrBlank()) {
            insights.add(
                FinancialInsight(
                    id = "insight_discretionary",
                    title = "Discretionary Spending",
                    message = "$highestDiscretionaryCategory is your highest discretionary expense this month.",
                    type = InsightType.INFO,
                    category = highestDiscretionaryCategory
                )
            )
        }

        // 5. Emergency fund status
        if (emergencyMonths < 3.0 && emergencyMonths > 0) {
            insights.add(
                FinancialInsight(
                    id = "insight_emergency_cushion",
                    title = "Emergency Cushion",
                    message = "Your emergency fund currently covers approximately ${String.format("%.1f", emergencyMonths)} months of essential expenses. Target at least 6 months.",
                    type = InsightType.INFO
                )
            )
        } else if (emergencyMonths >= 6.0) {
            insights.add(
                FinancialInsight(
                    id = "insight_emergency_secure",
                    title = "Emergency Fund Safe",
                    message = "Great job! Your emergency fund covers ${String.format("%.1f", emergencyMonths)} months of living costs.",
                    type = InsightType.SUCCESS
                )
            )
        }

        // 6. General savings rate praise
        val savingsRate = if (currentSalary > 0) ((currentSalary - currentExpenses) / currentSalary) * 100.0 else 0.0
        if (savingsRate >= 30.0 && currentExpenses <= currentSalary) {
            insights.add(
                FinancialInsight(
                    id = "insight_healthy_rate",
                    title = "High Discipline",
                    message = "Your savings rate is ${String.format("%.1f", savingsRate)}% this month. You are maintaining excellent financial momentum.",
                    type = InsightType.SUCCESS
                )
            )
        }

        return insights
    }
}
