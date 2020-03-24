package domilopment.apkextractor

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListofAPKs(packageManager: PackageManager) {
    private val packages: List<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    val userApps: ArrayList<Application> = ArrayList()
    val systemApps: ArrayList<Application> = ArrayList()
    val updatedSystemApps: ArrayList<Application> = ArrayList()

    init {
        packages.forEach { packageInfo: ApplicationInfo ->
            val app = Application(
                packageInfo,
                packageManager
            )
            when {
                (packageInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 ->
                    updatedSystemApps.add(app)
                (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ->
                    systemApps.add(app)
                else ->
                    userApps.add(app)
            }
        }
    }
}