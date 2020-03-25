package domilopment.apkextractor

import android.content.Context
import androidx.preference.PreferenceManager

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
        return mData
    }

    fun saveDir(): String {
        return sharedPreferences.getString("dir", null).toString() + '/'
    }
}