package domilopment.apkextractor.utils.appFilterOptions

import android.content.Context
import domilopment.apkextractor.data.model.appList.ApplicationModel

interface AppFilter {
    val name: String?
    val ordinal: Int

    fun getFilter(list: List<ApplicationModel>): List<ApplicationModel>
    fun getTitleString(context: Context): CharSequence?
}