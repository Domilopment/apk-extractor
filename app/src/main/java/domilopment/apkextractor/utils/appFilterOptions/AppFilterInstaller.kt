package domilopment.apkextractor.utils.appFilterOptions

import android.content.Context
import android.content.pm.PackageManager
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.Utils

enum class AppFilterInstaller(private val packageName: String?) : AppFilter {
    GOOGLE("com.android.vending"),
    SAMSUNG("com.sec.android.app.samsungapps"),
    AMAZON("com.amazon.venezia"),
    OTHERS(null) {
        override fun getFilter(list: List<ApplicationModel.ApplicationListModel>): List<ApplicationModel.ApplicationListModel> {
            return list.filter { it.installationSource !in Utils.listOfKnownStores }
        }

        override fun getTitleString(context: Context): CharSequence {
            return context.getString(R.string.other_sources)
        }
    };

    override fun getFilter(list: List<ApplicationModel.ApplicationListModel>): List<ApplicationModel.ApplicationListModel> {
        return list.filter { it.installationSource == packageName }
    }

    override fun getTitleString(context: Context): CharSequence? {
        val packageManager = context.packageManager
        return try {
            packageName?.let {
                Utils.getApplicationInfo(packageManager, packageName).loadLabel(packageManager)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}