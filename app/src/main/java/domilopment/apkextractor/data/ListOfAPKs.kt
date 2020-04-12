package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListOfAPKs(private val packageManager: PackageManager) {
    //Static List of APKs
    companion object Apps {
        private val userApps = ArrayList<Application>()
        private val systemApps = ArrayList<Application>()
        private val updatedSystemApps = ArrayList<Application>()
        private val isEmpty
            get() = Apps.systemApps.isEmpty() || Apps.updatedSystemApps.isEmpty() || Apps.userApps.isEmpty()
    }

    //Lists of APK Types
    val userApps: List<Application>
        get() = Apps.userApps
    val systemApps: List<Application>
        get() = Apps.systemApps
    val updatedSystemApps: List<Application>
        get() = Apps.updatedSystemApps

    // initialize APK list
    init {
        if (isEmpty) {
            // Ensure all list are Empty!
            Apps.userApps.clear()
            Apps.systemApps.clear()
            Apps.updatedSystemApps.clear()
            // Fill each list with its specific type
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .forEach { packageInfo: ApplicationInfo ->
                    Application(
                        packageInfo,
                        packageManager
                    ).also {
                        when {
                            (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ->
                                Apps.updatedSystemApps.add(it)
                            (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM ->
                                Apps.systemApps.add(it)
                            else ->
                                Apps.userApps.add(it)
                        }
                    }
                }
        }
    }
}
