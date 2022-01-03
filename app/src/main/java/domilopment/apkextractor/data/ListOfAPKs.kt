package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ListOfAPKs private constructor(private val packageManager: PackageManager) {
    //Static Singleton Constructor
    companion object {
        private lateinit var INSTANCE: ListOfAPKs
        operator fun invoke(packageManager: PackageManager): ListOfAPKs {
            if (!::INSTANCE.isInitialized)
                INSTANCE = ListOfAPKs(packageManager)
            return INSTANCE
        }
    }

    // Check lists to hold Data
    private val isEmpty
        get() = systemApps.isEmpty() && updatedSystemApps.isEmpty() && userApps.isEmpty()

    //Lists of APK Types
    val userApps = ArrayList<ApplicationModel>()
    val systemApps = ArrayList<ApplicationModel>()
    val updatedSystemApps = ArrayList<ApplicationModel>()

    // initialize APK list
    init {
        if (isEmpty) {
            updateData()
        }
    }

    /**
     * Update Installed APK lists
     */
    fun updateData() {
        // Ensure all list are Empty!
        userApps.clear()
        systemApps.clear()
        updatedSystemApps.clear()
        // Fill each list with its specific type
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .forEach { packageInfo: ApplicationInfo ->
                ApplicationModel(
                    packageInfo,
                    packageManager
                ).also {
                    when {
                        (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ->
                            updatedSystemApps.add(it)
                        (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM ->
                            systemApps.add(it)
                        else ->
                            userApps.add(it)
                    }
                }
            }
    }
}
