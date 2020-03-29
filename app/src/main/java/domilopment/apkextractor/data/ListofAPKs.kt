package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListofAPKs() {
    companion object {
        val userApps: ArrayList<Application> = ArrayList()
        val systemApps: ArrayList<Application> = ArrayList()
        val updatedSystemApps: ArrayList<Application> = ArrayList()
    }

    fun init(packageManager: PackageManager) {
        val packages: List<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
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