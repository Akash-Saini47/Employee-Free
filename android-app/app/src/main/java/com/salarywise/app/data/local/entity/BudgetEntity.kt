package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
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
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val monthYear: String, // format "yyyy-MM", e.g. "2026-10"
    val totalBudget: Double,
    val createdAt: Long = System.currentTimeMillis()
)
