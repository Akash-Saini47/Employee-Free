package com.salarywise.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.salarywise.app.data.local.SalaryWiseDatabase
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.worker.BillReminderWorker
import java.util.concurrent.TimeUnit

class SalaryWiseApplication : Application() {

    val database: SalaryWiseDatabase by lazy {
        SalaryWiseDatabase.getDatabase(this)
    }

    val repository: SalaryWiseRepository by lazy {
        SalaryWiseRepository(database)
    }

    fun scheduleBillRemindersSafely() {
        try {
            // WorkManager auto-initialization is disabled in the manifest.
            // Initialize it only after MainActivity is running.
            try {
                WorkManager.initialize(
                    applicationContext,
                    Configuration.Builder().build()
                )
            } catch (_: IllegalStateException) {
                // Already initialized; continue to getInstance().
            }

            val reminderRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "salarywise_bill_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        } catch (_: Throwable) {
            // Reminders are non-critical. Never crash SalaryWise because
            // WorkManager cannot initialize or schedule on a device.
        }
    }

    override fun onCreate() {
        super.onCreate()
        // No Room or WorkManager initialization here.
    }
}
