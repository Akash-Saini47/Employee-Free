package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_categories",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["budgetId"]), Index(value = ["budgetId", "categoryName"], unique = true)]
)
data class BudgetCategoryEntity(
    @PrimaryKey
    val id: String,
    val budgetId: String,
    val userId: String,
    val categoryName: String,
    val allocatedAmount: Double,
    val isCustom: Boolean = false,
    val iconName: String = "category"
)
