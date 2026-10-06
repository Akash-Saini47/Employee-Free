package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salarywise.app.data.local.entity.SalaryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryDao {
    @Query("SELECT * FROM salary_records WHERE userId = :userId ORDER BY monthYear DESC")
    fun getAllSalariesFlow(userId: String): Flow<List<SalaryRecordEntity>>

    @Query("SELECT * FROM salary_records WHERE userId = :userId ORDER BY monthYear ASC")
    fun getSalaryGrowthFlow(userId: String): Flow<List<SalaryRecordEntity>>

    @Query("SELECT * FROM salary_records WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    fun getSalaryForMonthFlow(userId: String, monthYear: String): Flow<SalaryRecordEntity?>

    @Query("SELECT * FROM salary_records WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    suspend fun getSalaryForMonth(userId: String, monthYear: String): SalaryRecordEntity?

    @Query("SELECT * FROM salary_records WHERE userId = :userId ORDER BY monthYear DESC LIMIT 1")
    suspend fun getLatestSalary(userId: String): SalaryRecordEntity?

    @Query("SELECT * FROM salary_records WHERE userId = :userId ORDER BY monthYear DESC LIMIT 1")
    fun getLatestSalaryFlow(userId: String): Flow<SalaryRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalary(salary: SalaryRecordEntity)

    @Update
    suspend fun updateSalary(salary: SalaryRecordEntity)

    @Delete
    suspend fun deleteSalary(salary: SalaryRecordEntity)

    @Query("DELETE FROM salary_records WHERE id = :id")
    suspend fun deleteSalaryById(id: String)

    @Query("DELETE FROM salary_records WHERE userId = :userId")
    suspend fun deleteAllSalaries(userId: String)
}
