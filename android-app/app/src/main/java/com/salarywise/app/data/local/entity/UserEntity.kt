package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String = "",
    val monthlyInHandSalary: Double = 0.0,
    val salaryDate: Int = 1,
    val currentSavings: Double = 0.0,
    val monthlyEssentialExpenses: Double = 0.0,
    val monthlySavingsTarget: Double = 0.0,
    val emergencyFundTarget: Double = 0.0,
    val currencySymbol: String = "₹",
    val dateFormat: String = "dd/MM/yyyy",
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val pinHash: String? = null,
    val isBiometricEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
