package domilopment.apkextractor.autoBackup

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.R
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicInteger

class AutoBackupService : Service() {
    enum class Actions {
        START, STOP
    }

    companion object {
        const val CHANNEL_ID = "domilopment.apkextractor.AUTO_BACKUP_SERVICE"
        const val ACTION_STOP_SERVICE = "domilopment.apkextractor.STOP_AUTO_BACKUP_SERVICE"

        // Check for Service is Running
        var isRunning = false

        // Create unique notification IDs
        private val c: AtomicInteger = AtomicInteger(1)
        fun getNewNotificationID() = c.incrementAndGet()
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
        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) restartService()
    }

    private fun startService() {
        isRunning = true

        // Start Foreground Service with Notification
        ServiceCompat.startForeground(
            this,
            1,
            createNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )

        // Set Filter for Broadcast
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        // Start Broadcast Receiver
        registerReceiver(br, filter)
    }

    private fun stopService() {
        try {
            unregisterReceiver(br)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        isRunning = false
    }

    /**
     * Restarts Service even if Phone is in dose mode
     */
    private fun restartService() {
        // restart Pending Intent
        val restartServicePendingIntent: PendingIntent = Intent(
            applicationContext, AutoBackupService::class.java
        ).apply {
            action = Actions.START.name
            setPackage(packageName)
        }.let { restartIntent ->
            PendingIntent.getService(
                this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // System Alarm Manager
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Set Alarm to be executed
        alarmService.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent,
        )
    }

    /**
     * Creates Notification
     * @return Notification
     * Returns a Notification ready to use
     */
    private fun createNotification(): Notification {
        // Call MainActivity an Notification Click
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        // Stop Foreground Service on Button Click
        val stopPendingIntent: PendingIntent =
            Intent(this, PackageBroadcastReceiver::class.java).apply {
                action = ACTION_STOP_SERVICE
            }.let { stopIntent ->
                PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        // Build and return Notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.auto_backup_notification_title))
            .setContentText(getString(R.string.auto_backup_notification_content_text))
            .setSmallIcon(R.drawable.ic_small_notification_icon_24)
            .setColor(getColor(R.color.notificationColor)).setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_small_notification_icon_24,
                getString(R.string.auto_backup_notification_action_stop),
                stopPendingIntent
            ).setOngoing(true).build()
    }
}