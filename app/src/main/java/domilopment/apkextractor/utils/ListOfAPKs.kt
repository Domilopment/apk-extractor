package domilopment.apkextractor.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.ApplicationModel

class ListOfAPKs(private val packageManager: PackageManager) {
    //Lists of APK Types
    val userApps = ArrayList<ApplicationModel>()
    val systemApps = ArrayList<ApplicationModel>()
    val updatedSystemApps = ArrayList<ApplicationModel>()
    val apps get() = Triple(updatedSystemApps, systemApps, userApps)

    // initialize APK list
    init {
        updateData()
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
        val applicationsInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    PackageManager.GET_META_DATA.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        applicationsInfo.forEach { packageInfo: ApplicationInfo ->
            ApplicationModel(
                packageManager, packageInfo.packageName
            ).also {
                when {
                    (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ->
                        updatedSystemApps.add(it)
                    (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM ->
                        systemApps.add(it)
                    else -> userApps.add(it)
                }
            }
        }
    }
}
