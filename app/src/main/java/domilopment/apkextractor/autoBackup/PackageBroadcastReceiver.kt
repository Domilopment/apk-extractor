package domilopment.apkextractor.autoBackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import domilopment.apkextractor.FileHelper
import domilopment.apkextractor.R
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.Application

class PackageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            // Check for App Updates
            Intent.ACTION_PACKAGE_REPLACED ->
                intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                // Check if Updated App is in Backup List
                if (
                    SettingsManager(context).listOfAutoBackupApps()!!.contains(packageName)
                ) {
                    val pendingResult: PendingResult = goAsync()
                    val asyncTask = Task(pendingResult, context, packageName)
                    asyncTask.execute()
                }
            }

            // Foreground Notification Button Call
            AutoBackupService.ACTION_STOP_SERVICE -> {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("auto_backup", false).apply()
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
        }
    }

    /**
     * Async Task to ensure Backup isn't killed by System when Broadcast finishes
     * @param pendingResult
     * @param context get Broadcast context
     * @param packageName Packagename of Updated App
     */
    private class Task(
        private val pendingResult: PendingResult,
        private val context: Context,
        packageName: String
    ) : AsyncTask<String, Int, Unit>() {
        private var success = false
        // Get Application Info from Package
        private val app =
            Application(
                context.packageManager.getPackageInfo(packageName, 0).applicationInfo,
                context.packageManager
            )

        override fun doInBackground(vararg params: String?) {
            val path = SettingsManager(context).saveDir()

            // Try to Backup App
            success = try {
                FileHelper(context).copy(
                    app.appSourceDirectory,
                    path!!,
                    SettingsManager(context).appName(app)
                )
            } catch (e: Exception) {
                Log.e("Apk Extractor: AutoBackupService", e.toString())
                false
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            // Let User know when App is or should be Updated
            if (success)
                Toast.makeText(context, context.getString(R.string.auto_backup_broadcast_receiver_backup_success, app.appName), Toast.LENGTH_LONG).show()
            else
                Toast.makeText(context, context.getString(R.string.auto_backup_broadcast_receiver_backup_failed, app.appName), Toast.LENGTH_LONG).show()

            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish()
        }
    }
}