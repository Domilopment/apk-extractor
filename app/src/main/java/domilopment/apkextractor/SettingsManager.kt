package domilopment.apkextractor

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class SettingsManager(
    context: Context
) {
    private val packageManager = context.packageManager
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun selectedAppTypes(): List<Application>{
        val mData: ArrayList<Application> = ArrayList()
        if (sharedPreferences.getBoolean("updated_system_apps", false)) {
            mData.addAll(ListofAPKs(packageManager).updatedSystemApps)
            if (sharedPreferences.getBoolean("system_apps", false))
                mData.addAll(ListofAPKs(packageManager).systemApps)
        }
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListofAPKs(packageManager).userApps)
        return sortData(mData)
    }

    fun saveDir(): String {
        return sharedPreferences.getString("dir", null).toString() + '/'
    }

    fun sortData(data : List<Application>): List<Application> {
        Log.e("sorted", sharedPreferences.getInt("app_sort", 0).toString())
        when (sharedPreferences.getInt("app_sort", 0)) {
            1 -> Collections.sort(data, Comparator.comparing(Application::appPackageName))
            else -> Collections.sort(data, Comparator.comparing(Application::appName))
        }
        return data
    }
}