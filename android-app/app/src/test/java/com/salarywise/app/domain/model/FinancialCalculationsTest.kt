package com.salarywise.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {
    @Test fun savingsRate_isCalculatedFromSurplus() {
        assertEquals(25.0, FinancialCalculations.calculateSavingsRate(10000.0, 40000.0), 0.001)
    }

    @Test fun savingsRate_isZeroWhenIncomeIsZero() {
        assertEquals(0.0, FinancialCalculations.calculateSavingsRate(1000.0, 0.0), 0.001)
    }

    @Test fun budgetStatus_marksEightypPercentAsWarning() {
        val status = FinancialCalculations.computeCategoryStatus("Food", 10000.0, 8000.0)
        assertTrue(status.isWarning)
        assertTrue(!status.isExceeded)
    }

    @Test fun budgetStatus_marksOverBudgetAsExceeded() {
        val status = FinancialCalculations.computeCategoryStatus("Food", 10000.0, 10001.0)
        assertTrue(status.isExceeded)
        assertTrue(!status.isWarning)
    }
}
