package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListOfAPKs(private val packageManager: PackageManager) {
    //Static List of APKs
    companion object {
        private val staticUserApps = ArrayList<Application>()
        private val staticSystemApps = ArrayList<Application>()
        private val staticUpdatedSystemApps = ArrayList<Application>()
    }

    private val isEmpty
        get() = staticSystemApps.isEmpty() && staticUpdatedSystemApps.isEmpty() && staticUserApps.isEmpty()

    //Lists of APK Types
    val userApps: List<Application>
        get() = staticUserApps
    val systemApps: List<Application>
        get() = staticSystemApps
    val updatedSystemApps: List<Application>
        get() = staticUpdatedSystemApps

    // initialize APK list
    init {
        if (isEmpty) {
            updateData()
        }
    }

    fun updateData() {
        // Ensure all list are Empty!
        staticUserApps.clear()
        staticSystemApps.clear()
        staticUpdatedSystemApps.clear()
        // Fill each list with its specific type
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .forEach { packageInfo: ApplicationInfo ->
                Application(
                    packageInfo,
                    packageManager
                ).also {
                    when {
                        (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ->
                            staticUpdatedSystemApps.add(it)
                        (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM ->
                            staticSystemApps.add(it)
                        else ->
                            staticUserApps.add(it)
                    }
                }
            }
    }
}
