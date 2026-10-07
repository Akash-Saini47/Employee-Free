package com.salarywise.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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

        /**
         * Repairs legacy/partial onboarding state during migration.
         *
         * Older builds could persist the user before all dependent records were
         * created. A completed user without a salary, budget, categories, or
         * emergency-fund goal is therefore treated as an incomplete profile.
         */
        private fun repairLegacyOnboardingState(db: SupportSQLiteDatabase) {
            db.execSQL("""
                UPDATE users
                SET isOnboardingCompleted = 0
                WHERE isOnboardingCompleted = 1
                  AND (
                    NOT EXISTS (
                        SELECT 1 FROM salary_records s WHERE s.userId = users.id
                    )
                    OR NOT EXISTS (
                        SELECT 1 FROM budgets b WHERE b.userId = users.id
                    )
                    OR NOT EXISTS (
                        SELECT 1
                        FROM budget_categories bc
                        INNER JOIN budgets b ON b.id = bc.budgetId
                        WHERE b.userId = users.id
                    )
                    OR NOT EXISTS (
                        SELECT 1
                        FROM savings_goals sg
                        WHERE sg.userId = users.id AND sg.isEmergencyFund = 1
                    )
                  )
            """.trimIndent())

            db.execSQL("""
                DELETE FROM savings_contributions
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM budget_categories
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM salary_records
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM budgets
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM expenses
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM recurring_expenses
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM savings_goals
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())
            db.execSQL("""
                DELETE FROM financial_reports
                WHERE userId NOT IN (SELECT id FROM users)
            """.trimIndent())

            // Remove dependent data belonging to an incomplete profile so
            // onboarding can safely rebuild it as one atomic transaction.
            db.execSQL("""
                DELETE FROM savings_contributions
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM budget_categories
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM salary_records
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM budgets
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM expenses
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM recurring_expenses
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM savings_goals
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
            db.execSQL("""
                DELETE FROM financial_reports
                WHERE userId IN (
                    SELECT id FROM users WHERE isOnboardingCompleted = 0
                )
            """.trimIndent())
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                repairLegacyOnboardingState(db)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                repairLegacyOnboardingState(db)
            }
        }

        fun getDatabase(context: Context): SalaryWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                fun buildDatabase(): SalaryWiseDatabase =
                    Room.databaseBuilder(
                        appContext,
                        SalaryWiseDatabase::class.java,
                        DATABASE_NAME
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .fallbackToDestructiveMigration()
                        .build()

                val instance = try {
                    buildDatabase().also { it.openHelper.writableDatabase }
                } catch (_: Exception) {
                    // If a legacy database is physically corrupt or cannot be
                    // migrated, remove only that local database and recreate it.
                    // This prevents the Android "Something went wrong" crash
                    // loop on startup.
                    try {
                        INSTANCE?.close()
                    } catch (_: Exception) {
                    }
                    appContext.deleteDatabase(DATABASE_NAME)
                    buildDatabase().also { it.openHelper.writableDatabase }
                }

                INSTANCE = instance
                instance
            }
        }
    }
}
