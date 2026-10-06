package com.salarywise.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"]), Index(value = ["userId"])]
)
data class SavingsContributionEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val userId: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
