package domilopment.apkextractor.utils.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions

object SettingsManager {
    /**
     * Switch ui mode (System, Light, Dark) either with given Parameter or with saved Preference
     * @param newValue Int castable String value to switch ui mode
     */
    fun changeUIMode(newValue: Int) {
        when (newValue) {
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Tells an Activity if AutoBackupService should be started
     * @return true if Service isn't running and should be started
     */
    fun shouldStartService(prefStartService: Boolean): Boolean {
        val service = AutoBackupService.isRunning
        return prefStartService and !service
    }

    /**
     * Set App Locale Language for Tag and apply
     * @param locale String Tag of Locale
     */
    fun setLocale(locale: String) {
        val appLocale: LocaleListCompat =
            if (locale == "default") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(locale)
        // Call this on the main thread as it may require Activity.restart()
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Get selected swipe actions from settings
     * @param preferenceValue value string from multiselect preference
     * @return ApkActionsOptions enum with information for selected option or null
     */
    fun getSwipeActionByPreferenceValue(preferenceValue: String?): ApkActionsOptions? {
        return ApkActionsOptions.entries.firstOrNull { it.preferenceValue == preferenceValue }
    }
}