package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListOfAPKs(private val packageManager: PackageManager) {
    //Static List of APKs
    companion object {
        private val apps = ArrayList<Application>()
    }

    //Lists of APK Types
    val userApps: List<Application>
        get() = apps.filter { (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == 0 }
    val systemApps: List<Application>
        get() = apps.filter { (it.appFlags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == ApplicationInfo.FLAG_SYSTEM }
    val updatedSystemApps: List<Application>
        get() = apps.filter { (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP }

    // initialize APK list
    init {
        if (apps.isEmpty())
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .forEach { packageInfo: ApplicationInfo ->
                        Application(
                            packageInfo,
                            packageManager
                        ).also {
                            apps.add(it)
                        }
                    }
    }
}
