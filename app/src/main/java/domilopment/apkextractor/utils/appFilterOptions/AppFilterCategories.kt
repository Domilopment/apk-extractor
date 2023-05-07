package domilopment.apkextractor.utils.appFilterOptions

import android.content.Context
import android.content.pm.ApplicationInfo
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel

enum class AppFilterCategories(
    private val category: Int, private val nameStrResId: Int
) : AppFilter {
    UNDEFINED(ApplicationInfo.CATEGORY_UNDEFINED, R.string.filter_category_undefined),
    GAMES(ApplicationInfo.CATEGORY_GAME, R.string.filter_category_games),
    AUDIO(ApplicationInfo.CATEGORY_AUDIO, R.string.filter_category_audio),
    VIDEO(ApplicationInfo.CATEGORY_VIDEO, R.string.filter_category_video),
    IMAGE(ApplicationInfo.CATEGORY_IMAGE, R.string.filter_category_image),
    SOCIAL(ApplicationInfo.CATEGORY_SOCIAL, R.string.filter_category_social),
    NEWS(ApplicationInfo.CATEGORY_NEWS, R.string.filter_category_news),
    MAPS(ApplicationInfo.CATEGORY_MAPS, R.string.filter_category_maps),
    PRODUCTIVITY(ApplicationInfo.CATEGORY_PRODUCTIVITY, R.string.filter_category_productivity);

    fun getTitleString(context: Context): String {
        return context.getString(nameStrResId)
    }

    override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
        return list.filter { it.appCategory == category }
    }

    companion object {
        private val categoriesMap = values().associateBy { it.category }
        fun getByCategory(appCategory: Int): AppFilterCategories? {
            return categoriesMap[appCategory]
        }
    }
}