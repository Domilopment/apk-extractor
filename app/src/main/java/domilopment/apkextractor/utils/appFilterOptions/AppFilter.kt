package domilopment.apkextractor.utils.appFilterOptions

import domilopment.apkextractor.data.ApplicationModel

interface AppFilter {
    val name: String?

    fun getFilter(list: List<ApplicationModel>): List<ApplicationModel>
}