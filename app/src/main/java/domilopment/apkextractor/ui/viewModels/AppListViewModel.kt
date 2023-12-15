package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import domilopment.apkextractor.data.*
import domilopment.apkextractor.utils.ApplicationRepository
import domilopment.apkextractor.utils.ListOfApps
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.utils.eventHandler.Observer
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AppListViewModel(application: Application) : AndroidViewModel(application), Observer {
    override val key: String = "AppListViewModel"

    private val appsRepository = ApplicationRepository(ListOfApps.getApplications(application))

    private val _mainFragmentState: MutableStateFlow<AppListScreenState> =
        MutableStateFlow(AppListScreenState())
    val mainFragmentState: StateFlow<AppListScreenState> = _mainFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val context get() = getApplication<Application>().applicationContext

    init {
        // Set applications in view once they are loaded
        viewModelScope.launch {
            searchQuery.debounce(500L)
                .combine(appsRepository.apps.mapLatest { userConfigFilteredApps(it) }) { searchQuery, appList ->
                    val searchString = searchQuery?.trim()

                    return@combine if (searchString.isNullOrBlank()) {
                        appList
                    } else {
                        appList.filter {
                            it.appName.contains(
                                searchString, ignoreCase = true
                            ) || it.appPackageName.contains(
                                searchString, ignoreCase = true
                            )
                        }
                    }
                }.collect {
                    _mainFragmentState.update { state ->
                        state.copy(
                            appList = it, isRefreshing = false
                        )
                    }
                }
        }

        EventDispatcher.registerObserver(this, EventType.INSTALLED, EventType.UNINSTALLED)
    }

    override fun onCleared() {
        EventDispatcher.unregisterObserver(this, EventType.ANY)
        super.onCleared()
    }

    override fun onEventReceived(event: Event<*>) {
        when (event.eventType) {
            EventType.INSTALLED -> addApps(event.data as String)
            EventType.UNINSTALLED -> if (Utils.isPackageInstalled(
                    context.packageManager, event.data as String
                )
            ) uninstallApps(event.data)

            else -> return
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectApplication(app: ApplicationModel?) {
        _mainFragmentState.update { state ->
            state.copy(selectedApp = app)
        }
    }

    /**
     * Set query string from search in App List, sets empty string if null
     * @param query last input Search String
     */
    fun searchQuery(query: String?) {
        _searchQuery.value = query
    }

    /**
     * Update App list
     */
    fun updateApps() {
        _mainFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            async { appsRepository.updateApps() }
        }
    }

    fun addApps(packageName: String) {
        viewModelScope.launch {
            async { appsRepository.addApp(ApplicationModel(context.packageManager, packageName)) }
        }
    }

    fun uninstallApps(packageName: String) {
        if (Utils.isPackageInstalled(context.packageManager, packageName)) return

        _mainFragmentState.value.appList.find { it.appPackageName == packageName }?.let { app ->
            _mainFragmentState.update {
                it.copy(
                    isRefreshing = true,
                    appList = it.appList.toMutableList().apply { remove(app) },
                    selectedApp = if (it.selectedApp?.appPackageName == packageName) null else it.selectedApp
                )
            }
            viewModelScope.launch {
                async { appsRepository.removeApp(app) }
            }
        }
    }

    /**
     * Sorts data on Call after Selected Sort type
     */
    fun sortApps() {
        _mainFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            _mainFragmentState.update { state ->
                state.copy(
                    appList = withContext(Dispatchers.IO) {
                        SettingsManager(context).sortAppData(state.appList)
                    }, isRefreshing = false
                )
            }
        }
    }

    /**
     * if new Favorite is added, sort to top of the list, if removed sort back in list with selected options
     */
    fun sortFavorites() {
        val settingsManager = SettingsManager(context)
        viewModelScope.launch {
            _mainFragmentState.update { state ->
                val sortedList = withContext(Dispatchers.IO) {
                    settingsManager.sortAppData(state.appList)
                }
                state.copy(appList = sortedList)
            }
        }
    }

    fun filterApps() {
        viewModelScope.launch {
            appsRepository.apps.collect {
                _mainFragmentState.update { state ->
                    val sortedList = withContext(Dispatchers.IO) {
                        userConfigFilteredApps(it)
                    }
                    state.copy(appList = sortedList)
                }
            }
        }
    }

    /**
     * update State for List view inside UI once user changed selected app types inside the Settings
     * @param key key of switch preference who has changed
     * @param b true for should be selected, false if no longer should be included
     * @throws Exception if key is not "updated_system_apps", "system_apps" or "user_apps"
     */
    @Throws(Exception::class)
    fun changeSelection(key: String, b: Boolean) {
        if (key !in listOf(
                "updated_system_apps", "system_apps", "user_apps"
            )
        ) throw Exception("No available key provided!")

        val settingsManager = SettingsManager(context)

        _mainFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            appsRepository.apps.collect {
                val selectedAppTypes = async(Dispatchers.IO) {
                    return@async when (key) {
                        "updated_system_apps" -> settingsManager.selectedAppTypes(
                            it, selectUpdatedSystemApps = b
                        )

                        "system_apps" -> settingsManager.selectedAppTypes(
                            it, selectSystemApps = b
                        )

                        "user_apps" -> settingsManager.selectedAppTypes(
                            it, selectUserApps = b
                        )

                        else -> null
                    }?.let {
                        settingsManager.sortAppData(settingsManager.filterApps(it))
                    }
                }
                selectedAppTypes.await()?.let {
                    _mainFragmentState.update { state ->
                        state.copy(appList = it, isRefreshing = false)
                    }
                }
            }
        }
    }

    private fun userConfigFilteredApps(apps: Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>): List<ApplicationModel> {
        val settingsManager = SettingsManager(context)

        return settingsManager.sortAppData(
            settingsManager.filterApps(
                settingsManager.selectedAppTypes(apps)
            )
        )
    }

    fun updateApp(app: ApplicationModel) {
        _mainFragmentState.update { state ->
            state.copy(appList = state.appList.toMutableList()
                .map { if (it.appPackageName == app.appPackageName) app else it })
        }

    }

    fun selectAllApps(select: Boolean) {
        _mainFragmentState.update { state ->
            state.copy(appList = state.appList.toMutableList().map { it.copy(isChecked = select) })
        }
    }
}