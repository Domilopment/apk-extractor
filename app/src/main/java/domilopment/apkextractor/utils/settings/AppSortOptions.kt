package domilopment.apkextractor.utils.settings

import domilopment.apkextractor.data.ApplicationModel

enum class AppSortOptions {
    SORT_BY_NAME {
        override fun comparator(asc: Boolean) = if (asc) compareBy(
            String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName
        ) else compareByDescending(
            String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName
        )
    },
    SORT_BY_PACKAGE {
        override fun comparator(asc: Boolean) = if (asc) compareBy(
            String.CASE_INSENSITIVE_ORDER, ApplicationModel::appPackageName
        ) else compareByDescending(
            String.CASE_INSENSITIVE_ORDER, ApplicationModel::appPackageName
        )
    },
    SORT_BY_INSTALL_TIME {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationModel::appInstallTime) else compareByDescending(
                ApplicationModel::appInstallTime
            )
    },
    SORT_BY_UPDATE_TIME {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationModel::appUpdateTime) else compareByDescending(
                ApplicationModel::appUpdateTime
            )
    },
    SORT_BY_APK_SIZE {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationModel::apkSize) else compareByDescending(ApplicationModel::apkSize)
    };

    abstract fun comparator(asc: Boolean): Comparator<ApplicationModel>

    companion object {
        private val apkSortOptionsArray =
            AppSortOptions.values().associateBy { it.ordinal }.withDefault { SORT_BY_NAME }
        operator fun get(sortMode: Int): AppSortOptions {
            return apkSortOptionsArray.getValue(sortMode)
        }
    }
}