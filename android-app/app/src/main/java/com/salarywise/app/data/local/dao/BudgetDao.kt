package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.salarywise.app.data.local.entity.BudgetCategoryEntity
import com.salarywise.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    fun getBudgetForMonthFlow(userId: String, monthYear: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    suspend fun getBudgetForMonth(userId: String, monthYear: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY monthYear DESC")
    fun getAllBudgetsFlow(userId: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    // Category methods
    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId ORDER BY categoryName ASC")
    fun getCategoriesForBudgetFlow(budgetId: String): Flow<List<BudgetCategoryEntity>>

    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId ORDER BY categoryName ASC")
    suspend fun getCategoriesForBudget(budgetId: String): List<BudgetCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<BudgetCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: BudgetCategoryEntity)

    @Update
    suspend fun updateCategory(category: BudgetCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: BudgetCategoryEntity)

    @Query("DELETE FROM budget_categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgets(userId: String)
}
