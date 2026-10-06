package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["userId", "nextDueDate"])]
)
data class RecurringExpenseEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String, // e.g. Rent, EMI, Netflix, Internet, Electricity
    val amount: Double,
    val categoryName: String,
    val frequency: String = "MONTHLY", // WEEKLY, MONTHLY, QUARTERLY, YEARLY
    val nextDueDate: Long, // timestamp
    val isActive: Boolean = true,
    val autoDebitAlert: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
