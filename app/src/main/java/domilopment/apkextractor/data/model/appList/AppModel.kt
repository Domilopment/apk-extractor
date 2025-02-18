package domilopment.apkextractor.data.model.appList

import android.content.pm.ApplicationInfo

sealed class AppModel(open val applicationInfo: ApplicationInfo) {
    data class SystemApp(override val applicationInfo: ApplicationInfo): AppModel(applicationInfo)
    data class UpdatedSystemApps(override val applicationInfo: ApplicationInfo): AppModel(applicationInfo)
    data class UserApp(override val applicationInfo: ApplicationInfo): AppModel(applicationInfo)
}