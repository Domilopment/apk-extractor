package domilopment.apkextractor.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.preference.PreferenceManager
import domilopment.apkextractor.data.ApplicationModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ListOfApps private constructor(context: Context) {
    private val packageManager = context.packageManager
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val _apps =
        MutableSharedFlow<Triple<MutableList<ApplicationModel>, MutableList<ApplicationModel>, MutableList<ApplicationModel>>>(
            replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val apps: Flow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        _apps.asSharedFlow()

    // initialize APK list
    init {
        updateData()
    }

    /**
     * Update Installed APK lists
     */
    fun updateData() {
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

        val favorites =
            sharedPreferences.getStringSet(Constants.PREFERENCE_KEY_FAVORITES, setOf()) ?: setOf()
        applicationsInfo.forEach { packageInfo: ApplicationInfo ->
            ApplicationModel(
                packageManager = packageManager,
                appPackageName = packageInfo.packageName,
                isFavorite = packageInfo.packageName in favorites
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

        _apps.tryEmit(Triple(newUpdatedSystemApps, newSystemApps, newUserApps))
    }

    companion object {
        private lateinit var INSTANCE: ListOfApps

        fun getApplications(context: Context): ListOfApps {
            synchronized(ListOfApps::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = ListOfApps(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }
}
