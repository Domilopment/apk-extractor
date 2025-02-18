package domilopment.apkextractor.data.model.appList

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Class holding information for a specific Application installed on device
 * @param appPackageName the package name of an app, we want to access information about
 * @param isChecked to save and hande a selection made by user about multiple app instances
 * @param isFavorite to save and handle if the app is of higher priority to the user to perform action on or retrieve Information
 */
data class ApplicationModel(
    val appPackageName: String,
    val appName: String,
    val appSourceDirectory: String,
    val appSplitSourceDirectories: Array<String>?,
    val appIcon: Drawable,
    val appVersionName: String,
    val appVersionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val appFlags: Int,
    val appCategory: Int,
    val appInstallTime: Long,
    val appUpdateTime: Long,
    val apkSize: Float,
    val launchIntent: Intent?,
    val installationSource: String?,
    val isChecked: Boolean = false,
    val isFavorite: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicationModel

        if (appPackageName != other.appPackageName) return false
        if (appName != other.appName) return false
        if (appSourceDirectory != other.appSourceDirectory) return false
        if (!appSplitSourceDirectories.contentEquals(other.appSplitSourceDirectories)) return false
        if (appIcon != other.appIcon) return false
        if (appVersionName != other.appVersionName) return false
        if (appVersionCode != other.appVersionCode) return false
        if (minSdkVersion != other.minSdkVersion) return false
        if (targetSdkVersion != other.targetSdkVersion) return false
        if (appFlags != other.appFlags) return false
        if (appCategory != other.appCategory) return false
        if (appInstallTime != other.appInstallTime) return false
        if (appUpdateTime != other.appUpdateTime) return false
        if (apkSize != other.apkSize) return false
        if (launchIntent != other.launchIntent) return false
        if (installationSource != other.installationSource) return false
        if (isChecked != other.isChecked) return false
        if (isFavorite != other.isFavorite) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appPackageName.hashCode()
        result = 31 * result + (appName.hashCode())
        result = 31 * result + (appSourceDirectory.hashCode())
        result = 31 * result + (appSplitSourceDirectories.contentHashCode())
        result = 31 * result + appIcon.hashCode()
        result = 31 * result + (appVersionName.hashCode())
        result = 31 * result + appVersionCode.hashCode()
        result = 31 * result + (minSdkVersion)
        result = 31 * result + (targetSdkVersion)
        result = 31 * result + (appFlags)
        result = 31 * result + (appCategory)
        result = 31 * result + appInstallTime.hashCode()
        result = 31 * result + appUpdateTime.hashCode()
        result = 31 * result + apkSize.hashCode()
        result = 31 * result + (launchIntent.hashCode())
        result = 31 * result + (installationSource.hashCode())
        result = 31 * result + isChecked.hashCode()
        result = 31 * result + isFavorite.hashCode()
        return result
    }
}