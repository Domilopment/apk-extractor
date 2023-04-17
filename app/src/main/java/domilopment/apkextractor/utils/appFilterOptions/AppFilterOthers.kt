package domilopment.apkextractor.utils.appFilterOptions

import domilopment.apkextractor.data.ApplicationModel

enum class AppFilterOthers : AppFilter {
    FAVORITES {
        override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
            return list.filter { it.isFavorite }
        }
    };
}