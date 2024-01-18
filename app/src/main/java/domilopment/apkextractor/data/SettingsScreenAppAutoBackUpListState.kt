package domilopment.apkextractor.data

import domilopment.apkextractor.data.appList.ApplicationModel

data class SettingsScreenAppAutoBackUpListState(private val list: List<ApplicationModel>) {
    private val set = list.associateBy({ it.appName }, { it.appPackageName })

    val entries = set.keys.toTypedArray()
    val entryValues = set.values.toTypedArray()

    fun isNotEmpty(): Boolean = set.isNotEmpty()
}