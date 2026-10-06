package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.salarywise.app.data.local.entity.SavingsContributionEntity
import com.salarywise.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY isEmergencyFund DESC, createdAt ASC")
    fun getAllGoalsFlow(userId: String): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY isEmergencyFund DESC, createdAt ASC")
    suspend fun getAllGoals(userId: String): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND isEmergencyFund = 1 LIMIT 1")
    fun getEmergencyFundGoalFlow(userId: String): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND isEmergencyFund = 1 LIMIT 1")
    suspend fun getEmergencyFundGoal(userId: String): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND id = :goalId LIMIT 1")
    suspend fun getGoalById(userId: String, goalId: String): SavingsGoalEntity?

    @Query("SELECT COALESCE(SUM(currentAmount), 0.0) FROM savings_goals WHERE userId = :userId")
    fun getTotalSavedAmountFlow(userId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(currentAmount), 0.0) FROM savings_goals WHERE userId = :userId")
    suspend fun getTotalSavedAmount(userId: String): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: String)

    // Contributions
    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun getContributionsForGoalFlow(goalId: String): Flow<List<SavingsContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingsContributionEntity)

    @Transaction
    suspend fun addContributionAndUpdateGoal(contribution: SavingsContributionEntity) {
        insertContribution(contribution)
        val goal = getGoalById(contribution.userId, contribution.goalId)
        if (goal != null) {
            val newAmount = goal.currentAmount + contribution.amount
            updateGoal(goal.copy(currentAmount = newAmount, isCompleted = newAmount >= goal.targetAmount))
        }
    }

    @Query("DELETE FROM savings_goals WHERE userId = :userId")
    suspend fun deleteAllGoals(userId: String)
}
