package domilopment.apkextractor.utils.appFilterOptions

import android.content.Context
import android.content.pm.ApplicationInfo
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel

enum class AppFilterCategories(private val category: Int) : AppFilter {
    UNDEFINED(ApplicationInfo.CATEGORY_UNDEFINED) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_undefined)
        }
    },
    GAMES(ApplicationInfo.CATEGORY_GAME) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_games)
        }
    },
    AUDIO(ApplicationInfo.CATEGORY_AUDIO) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_audio)
        }
    },
    VIDEO(ApplicationInfo.CATEGORY_VIDEO) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_video)
        }
    },
    IMAGE(ApplicationInfo.CATEGORY_IMAGE) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_image)
        }
    },
    SOCIAL(ApplicationInfo.CATEGORY_SOCIAL) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_social)
        }
    },
    NEWS(ApplicationInfo.CATEGORY_NEWS) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_news)
        }
    },
    MAPS(ApplicationInfo.CATEGORY_MAPS) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_maps)
        }
    },
    PRODUCTIVITY(ApplicationInfo.CATEGORY_PRODUCTIVITY) {
        override fun getTitleString(context: Context): String {
            return context.getString(R.string.filter_category_productivity)
        }
    };

    abstract fun getTitleString(context: Context): String

    override fun getFilter(list: List<ApplicationModel>): List<ApplicationModel> {
        return list.filter { it.appCategory == category }
    }
}