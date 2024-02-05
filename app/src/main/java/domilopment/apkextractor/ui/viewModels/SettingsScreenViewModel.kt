package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.settings.AppSortOptions
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    application: Application,
    appsRepository: ApplicationRepository,
    private val settings: PreferenceRepository
) : AndroidViewModel(application) {
    val autoBackupAppsListState = appsRepository.apps.map { apps ->
        ApplicationUtil.selectedAppTypes(
            apps,
            selectUpdatedSystemApps = true,
            selectSystemApps = false,
            selectUserApps = true,
            emptySet()
        ).let {
            ApplicationUtil.sortAppData(
                it,
                sortMode = AppSortOptions.SORT_BY_NAME.ordinal,
                sortFavorites = false,
                sortAsc = true
            )
        }.let { list ->
            SettingsScreenAppAutoBackUpListState(list)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        SettingsScreenAppAutoBackUpListState(emptyList())
    )

    val saveDir = settings.saveDir.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val saveName = settings.appSaveName.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), setOf("0:name")
    )
    val autoBackupService = settings.autoBackupService.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val autoBackupList = settings.autoBackupAppList.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), emptySet()
    )
    val nightMode = settings.nightMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )
    val useMaterialYou = settings.useMaterialYou.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val rightSwipeAction = settings.appRightSwipeAction.map { it.preferenceValue }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        ApkActionsOptions.SAVE.preferenceValue
    )
    val leftSwipeAction = settings.appLeftSwipeAction.map { it.preferenceValue }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        ApkActionsOptions.SHARE.preferenceValue
    )
    val swipeActionCustomThreshold = settings.appSwipeActionCustomThreshold.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val swipeActionThresholdMod = settings.appSwipeActionThresholdMod.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 32f
    )
    val checkUpdateOnStart = settings.checkUpdateOnStart.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val backupModeXapk = settings.backupModeXapk.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )

    fun setAppSaveName(set: Set<String>) {
        viewModelScope.launch { settings.setAppSaveName(set) }
    }

    fun setAutoBackupService(b: Boolean) {
        viewModelScope.launch { settings.setAutoBackupService(b) }
    }

    fun setAutoBackupList(set: Set<String>) {
        viewModelScope.launch { settings.setListOfAutoBackupApps(set) }
    }

    fun setNightMode(mode: Int) {
        viewModelScope.launch { settings.setNightMode(mode) }
    }

    fun setUseMaterialYou(b: Boolean) {
        viewModelScope.launch { settings.setUseMaterialYou(b) }
    }

    fun setRightSwipeAction(s: String) {
        viewModelScope.launch { settings.setRightSwipeAction(s) }
    }

    fun setLeftSwipeAction(s: String) {
        viewModelScope.launch { settings.setLeftSwipeAction(s) }
    }

    fun setSwipeActionCustomThreshold(b: Boolean) {
        viewModelScope.launch { settings.setSwipeActionCustomThreshold(b) }
    }

    fun setSwipeActionThresholdMod(f: Float) {
        viewModelScope.launch { settings.setSwipeActionThresholdMod(f) }
    }

    fun setCheckUpdateOnStart(b: Boolean) {
        viewModelScope.launch { settings.setCheckUpdateOnStart(b) }
    }

    fun setBackupModeXapk(b: Boolean) {
        viewModelScope.launch { settings.setBackupModeXapk(b) }
    }
}
