package domilopment.apkextractor

import android.content.SharedPreferences
import android.content.pm.PackageManager

class SettingsManager(
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {
    fun selectedAppTypes(): List<Application>{
        val mData: ArrayList<Application> = ArrayList()
        if (sharedPreferences.getBoolean("system_apps", false))
            mData.addAll(ListofAPKs(packageManager).systemApps)
        if (sharedPreferences.getBoolean("updated_system_apps", false))
            mData.addAll(ListofAPKs(packageManager).updatedSystemApps)
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListofAPKs(packageManager).userApps)
        return mData
    }
}