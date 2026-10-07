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
    // v5 intentionally invalidates all legacy local databases. Previous
    // releases used schema version 1 and then shipped an incompatible
    // migration/recovery attempt. Keeping the same schema version would make
    // an already-installed broken DB survive an update.
    //
    // This app stores local financial data only. A clean v5 database is safer
    // than repeatedly opening a potentially partial/corrupt legacy database.
    version = 5,
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
        private const val DATABASE_NAME = "salarywise_database.db"

        @Volatile
        private var INSTANCE: SalaryWiseDatabase? = null

        fun getDatabase(context: Context): SalaryWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: createDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        private fun createDatabase(context: Context): SalaryWiseDatabase {
            return Room.databaseBuilder(
                context,
                SalaryWiseDatabase::class.java,
                DATABASE_NAME
            )
                // Any database from versions 1–4 is intentionally discarded.
                // This guarantees that the broken/partial legacy onboarding
                // state cannot be reopened after updating the app.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
