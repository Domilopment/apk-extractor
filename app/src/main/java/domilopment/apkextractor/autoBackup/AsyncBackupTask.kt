package domilopment.apkextractor.autoBackup

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.FileUtil
import kotlinx.coroutines.*
import java.io.FileNotFoundException

/**
 * Async Task to ensure Backup isn't killed by System when Broadcast finishes
 * @param pendingResult
 * @param context get Broadcast context
 * @param packageName Package name of Updated App
 */
class AsyncBackupTask(
    private val pendingResult: BroadcastReceiver.PendingResult,
    private val context: Context,
    private val appName: Set<String>,
    private val saveDir: Uri,
    private val extractXapk: Boolean,
    packageName: String
) : CoroutineScope by GlobalScope {
    companion object {
        private const val CHANNEL_ID = "domilopment.apkextractor.BROADCAST_RECEIVER"
        const val ACTION_DELETE_APK = "domilopment.apkextractor.DELETE_APK"
    }

    private val mainDispatcher get() = Dispatchers.Main

    // Get Application Info from Package
    private val app = ApplicationModel(
        context.packageManager, packageName
    )

    fun execute(dispatcher: CoroutineDispatcher = Dispatchers.Default) {
        launch(mainDispatcher) {
            val doInBackground = async(dispatcher) {
                doInBackground()
            }
            withContext(mainDispatcher) {
                onPostExecute(doInBackground.await())
            }
        }
    }

    private suspend fun backup(): SaveApkResult {
        val splits = arrayListOf(app.appSourceDirectory)
        if (!app.appSplitSourceDirectories.isNullOrEmpty() && extractXapk) splits.addAll(app.appSplitSourceDirectories!!)

        val name = ApplicationUtil.appName(app, appName)
        return if (splits.size == 1) {
            ApplicationUtil.saveApk(context, app.appSourceDirectory, saveDir, name)
        } else {
            ApplicationUtil.saveXapk(context, splits.toTypedArray(), saveDir, name) {}
        }
    }

    private suspend fun doInBackground(): Uri? {
        // Try to Backup App
        return try {
            when (val result = backup()) {
                is SaveApkResult.Success -> result.uri
                is SaveApkResult.Failure -> null
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun onPostExecute(result: Uri?) {
        // Let User know when App is or should be Updated
        createNotificationChannel()
        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            result?.let {
                // notificationId is a unique int for each notification that you must define
                val id = AutoBackupService.getNewNotificationID()
                notify(id, createNotificationSuccess(id, it).build())
            } ?: run {
                // notificationId is a unique int for each notification that you must define
                notify(
                    AutoBackupService.getNewNotificationID(), createNotificationFailed().build()
                )
            }
        }
        // Must call finish() so the BroadcastReceiver can be recycled.
        pendingResult.finish()
    }


    /**
     * Create notification Channel for auto Backup Apk results
     */
    private fun createNotificationChannel() {
        // Create Notification Channel
        val channel = NotificationChannel(
            CHANNEL_ID, "App Backup Created", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lightColor = R.attr.colorPrimary
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        // Open Channel with Notification Service
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    /**
     * Creates Notification
     * @return Notification
     * Returns a Notification for Backup Apk
     */
    private fun createNotificationSuccess(
        notificationID: Int, fileUri: Uri
    ): NotificationCompat.Builder {
        // Call MainActivity an Notification Click
        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

        // Share APK on Button Click
        val sharePendingIntent: PendingIntent = Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = FileUtil.FileInfo.APK.mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
            }, context.getString(R.string.share_intent_title)
        ).let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        // Delete APK on Button Click
        val deletePendingIntent: PendingIntent =
            Intent(context, PackageBroadcastReceiver::class.java).apply {
                action = ACTION_DELETE_APK
                data = fileUri
                putExtra("ID", notificationID)
            }.let { stopIntent ->
                PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        // Build and return Notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.auto_backup_broadcast_receiver_notification_title))
            .setContentText(
                context.getString(
                    R.string.auto_backup_broadcast_receiver_backup_success, app.appName
                )
            ).setSmallIcon(R.drawable.ic_small_notification_icon_24)
            .setColor(context.getColor(R.color.notificationColor)).setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_small_notification_icon_24,
                context.getString(R.string.action_bottom_sheet_share),
                sharePendingIntent
            ).addAction(
                R.drawable.ic_small_notification_icon_24,
                context.getString(R.string.alert_apk_selected_delete),
                deletePendingIntent
            ).setAutoCancel(true)
    }

    /**
     * Creates Notification
     * @return Notification
     * Returns a Notification if Backup Apk failed
     */
    private fun createNotificationFailed(): NotificationCompat.Builder {
        // Call MainActivity an Notification Click
        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

        // Build and return Notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.auto_backup_broadcast_receiver_notification_title))
            .setContentText(
                context.getString(
                    R.string.auto_backup_broadcast_receiver_backup_failed, app.appName
                )
            ).setSmallIcon(R.drawable.ic_small_notification_icon_24)
            .setColor(context.getColor(R.color.notificationColor)).setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }
}