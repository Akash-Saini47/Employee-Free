package com.salarywise.app.domain.model

data class HealthScoreFactor(
    val title: String,
    val isPositive: Boolean,
    val points: Int,
    val description: String
)

data class FinancialHealthScoreResult(
    val score: Int, // 0 to 100
    val rating: String, // Excellent, Good, Fair, Needs Attention, Critical
    val positiveFactors: List<HealthScoreFactor>,
    val negativeFactors: List<HealthScoreFactor>,
    val disclaimer: String = "The Financial Health Score is an automated algorithmic indicator to help you track financial discipline. It is not professional financial or investment advice."
)

object FinancialHealthScoreCalculator {

    fun calculateScore(
        savingsRate: Double,
        budgetUtilization: Double,
        emergencyFundMonths: Double,
        isExpensesOverIncome: Boolean,
        hasEmergencyFundGoal: Boolean,
        discretionarySpikeCategory: String? = null
    ): FinancialHealthScoreResult {
        var totalPoints = 0
        val positives = mutableListOf<HealthScoreFactor>()
        val negatives = mutableListOf<HealthScoreFactor>()

        // 1. Savings Rate (Max 30 points)
        when {
            savingsRate >= 35.0 -> {
                totalPoints += 30
                positives.add(HealthScoreFactor("High Savings Rate", true, 30, "You are saving ${String.format("%.1f", savingsRate)}% of your salary, well above recommended benchmarks."))
            }
            savingsRate >= 20.0 -> {
                totalPoints += 22
                positives.add(HealthScoreFactor("Good Savings Rate", true, 22, "Saving ${String.format("%.1f", savingsRate)}% of your monthly in-hand income."))
            }
            savingsRate >= 10.0 -> {
                totalPoints += 14
                positives.add(HealthScoreFactor("Moderate Savings Rate", true, 14, "Saving ${String.format("%.1f", savingsRate)}%. Try aiming for at least 20%."))
            }
            savingsRate > 0.0 -> {
                totalPoints += 6
                negatives.add(HealthScoreFactor("Low Savings Rate", false, -8, "Savings rate is under 10%. Consider cutting discretionary expenses."))
            }
            else -> {
                totalPoints += 0
                negatives.add(HealthScoreFactor("Zero or Negative Savings", false, -15, "No money saved this month."))
            }
        }

        // 2. Budget Adherence (Max 25 points)
        when {
            budgetUtilization == 0.0 -> {
                totalPoints += 15 // No budget set or no expenses yet
            }
            budgetUtilization in 1.0..80.0 -> {
                totalPoints += 25
                positives.add(HealthScoreFactor("Budget Well Maintained", true, 25, "Expenses are under 80% of your allocated monthly budget."))
            }
            budgetUtilization in 80.01..100.0 -> {
                totalPoints += 18
                positives.add(HealthScoreFactor("Budget Close to Limit", true, 18, "Used ${String.format("%.0f", budgetUtilization)}% of budget. Stay vigilant towards month end."))
            }
            else -> {
                totalPoints += 5
                negatives.add(HealthScoreFactor("Budget Exceeded", false, -10, "Total expenses exceeded monthly budget by ${(budgetUtilization - 100).toInt()}%."))
            }
        }

        // 3. Emergency Fund Progress (Max 25 points)
        when {
            emergencyFundMonths >= 6.0 -> {
                totalPoints += 25
                positives.add(HealthScoreFactor("Robust Emergency Cushion", true, 25, "Emergency fund covers over 6 months of essential living expenses."))
            }
            emergencyFundMonths >= 3.0 -> {
                totalPoints += 18
                positives.add(HealthScoreFactor("Emergency Fund Growing", true, 18, "Covers ${String.format("%.1f", emergencyFundMonths)} months of essential expenses (target: 6 months)."))
            }
            emergencyFundMonths >= 1.0 -> {
                totalPoints += 10
                positives.add(HealthScoreFactor("Initial Buffer Built", true, 10, "Covers ${String.format("%.1f", emergencyFundMonths)} month. Continue building toward 3-6 months."))
            }
            else -> {
                totalPoints += 2
                negatives.add(HealthScoreFactor("Insufficient Emergency Buffer", false, -8, "Emergency fund covers less than 1 month of essential expenses."))
            }
        }

        // 4. Income vs Expenses Consistency (Max 10 points)
        if (isExpensesOverIncome) {
            negatives.add(HealthScoreFactor("Expenses Exceed Income", false, -12, "This month's expenses are higher than your salary."))
        } else {
            totalPoints += 10
            positives.add(HealthScoreFactor("Living Within Means", true, 10, "Total spending remains strictly within your monthly income."))
        }

        // 5. Goal & Discretionary Control (Max 10 points)
        if (hasEmergencyFundGoal) {
            totalPoints += 10
            positives.add(HealthScoreFactor("Active Financial Goals", true, 10, "Clear savings and emergency fund targets established."))
        } else {
            totalPoints += 4
            negatives.add(HealthScoreFactor("No Emergency Target Set", false, -4, "Set an emergency target to safeguard your future."))
        }

        if (discretionarySpikeCategory != null) {
            totalPoints = (totalPoints - 5).coerceAtLeast(0)
            negatives.add(HealthScoreFactor("Spike in $discretionarySpikeCategory", false, -5, "Higher than usual spending detected in $discretionarySpikeCategory."))
        }

        val finalScore = totalPoints.coerceIn(0, 100)
        val rating = when {
            finalScore >= 85 -> "Excellent"
            finalScore >= 70 -> "Good"
            finalScore >= 50 -> "Fair"
            finalScore >= 30 -> "Needs Attention"
            else -> "Critical"
        }

        return FinancialHealthScoreResult(
            score = finalScore,
            rating = rating,
            positiveFactors = positives,
            negativeFactors = negatives
        )
    }
}
