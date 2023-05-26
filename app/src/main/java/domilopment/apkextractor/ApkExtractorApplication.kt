package domilopment.apkextractor

import android.app.Application
import android.content.res.Configuration
import domilopment.apkextractor.utils.settings.SettingsManager

class ApkExtractorApplication: Application() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)

        // Set Material You Colors
        settingsManager.useMaterialYou(this)

        // Set UI Mode
        settingsManager.changeUIMode()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        settingsManager.changeUIMode()
    }
}