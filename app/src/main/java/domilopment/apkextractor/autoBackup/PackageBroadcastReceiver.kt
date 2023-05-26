package domilopment.apkextractor.autoBackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.DocumentsContract
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import domilopment.apkextractor.autoBackup.AsyncBackupTask.Companion.ACTION_DELETE_APK
import domilopment.apkextractor.utils.settings.SettingsManager

class PackageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Check for App Updates
            Intent.ACTION_PACKAGE_REPLACED ->
                intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                    // Check if Updated App is in Backup List
                    if (
                        SettingsManager(context).listOfAutoBackupApps()!!.contains(packageName)
                    ) {
                        val pendingResult: PendingResult = goAsync()
                        val asyncTask = AsyncBackupTask(pendingResult, context, packageName)
                        asyncTask.execute()
                    }
                }

            // Foreground Notification Button Call
            AutoBackupService.ACTION_STOP_SERVICE -> {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean("auto_backup", false).apply()
                context.stopService(Intent(context, AutoBackupService::class.java))
            }

            // Restart Service on Device Boot
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                if (SettingsManager(context).shouldStartService()) {
                    context.startForegroundService(Intent(context, AutoBackupService::class.java))
                }
            }

            // Restart Service if it is killed
            AutoBackupService.ACTION_RESTART_SERVICE -> {
                context.startForegroundService(Intent(context, AutoBackupService::class.java))
            }

            // Delete Backup APK file
            ACTION_DELETE_APK -> {
                intent.data?.let {
                    val deleted = DocumentsContract.deleteDocument(
                        context.contentResolver,
                        it
                    )
                    if (deleted) with(NotificationManagerCompat.from(context)) {
                        val id = intent.getIntExtra("ID", -1)
                        if (id > 1) cancel(id)
                    }
                }
            }
        }
    }
}