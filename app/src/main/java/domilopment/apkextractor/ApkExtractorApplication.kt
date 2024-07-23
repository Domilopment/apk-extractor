package domilopment.apkextractor

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.database.CursorWindow
import dagger.hilt.android.HiltAndroidApp
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class ApkExtractorApplication : Application() {
    @Inject
    lateinit var prefs: PreferenceRepository

    @SuppressLint("DiscouragedPrivateApi")
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

        // Increase the CursorWindow size to 100 MB
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field[null] = 100 * 1024 * 1024
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runBlocking {
            // Set UI Mode
            SettingsManager.changeUIMode(prefs.nightMode.first())
        }
    }
}