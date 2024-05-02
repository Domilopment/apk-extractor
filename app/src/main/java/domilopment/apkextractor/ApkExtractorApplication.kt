package domilopment.apkextractor

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class ApkExtractorApplication : Application() {
    @Inject
    lateinit var prefs: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            // Set UI Mode
            SettingsManager.changeUIMode(prefs.nightMode.first())
        }

        // Create Notification Channel
        val channel = NotificationChannel(
            AutoBackupService.CHANNEL_ID,
            "App Update Watching Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        // Open Channel with Notification Service
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runBlocking {
            // Set UI Mode
            SettingsManager.changeUIMode(prefs.nightMode.first())
        }
    }
}