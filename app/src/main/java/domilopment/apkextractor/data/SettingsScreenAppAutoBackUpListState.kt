package domilopment.apkextractor.data

import androidx.compose.runtime.Stable
import domilopment.apkextractor.data.model.appList.ApplicationModel

@Stable
data class SettingsScreenAppAutoBackUpListState(private val list: List<ApplicationModel>) {
    private val set = list.associateBy({ it.appName }, { it.appPackageName })

    val entries = set.keys.toTypedArray()
    val entryValues = set.values.toTypedArray()

    fun isNotEmpty(): Boolean = set.isNotEmpty()
}