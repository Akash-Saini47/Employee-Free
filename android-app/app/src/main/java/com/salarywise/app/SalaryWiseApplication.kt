package com.salarywise.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.salarywise.app.data.local.SalaryWiseDatabase
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.worker.BillReminderWorker
import java.util.concurrent.TimeUnit

class SalaryWiseApplication : Application() {

    lateinit var database: SalaryWiseDatabase
        private set

    lateinit var repository: SalaryWiseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = SalaryWiseDatabase.getDatabase(this)
        repository = SalaryWiseRepository(database)

        scheduleBillReminders()
    }

    private fun scheduleBillReminders() {
        val reminderRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "salarywise_bill_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}
