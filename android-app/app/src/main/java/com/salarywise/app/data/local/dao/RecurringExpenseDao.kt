package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salarywise.app.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRecurringExpensesFlow(userId: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId ORDER BY nextDueDate ASC")
    fun getAllRecurringExpensesFlow(userId: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId AND isActive = 1 AND nextDueDate <= :upToTimestamp ORDER BY nextDueDate ASC")
    fun getUpcomingBillsFlow(userId: String, upToTimestamp: Long): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId AND isActive = 1 AND nextDueDate <= :upToTimestamp ORDER BY nextDueDate ASC")
    suspend fun getUpcomingBills(userId: String, upToTimestamp: Long): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getRecurringExpenseById(userId: String, id: String): RecurringExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity)

    @Update
    suspend fun updateRecurringExpense(expense: RecurringExpenseEntity)

    @Delete
    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteRecurringExpenseById(id: String)

    @Query("DELETE FROM recurring_expenses WHERE userId = :userId")
    suspend fun deleteAllRecurringExpenses(userId: String)
}
