package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salarywise.app.data.local.entity.FinancialReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialReportDao {
    @Query("SELECT * FROM financial_reports WHERE userId = :userId ORDER BY monthYear DESC")
    fun getAllReportsFlow(userId: String): Flow<List<FinancialReportEntity>>

    @Query("SELECT * FROM financial_reports WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    fun getReportForMonthFlow(userId: String, monthYear: String): Flow<FinancialReportEntity?>

    @Query("SELECT * FROM financial_reports WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    suspend fun getReportForMonth(userId: String, monthYear: String): FinancialReportEntity?

    @Query("SELECT * FROM financial_reports WHERE userId = :userId ORDER BY monthYear DESC LIMIT 1")
    suspend fun getLatestReport(userId: String): FinancialReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FinancialReportEntity)

    @Update
    suspend fun updateReport(report: FinancialReportEntity)

    @Delete
    suspend fun deleteReport(report: FinancialReportEntity)

    @Query("DELETE FROM financial_reports WHERE userId = :userId")
    suspend fun deleteAllReports(userId: String)
}
