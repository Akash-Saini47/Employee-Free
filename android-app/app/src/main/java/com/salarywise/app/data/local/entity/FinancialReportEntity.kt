package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_reports",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["userId", "monthYear"], unique = true)]
)
data class FinancialReportEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val monthYear: String, // "yyyy-MM", e.g. "2026-10"
    val income: Double,
    val totalExpenses: Double,
    val totalSavings: Double,
    val savingsRate: Double,
    val budgetUsed: Double,
    val topCategory: String,
    val largestExpenseAmount: Double,
    val largestExpenseName: String,
    val expenseChangePercent: Double = 0.0,
    val savingsChangePercent: Double = 0.0,
    val healthScore: Int = 0,
    val summaryText: String = "",
    val generatedAt: Long = System.currentTimeMillis()
)
