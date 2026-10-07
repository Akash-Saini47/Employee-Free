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

    /**
     * Do not open Room or initialize WorkManager from Application.onCreate().
     * Either operation can fail on a broken/legacy device state and an
     * exception here kills the entire Android process before MainActivity can
     * display a recovery UI.
     */
    val database: SalaryWiseDatabase by lazy {
        SalaryWiseDatabase.getDatabase(this)
    }

    val repository: SalaryWiseRepository by lazy {
        SalaryWiseRepository(database)
    }

    fun scheduleBillRemindersSafely() {
        try {
            val reminderRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "salarywise_bill_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        } catch (_: Throwable) {
            // Reminders are non-critical. Never crash the application because
            // WorkManager cannot initialize or schedule on a particular device.
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Intentionally no database or WorkManager initialization here.
    }
}
