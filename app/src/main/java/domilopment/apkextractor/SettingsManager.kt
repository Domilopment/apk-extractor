package domilopment.apkextractor

import android.content.Context
import androidx.preference.PreferenceManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListofAPKs
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class SettingsManager(
    context: Context
) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun selectedAppTypes(): List<Application>{
        val mData: ArrayList<Application> = ArrayList()
        if (sharedPreferences.getBoolean("updated_system_apps", false)) {
            mData.addAll(ListofAPKs.updatedSystemApps)
            if (sharedPreferences.getBoolean("system_apps", false))
                mData.addAll(ListofAPKs.systemApps)
        }
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListofAPKs.userApps)
        return sortData(mData)
    }

    fun saveDir(): String {
        return sharedPreferences.getString("dir", null).toString() + '/'
    }

    fun sortData(data : List<Application>): List<Application> {
        when (sharedPreferences.getInt("app_sort", 0)) {
            1 -> Collections.sort(data, Comparator.comparing(Application::appPackageName))
            else -> Collections.sort(data, Comparator.comparing(Application::appName))
        }
        return data
    }
}