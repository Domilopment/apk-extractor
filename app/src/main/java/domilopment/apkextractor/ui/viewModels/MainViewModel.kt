package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.*
import domilopment.apkextractor.UpdateTrigger
import domilopment.apkextractor.data.*
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
import kotlinx.coroutines.flow.update

class MainViewModel(application: Application) : AndroidViewModel(application), Observer {
    override val key: String = "AppListViewModel"

    private val _applications: MutableLiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> by lazy {
        MutableLiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>>().also {
            viewModelScope.launch {
                it.value = loadApps()
            }
        }
    }
    val applications: LiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        _applications

    private val _mainFragmentState: MutableStateFlow<MainFragmentUIState> =
        MutableStateFlow(MainFragmentUIState())
    val mainFragmentState: StateFlow<MainFragmentUIState> = _mainFragmentState.asStateFlow()

    private val _appOptionsBottomSheetState: MutableStateFlow<AppOptionsBottomSheetUIState> =
        MutableStateFlow(AppOptionsBottomSheetUIState())
    val appOptionsBottomSheetUIState: StateFlow<AppOptionsBottomSheetUIState> =
        _appOptionsBottomSheetState.asStateFlow()

    private val _searchQuery: MutableLiveData<String?> = MutableLiveData(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private val context get() = getApplication<Application>().applicationContext

    private val observer =
        Observer<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> { apps ->
            _mainFragmentState.update { state ->
                val settingsManager = SettingsManager(context)
                state.copy(
                    appList = settingsManager.sortAppData(
                        settingsManager.filterApps(
                            settingsManager.selectedAppTypes(
                                apps
                            )
                        )
                    ), isRefreshing = false, updateTrigger = UpdateTrigger(true)
                )
            }
        }

    init {
        // Set applications in view once they are loaded
        _applications.observeForever(observer)
        EventDispatcher.registerObserver(this, EventType.INSTALLED, EventType.UNINSTALLED)
    }

    override fun onCleared() {
        EventDispatcher.unregisterObserver(this, EventType.ANY)
        _applications.removeObserver(observer)
        super.onCleared()
    }

    override fun onEventReceived(event: Event<*>) {
        when (event.eventType) {
            EventType.INSTALLED -> addApplication(event.data as String)
            EventType.UNINSTALLED -> if (Utils.isPackageInstalled(
                    context.packageManager, event.data as String
                )
            ) moveFromUpdatedToSystemApps(event.data) else removeApp(event.data)

            else -> return
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectApplication(app: ApplicationModel?) {
        _appOptionsBottomSheetState.update { state ->
            state.copy(
                selectedApplicationModel = app
            )
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
     * Set View state of action mode
     * @param actionMode Boolean of action mode is active
     */
    fun addActionModeCallback(actionMode: Boolean) {
        _mainFragmentState.update { state ->
            state.copy(actionMode = actionMode)
        }
    }

    /**
     * Update App list
     */
    fun updateApps() {
        _mainFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val load = async(Dispatchers.IO) {
                return@async loadApps()
            }
            val apps = load.await()
            _applications.postValue(apps)
        }
    }

    private fun addApplication(packageName: String) {
        val apps = _applications.value ?: return

        if ((apps.first + apps.third).any { it.appPackageName == packageName }) return

        val updatedSystemApps = apps.first.toMutableList()
        val systemApps = apps.second.toMutableList()
        val userApps = apps.third.toMutableList()

        systemApps.find { it.appPackageName == packageName }?.let {
            updatedSystemApps.add(it)
            systemApps.remove(it)
        } ?: userApps.add(ApplicationModel(context.packageManager, packageName))

        _applications.value = Triple(systemApps, updatedSystemApps, userApps)
    }

    /**
     * Remove app from app list, for example on uninstall
     * @param app
     * uninstalled app
     */
    fun removeApp(app: ApplicationModel) {
        _applications.value = _applications.value?.let { apps ->
            val userApps = apps.third.toMutableList().apply {
                remove(app)
            }
            return@let Triple(apps.first, apps.second, userApps)
        }
    }

    /**
     * Remove app from app list, for example on uninstall
     * @param appPackageName
     * uninstalled apps package name
     */
    private fun removeApp(appPackageName: String) {
        _applications.value = _applications.value?.let { apps ->
            val userApps = apps.third.filter { it.appPackageName != appPackageName }
            return@let Triple(apps.first, apps.second, userApps)
        }
    }

    /**
     * Moves app from updated system apps list, and moves it to system apps, for example on uninstall
     * @param app
     * uninstalled app
     */
    fun moveFromUpdatedToSystemApps(app: ApplicationModel) {
        _applications.value = _applications.value?.let { apps ->
            val updatedSystemApps = apps.first.toMutableList()
            val systemApps = if (updatedSystemApps.remove(app)) {
                apps.second.toMutableList().apply {
                    add(app)
                }
            } else apps.second
            return@let Triple(updatedSystemApps, systemApps, apps.third)
        }
    }

    private fun moveFromUpdatedToSystemApps(packageName: String) {
        _applications.value = _applications.value?.let { apps ->
            val updatedSystemApps = apps.first.toMutableList()
            val systemApps = if (updatedSystemApps.removeIf { it.appPackageName == packageName }) {
                apps.second.toMutableList().apply {
                    add(ApplicationModel(context.packageManager, packageName))
                }
            } else apps.second
            return@let Triple(updatedSystemApps, systemApps, apps.third)
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
                state.copy(
                    appList = sortedList, updateTrigger = UpdateTrigger(state.appList == sortedList)
                )
            }
        }
    }

    fun filterApps() {
        val settingsManager = SettingsManager(context)
        viewModelScope.launch {
            applications.value?.let {
                _mainFragmentState.update { state ->
                    val sortedList = withContext(Dispatchers.IO) {
                        settingsManager.sortAppData(
                            settingsManager.filterApps(
                                SettingsManager(context).selectedAppTypes(
                                    it
                                )
                            )
                        )
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

        _applications.value?.let {
            _mainFragmentState.update { state ->
                state.copy(isRefreshing = true)
            }
            viewModelScope.launch {
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

    /**
     * Load apps from device
     */
    private suspend fun loadApps(): Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>> =
        withContext(Dispatchers.IO) {
            // Do an asynchronous operation to fetch users.
            return@withContext SettingsManager(context).getApps()
        }
}