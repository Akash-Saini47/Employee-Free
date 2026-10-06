package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "date"]),
        Index(value = ["userId", "categoryName"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val categoryName: String,
    val date: Long, // timestamp
    val paymentMethod: String, // Cash, UPI, Debit Card, Credit Card, Bank Transfer, Other
    val description: String = "",
    val isRecurring: Boolean = false,
    val recurringExpenseId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
