package domilopment.apkextractor.autoBackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import domilopment.apkextractor.autoBackup.AsyncBackupTask.Companion.ACTION_DELETE_APK
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Check for App Updates
            Intent.ACTION_PACKAGE_REPLACED -> intent.data?.encodedSchemeSpecificPart?.let { packageName ->
                val autoBackupList = runBlocking {
                    preferenceRepository.autoBackupAppList.first()
                }
                val appSaveName = runBlocking {
                    preferenceRepository.appSaveName.first()
                }
                val saveDir = runBlocking {
                    preferenceRepository.saveDir.first()
                }
                val extractXapk = runBlocking {
                    preferenceRepository.backupModeXapk.first()
                }
                // Check if Updated App is in Backup List
                if (autoBackupList.contains(packageName) && saveDir != null) {
                    val pendingResult: PendingResult = goAsync()
                    val asyncTask = AsyncBackupTask(
                        pendingResult, context, appSaveName, saveDir, extractXapk, packageName
                    )
                    asyncTask.execute()
                }
            }

            // Foreground Notification Button Call
            AutoBackupService.ACTION_STOP_SERVICE -> {
                runBlocking { preferenceRepository.setAutoBackupService(false) }
                context.stopService(Intent(context, AutoBackupService::class.java))
            }

            // Restart Service on Device Boot
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                val isBackupService = runBlocking {
                    preferenceRepository.autoBackupService.first()
                }

                if (SettingsManager.shouldStartService(isBackupService)) {
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
                    val deleted = FileUtil.deleteDocument(context, it)
                    if (deleted) with(NotificationManagerCompat.from(context)) {
                        val id = intent.getIntExtra("ID", -1)
                        if (id > 1) cancel(id)
                    }
                }
            }
        }
    }
}