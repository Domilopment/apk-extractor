package domilopment.apkextractor.data.sources

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.model.appList.AppModel
import domilopment.apkextractor.utils.NonComparingMutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ListOfApps private constructor(context: Context) {
    private val packageManager = context.packageManager

    private val _apps: NonComparingMutableStateFlow<MutableList<AppModel>> =
        NonComparingMutableStateFlow(mutableListOf())

    val apps: Flow<List<AppModel>> = _apps.asStateFlow()
    val systemApps: Flow<List<AppModel.SystemApp>> =
        _apps.map { it.filterIsInstance<AppModel.SystemApp>() }
    val updatedSystemApps: Flow<List<AppModel.UpdatedSystemApps>> =
        _apps.map { it.filterIsInstance<AppModel.UpdatedSystemApps>() }
    val userApps: Flow<List<AppModel.UserApp>> =
        _apps.map { it.filterIsInstance<AppModel.UserApp>() }

    // initialize APK list
    init {
        runBlocking { updateData() }
    }

    /**
     * Update Installed APK lists
     */
    suspend fun updateData() = withContext(Dispatchers.Default) {
        // Ensure all list are Empty!
        val newApps = mutableListOf<AppModel>()

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

        applicationsInfo.forEach { applicationInfo: ApplicationInfo ->
            when {
                (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP -> newApps.add(
                    AppModel.UpdatedSystemApps(applicationInfo)
                )

                (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM -> newApps.add(
                    AppModel.SystemApp(applicationInfo)
                )

                else -> newApps.add(AppModel.UserApp(applicationInfo))
            }
        }

        _apps.value = newApps
    }

    suspend fun add(app: AppModel) = withContext(Dispatchers.IO) {
        _apps.update { apps ->
            val newApps = apps.toMutableList()
            val element: AppModel? =
                apps.find { it.applicationInfo.packageName == app.applicationInfo.packageName }

            if (element == null) {
                newApps.add(app)
            } else {
                newApps.remove(element)
                newApps.add(app)
            }

            newApps
        }
    }

    suspend fun remove(app: AppModel) = withContext(Dispatchers.IO) {
        _apps.update { apps ->
            val element: AppModel =
                apps.find { it.applicationInfo.packageName == app.applicationInfo.packageName }
                    ?: return@withContext
            if (element is AppModel.SystemApp) return@withContext

            val newApps = apps.toMutableList()

            if (element is AppModel.UpdatedSystemApps || element is AppModel.UserApp) {
                newApps.remove(element)
                if (element is AppModel.UpdatedSystemApps) newApps.add(AppModel.SystemApp(element.applicationInfo))
            }

            newApps
        }
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
