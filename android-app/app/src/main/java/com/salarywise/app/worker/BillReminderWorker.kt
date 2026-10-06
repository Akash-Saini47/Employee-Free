package com.salarywise.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.salarywise.app.data.local.SalaryWiseDatabase
import com.salarywise.app.domain.model.CurrencyFormatter
import kotlinx.coroutines.flow.firstOrNull

class BillReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = SalaryWiseDatabase.getDatabase(context)
        val user = db.userDao().getCurrentUser() ?: return Result.success()

        if (!user.isNotificationsEnabled) {
            return Result.success()
        }

        val threeDaysFromNow = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000)
        val upcoming = db.recurringExpenseDao().getUpcomingBills(user.id, threeDaysFromNow)

        if (upcoming.isNotEmpty()) {
            val bill = upcoming.first()
            showNotification(
                title = "Upcoming Bill Due: ${bill.name}",
                message = "${CurrencyFormatter.formatInr(bill.amount)} is due soon. Open SalaryWise to review and pay."
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "salarywise_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bills & Budget Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies about upcoming recurring bills and budget limits"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }
}
