package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salary_records",
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
data class SalaryRecordEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val monthYear: String, // format "yyyy-MM", e.g. "2026-04"
    val grossSalary: Double,
    val deductions: Double = 0.0,
    val inHandSalary: Double, // grossSalary - deductions
    val paymentDate: Long, // timestamp
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
