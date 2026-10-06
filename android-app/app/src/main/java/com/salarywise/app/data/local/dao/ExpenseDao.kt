package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salarywise.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val categoryName: String,
    val totalAmount: Double,
    val expenseCount: Int
)

data class MonthTotal(
    val monthYear: String,
    val totalAmount: Double
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpensesFlow(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesInRangeFlow(userId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getExpensesInRange(userId: String, startDate: Long, endDate: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getExpenseById(userId: String, id: String): ExpenseEntity?

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND (:category IS NULL OR categoryName = :category)
        AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:searchQuery IS NULL OR description LIKE '%' || :searchQuery || '%' OR categoryName LIKE '%' || :searchQuery || '%')
        AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun filterExpensesFlow(
        userId: String,
        startDate: Long,
        endDate: Long,
        category: String? = null,
        paymentMethod: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        searchQuery: String? = null
    ): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT categoryName, SUM(amount) as totalAmount, COUNT(id) as expenseCount 
        FROM expenses 
        WHERE userId = :userId AND date >= :startDate AND date <= :endDate 
        GROUP BY categoryName 
        ORDER BY totalAmount DESC
    """)
    fun getCategoryTotalsInRangeFlow(userId: String, startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("""
        SELECT categoryName, SUM(amount) as totalAmount, COUNT(id) as expenseCount 
        FROM expenses 
        WHERE userId = :userId AND date >= :startDate AND date <= :endDate 
        GROUP BY categoryName 
        ORDER BY totalAmount DESC
    """)
    suspend fun getCategoryTotalsInRange(userId: String, startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    fun getTotalExpenseInRangeFlow(userId: String, startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpenseInRange(userId: String, startDate: Long, endDate: Long): Double

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY amount DESC LIMIT 1")
    suspend fun getHighestExpense(userId: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY amount DESC LIMIT 1")
    suspend fun getHighestExpenseInRange(userId: String, startDate: Long, endDate: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllExpenses(userId: String)
}
