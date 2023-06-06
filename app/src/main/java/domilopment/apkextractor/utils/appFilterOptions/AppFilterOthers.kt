package domilopment.apkextractor.utils.appFilterOptions

import android.content.Context
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel

enum class AppFilterOthers : AppFilter {
    FAVORITES {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.isFavorite }
        }

        override fun getTitleString(context: Context): String {
            return context.getString(R.string.favorites)
        }
    };
}