package com.salarywise.app.domain.model

data class FinancialFreedomMetrics(
    val emergencyFundAmount: Double,
    val essentialMonthlyExpenses: Double,
    val monthsCovered: Double,
    val savingsRate: Double,
    val totalSavings: Double,
    val monthlySavings: Double,
    val totalDebtEmi: Double,
    val debtToIncomeRatio: Double,
    val stabilityStage: String,
    val stageDescription: String,
    val explanationText: String,
    val recommendation: String
)

object FinancialFreedomCalculator {
    fun calculate(
        emergencyFund: Double,
        essentialExpenses: Double,
        savingsRate: Double,
        totalSavings: Double,
        monthlySalary: Double,
        debtEmi: Double
    ): FinancialFreedomMetrics {
        val months = if (essentialExpenses > 0) emergencyFund / essentialExpenses else 0.0
        val dti = if (monthlySalary > 0) (debtEmi / monthlySalary) * 100.0 else 0.0

        val (stage, stageDesc) = when {
            months >= 12.0 -> Pair("High Resilience", "Your emergency fund covers a full year of living expenses. You possess substantial financial security.")
            months >= 6.0 -> Pair("Financial Stability", "You have established a solid 6+ months safety cushion against unexpected life disruptions.")
            months >= 3.0 -> Pair("Essential Security", "Your safety net covers 3-6 months. Keep adding until you reach the 6-month benchmark.")
            months >= 1.0 -> Pair("Starter Buffer", "You have begun building an initial buffer. Continue prioritizing essential savings.")
            else -> Pair("Vulnerable", "You have less than 1 month of emergency cushion. Prioritize building an emergency fund.")
        }

        val explanation = "Your emergency fund currently covers approximately ${String.format("%.1f", months)} months of essential expenses."

        val rec = when {
            dti > 40.0 -> "Your Debt/EMI commitments represent ${String.format("%.0f", dti)}% of salary. Prioritize paying down high-interest liabilities."
            months < 3.0 -> "Focus spare funds on reaching at least 3 months (${CurrencyFormatter.formatInr(essentialExpenses * 3)}) of living expenses."
            months < 6.0 -> "Keep steadily depositing into your emergency fund to unlock full 6-month stability."
            else -> "Safety net is solid. You can now aggressively fund mid-to-long term wealth goals."
        }

        return FinancialFreedomMetrics(
            emergencyFundAmount = emergencyFund,
            essentialMonthlyExpenses = essentialExpenses,
            monthsCovered = months,
            savingsRate = savingsRate,
            totalSavings = totalSavings,
            monthlySavings = (monthlySalary - (essentialExpenses + debtEmi)).coerceAtLeast(0.0),
            totalDebtEmi = debtEmi,
            debtToIncomeRatio = dti,
            stabilityStage = stage,
            stageDescription = stageDesc,
            explanationText = explanation,
            recommendation = rec
        )
    }
}
