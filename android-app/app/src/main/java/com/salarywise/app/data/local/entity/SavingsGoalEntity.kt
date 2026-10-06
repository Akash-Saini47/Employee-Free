package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String, // Emergency Fund, New Laptop, Vacation, Car, Education, House, Custom Goal
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val monthlyContribution: Double = 0.0,
    val category: String = "General", // Emergency, Gadget, Travel, Vehicle, Education, RealEstate, Other
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val isEmergencyFund: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
