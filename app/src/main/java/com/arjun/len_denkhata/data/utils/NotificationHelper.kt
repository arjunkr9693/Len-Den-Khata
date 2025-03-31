package com.arjun.len_denkhata.data.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arjun.len_denkhata.MainActivity
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat
) {
    companion object {
        const val TRANSACTION_CHANNEL_ID = "transactions_channel"
        const val TRANSACTION_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TRANSACTION_CHANNEL_ID,
                "Transaction Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows notifications for new transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTransactionNotification(transaction: CustomerTransactionEntity) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TRANSACTION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("New Transaction")
            .setContentText("${if (transaction.isCredit) "Credit" else "Debit"} of ${transaction.amount}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(
                transaction.hashCode(), // Unique ID for each notification
                notification
            )
        }
    }
}