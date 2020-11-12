package domilopment.apkextractor.autoBackup

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.R

class AutoBackupService : Service() {
    companion object {
        private const val CHANNEL_ID = "domilopment.apkextractor.AUTO_BACKUP_SERVICE"
        const val ACTION_STOP_SERVICE = "domilopment.apkextractor.STOP_AUTO_BACKUP_SERVICE"
        // Check for Service is Running
        var isRunning = false
    }

    private lateinit var br: BroadcastReceiver

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Create Broadcast Receiver for App Updates
        br = PackageBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start Foreground Service with Notification
        startForeground(1, createNotification())

        // Set Filter for Broadcast
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        // Start Broadcast Receiver
        registerReceiver(br, filter)

        isRunning = true

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        unregisterReceiver(br)
        stopForeground(true)
    }

    /**
     * Creates Notification
     * @return Notification
     * Returns a Notification ready to use
     */
    private fun createNotification(): Notification {
        // Create Notification Channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Update Watching Service",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        // Open Channel with Notification Service
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)

        // Call MainActivity an Notification Click
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        // Stop Foreground Service on Button Click
        val stopPendingIntent: PendingIntent =
            Intent(this, PackageBroadcastReceiver::class.java).apply {
                action = ACTION_STOP_SERVICE
            }.let { stopIntent ->
                PendingIntent.getBroadcast(this, 0, stopIntent, 0)
            }


        // Build and return Notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Watching for Updates")
            .setContentText("Watching for Updates on Selected Packages")
            .setSmallIcon(R.drawable.ic_small_notification_icon_24)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_small_notification_icon_24, "STOP SERVICE", stopPendingIntent)
            .build()
    }
}