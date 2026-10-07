package com.salarywise.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salarywise.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET pinHash = :pinHash WHERE id = :userId")
    suspend fun updatePin(userId: String, pinHash: String?)

    @Query("UPDATE users SET isDarkMode = :isDarkMode WHERE id = :userId")
    suspend fun updateDarkMode(userId: String, isDarkMode: Boolean)

    @Query("UPDATE users SET isNotificationsEnabled = :enabled WHERE id = :userId")
    suspend fun updateNotifications(userId: String, enabled: Boolean)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
