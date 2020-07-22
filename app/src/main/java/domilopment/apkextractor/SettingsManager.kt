package domilopment.apkextractor

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.loader.content.AsyncTaskLoader
import androidx.preference.PreferenceManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class SettingsManager(
    context: Context
) : AsyncTaskLoader<List<Application>>(context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val packageManager = context.packageManager

    companion object {
        const val DATA_LOADER_ID = 42
    }

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(): List<Application>{
        val mData: ArrayList<Application> = ArrayList()
        if (sharedPreferences.getBoolean("updated_system_apps", false)) {
            mData.addAll(ListOfAPKs(packageManager).updatedSystemApps)
            if (sharedPreferences.getBoolean("system_apps", false))
                mData.addAll(ListOfAPKs(packageManager).systemApps)
        }
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListOfAPKs(packageManager).userApps)
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
            0 -> Collections.sort(data, Comparator.comparing(Application::appName))
            1 -> Collections.sort(data, Comparator.comparing(Application::appPackageName))
            2 -> Collections.sort(data, Comparator.comparing(Application::appInstallTime).reversed())
            3 -> Collections.sort(data, Comparator.comparing(Application::appUpdateTime).reversed())
            else -> throw Exception("No such sort type")
        }
        return data
    }

    fun changeUIMode(newValue: String = sharedPreferences.getString("list_preference_ui_mode", "0")!!) {
        when (newValue.toInt()) {
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Loading data in Background
     */
    override fun loadInBackground(): List<Application> {
        ListOfAPKs(packageManager).updateData()
        return selectedAppTypes()
    }

    /**
     * Force load on Start
     */
    override fun onStartLoading() {
        forceLoad()
    }
}