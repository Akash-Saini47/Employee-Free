package com.salarywise.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.salarywise.app.data.local.SalaryWiseDatabase
import com.salarywise.app.data.repository.SalaryWiseRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SalaryWiseOnboardingDatabaseTest {

    private lateinit var database: SalaryWiseDatabase
    private lateinit var repository: SalaryWiseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            SalaryWiseDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = SalaryWiseRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completeOnboardingCreatesCompleteFinancialPlan() = runBlocking {
        val user = repository.completeOnboarding(
            name = "Test User",
            monthlySalary = 40_000.0,
            salaryDate = 1,
            currentSavings = 15_000.0,
            essentialExpenses = 20_000.0,
            monthlySavingsTarget = 12_000.0,
            emergencyFundTarget = 120_000.0
        )

        assertTrue(user.isOnboardingCompleted)
        assertEquals("Test User", user.name)
        assertEquals(40_000.0, user.monthlyInHandSalary, 0.001)

        val savedUser = repository.getCurrentUser()
        assertEquals(user.id, savedUser?.id)

        val salaries = database.salaryDao()
            .getAllSalariesFlow(user.id)
            .firstValue()
        assertEquals(1, salaries.size)

        val budgets = database.budgetDao()
            .getAllBudgetsFlow(user.id)
            .firstValue()
        assertEquals(1, budgets.size)

        val categories = database.budgetDao()
            .getCategoriesForBudget(budgets.first().id)
        assertEquals(14, categories.size)

        val goals = database.savingsDao().getAllGoals(user.id)
        assertEquals(1, goals.size)
        assertTrue(goals.first().isEmergencyFund)
    }

    @Test
    fun failedSecondOnboardingCannotCreateDuplicateCompletedProfile() = runBlocking {
        repository.completeOnboarding(
            name = "First User",
            monthlySalary = 40_000.0,
            salaryDate = 1,
            currentSavings = 15_000.0,
            essentialExpenses = 20_000.0,
            monthlySavingsTarget = 12_000.0,
            emergencyFundTarget = 120_000.0
        )

        var failed = false
        try {
            repository.completeOnboarding(
                name = "Second User",
                monthlySalary = 50_000.0,
                salaryDate = 5,
                currentSavings = 10_000.0,
                essentialExpenses = 25_000.0,
                monthlySavingsTarget = 10_000.0,
                emergencyFundTarget = 150_000.0
            )
        } catch (_: IllegalStateException) {
            failed = true
        }

        assertTrue(failed)
        assertEquals("First User", repository.getCurrentUser()?.name)
    }
}

private fun <T> kotlinx.coroutines.flow.Flow<T>.firstValue(): T =
    kotlinx.coroutines.flow.first(this)
