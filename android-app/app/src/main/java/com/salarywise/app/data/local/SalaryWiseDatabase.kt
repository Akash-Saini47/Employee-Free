package com.salarywise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salarywise.app.data.local.dao.*
import com.salarywise.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        SalaryRecordEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        ExpenseEntity::class,
        RecurringExpenseEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        FinancialReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalaryWiseDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun salaryDao(): SalaryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun savingsDao(): SavingsDao
    abstract fun financialReportDao(): FinancialReportDao

    companion object {
        @Volatile
        private var INSTANCE: SalaryWiseDatabase? = null

        fun getDatabase(context: Context): SalaryWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalaryWiseDatabase::class.java,
                    "salarywise_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
