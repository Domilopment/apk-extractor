package domilopment.apkextractor

import android.app.Application
import android.content.res.Configuration
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
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
        // Set Material You Colors
        DynamicColors.applyToActivitiesIfAvailable(this)
        runBlocking {
            // Set UI Mode
            SettingsManager.changeUIMode(prefs.nightMode.first())
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