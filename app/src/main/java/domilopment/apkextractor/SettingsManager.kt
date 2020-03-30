package domilopment.apkextractor

import android.content.Context
import androidx.preference.PreferenceManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class SettingsManager(
    context: Context
) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(): List<Application>{
        val mData: ArrayList<Application> = ArrayList()
        if (sharedPreferences.getBoolean("updated_system_apps", false)) {
            mData.addAll(ListOfAPKs.updatedSystemApps)
            if (sharedPreferences.getBoolean("system_apps", false))
                mData.addAll(ListOfAPKs.systemApps)
        }
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListOfAPKs.userApps)
        return sortData(mData)
    }

    /**
     * Gives back in SharedPreferences Saved Directory Path
     * @return Saved Directory Path
     */
    fun saveDir(): String {
        return sharedPreferences.getString("dir", null).toString() + '/'
    }

    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of APKs
     * @return Sorted List of APKs
     */
    fun sortData(data : List<Application>): List<Application> {
        when (sharedPreferences.getInt("app_sort", 0)) {
            1 -> Collections.sort(data, Comparator.comparing(Application::appPackageName))
            2 -> Collections.sort(data, Comparator.comparing(Application::appInstallTime).reversed())
            3 -> Collections.sort(data, Comparator.comparing(Application::appUpdateTime).reversed())
            else -> Collections.sort(data, Comparator.comparing(Application::appName))
        }
        return data
    }
}