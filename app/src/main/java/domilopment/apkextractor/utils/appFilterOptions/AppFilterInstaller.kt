package domilopment.apkextractor.utils.appFilterOptions

import android.content.pm.ApplicationInfo
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.utils.Utils

enum class AppFilterInstaller : AppFilter {
    GOOGLE {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.installationSource == "com.android.vending" }
        }
    },
    SAMSUNG {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.installationSource == "com.sec.android.app.samsungapps" }
        }
    },
    AMAZON {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.installationSource == "com.amazon.venezia" }
        }
    },
    OTHERS {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.installationSource !in Utils.listOfKnownStores }
        }
    },
    GAMES {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.appCategory == ApplicationInfo.CATEGORY_GAME }
        }
    };
}