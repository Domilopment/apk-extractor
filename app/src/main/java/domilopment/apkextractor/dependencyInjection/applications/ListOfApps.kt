package domilopment.apkextractor.dependencyInjection.applications

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.utils.NonComparingMutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class ListOfApps private constructor(context: Context) {
    private val packageManager = context.packageManager

    private val _apps: NonComparingMutableStateFlow<Triple<MutableList<ApplicationModel>, MutableList<ApplicationModel>, MutableList<ApplicationModel>>> =
        NonComparingMutableStateFlow(
            Triple(mutableListOf(), mutableListOf(), mutableListOf())
        )

    val apps: Flow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        _apps.asStateFlow()

    // initialize APK list
    init {
        runBlocking { updateData() }
    }

    /**
     * Update Installed APK lists
     */
    suspend fun updateData() {
        // Ensure all list are Empty!
        val newUpdatedSystemApps = mutableListOf<ApplicationModel>()
        val newSystemApps = mutableListOf<ApplicationModel>()
        val newUserApps = mutableListOf<ApplicationModel>()

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
                packageManager = packageManager, appPackageName = packageInfo.packageName,
            ).also {
                when {
                    (it.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP -> newUpdatedSystemApps.add(
                        it
                    )

                    (it.appFlags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM -> newSystemApps.add(
                        it
                    )

                    else -> newUserApps.add(it)
                }
            }
        }

        _apps.value = Triple(newUpdatedSystemApps, newSystemApps, newUserApps)
    }

    suspend fun add(app: ApplicationModel) {
        val apps = _apps.value.copy()
        val updatedSysApps = apps.first.toMutableList()
        val sysApps = apps.second.toMutableList()
        val userApps = apps.third.toMutableList()

        when (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) {
            (app.appFlags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) -> {
                if (updatedSysApps.find { it.appPackageName == app.appPackageName } == null) updatedSysApps.add(
                    app
                )
                sysApps.removeIf { it.appPackageName == app.appPackageName }
            }

            else -> if (userApps.find { it.appPackageName == app.appPackageName } == null) userApps.add(
                app
            )
        }

        _apps.value = Triple(updatedSysApps, sysApps, userApps)
    }

    suspend fun remove(app: ApplicationModel) {
        val apps = _apps.value.copy()
        val updatedSysApps = apps.first.toMutableList()
        val sysApps = apps.second.toMutableList()
        val userApps = apps.third.toMutableList()

        if (updatedSysApps.removeIf { it.appPackageName == app.appPackageName }) sysApps.add(app)
        else userApps.removeIf { it.appPackageName == app.appPackageName }

        _apps.value = Triple(updatedSysApps, sysApps, userApps)
    }

    companion object {
        private lateinit var INSTANCE: ListOfApps

        fun getApplications(context: Context): ListOfApps {
            synchronized(ListOfApps::class.java) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = ListOfApps(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }
}
