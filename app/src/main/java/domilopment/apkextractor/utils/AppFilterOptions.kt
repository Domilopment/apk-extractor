package domilopment.apkextractor.utils

import domilopment.apkextractor.data.ApplicationModel

enum class AppFilterOptions {
    FAVORITES {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.isFavorite }
        }
    },
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
    };

    abstract fun getFilter(list: List<ApplicationModel>): List<ApplicationModel>
}