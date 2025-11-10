package domilopment.apkextractor.utils.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ApplicationModel.ApplicationListModel

enum class AppSortOptions {
    SORT_BY_NAME {
        override fun comparator(asc: Boolean) = if (asc) compareBy(
            nullsLast(String.CASE_INSENSITIVE_ORDER), ApplicationListModel::appName
        ) else compareByDescending(
            nullsLast(String.CASE_INSENSITIVE_ORDER), ApplicationListModel::appName
        )
    },
    SORT_BY_PACKAGE {
        override fun comparator(asc: Boolean) = if (asc) compareBy(
            String.CASE_INSENSITIVE_ORDER, ApplicationListModel::appPackageName
        ) else compareByDescending(
            String.CASE_INSENSITIVE_ORDER, ApplicationListModel::appPackageName
        )
    },
    SORT_BY_INSTALL_TIME {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationListModel::appInstallTime) else compareByDescending(
                ApplicationListModel::appInstallTime
            )
    },
    SORT_BY_UPDATE_TIME {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationListModel::appUpdateTime) else compareByDescending(
                ApplicationListModel::appUpdateTime
            )
    },
    SORT_BY_APK_SIZE {
        override fun comparator(asc: Boolean) =
            if (asc) compareBy(ApplicationListModel::apkSize) else compareByDescending(
                ApplicationListModel::apkSize
            )
    };

    abstract fun comparator(asc: Boolean): Comparator<ApplicationListModel>

    companion object {
        private val apkSortOptionsArray =
            entries.associateBy { it.ordinal }.withDefault { SORT_BY_NAME }

        operator fun get(sortMode: Int): AppSortOptions {
            return apkSortOptionsArray.getValue(sortMode)
        }
    }
}

@Composable
@ReadOnlyComposable
fun AppSortOptions.uiLabel() = when (this) {
    AppSortOptions.SORT_BY_NAME -> stringResource(id = R.string.menu_sort_app_name)
    AppSortOptions.SORT_BY_PACKAGE -> stringResource(id = R.string.menu_sort_app_package)
    AppSortOptions.SORT_BY_INSTALL_TIME -> stringResource(id = R.string.menu_sort_app_install)
    AppSortOptions.SORT_BY_UPDATE_TIME -> stringResource(id = R.string.menu_sort_app_update)
    AppSortOptions.SORT_BY_APK_SIZE -> stringResource(id = R.string.menu_sort_app_apk_size)
}