package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.UpdateTrigger
import domilopment.apkextractor.data.ApkListFragmentUIState
import domilopment.apkextractor.data.ApkOptionsBottomSheetUIState
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApkListViewModel(application: Application):  AndroidViewModel(application),
    HasDefaultViewModelProviderFactory {

    private val _applications: MutableLiveData<List<PackageArchiveModel>> by lazy {
        MutableLiveData< List<PackageArchiveModel>>().also {
            viewModelScope.launch {
                it.value = loadApks()
            }
        }
    }
    val applications: LiveData<List<PackageArchiveModel>> =
        _applications

    private val _apkListFragmentState: MutableStateFlow<ApkListFragmentUIState> =
        MutableStateFlow(ApkListFragmentUIState())
    val apkListFragmentState: StateFlow<ApkListFragmentUIState> = _apkListFragmentState.asStateFlow()

    private val _apkOptionsBottomSheetState: MutableStateFlow<ApkOptionsBottomSheetUIState> =
        MutableStateFlow(ApkOptionsBottomSheetUIState())
    val akpOptionsBottomSheetUIState: StateFlow<ApkOptionsBottomSheetUIState> =
        _apkOptionsBottomSheetState.asStateFlow()

    private val _searchQuery: MutableLiveData<String?> = MutableLiveData(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private val context get() = getApplication<Application>().applicationContext

    init {
        // Set applications in view once they are loaded
        _applications.observeForever { apps ->
            _apkListFragmentState.update { state ->
                state.copy(
                    appList = apps
                    , isRefreshing = false, updateTrigger = UpdateTrigger(true)
                )
            }
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectApplication(app: PackageArchiveModel) {
        _apkOptionsBottomSheetState.update { state ->
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
     * Update App list
     */
    fun updateApps() {
        _apkListFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val load = async(Dispatchers.IO) {
                return@async loadApks()
            }
            val apps = load.await()
            _applications.postValue(apps)
        }
    }

    fun remove(apk: PackageArchiveModel) {
        _applications.value = _applications.value?.let { apps ->
            apps.toMutableList().apply {
                remove(apk)
            }
        }
    }

    /*
    /**
     * Install APK file from ACTION_OPEN_DOCUMENT uri
     * @param apkUri uri from Intent return data
     */
    fun installApk(apkUri: Uri, callback: PackageInstallerSessionCallback) {
        viewModelScope.launch(Dispatchers.Main) {
            val packageInstaller = context.applicationContext.packageManager.packageInstaller
            val contentResolver = context.applicationContext.contentResolver
            packageInstaller.registerSessionCallback(callback)

            withContext(Dispatchers.IO) {
                contentResolver.openInputStream(apkUri)?.use { apkStream ->
                    val params =
                        PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                    val sessionId = packageInstaller.createSession(params)
                    callback.initialSessionId = sessionId

                    _progressDialogState.update {
                        it.copy(
                            title = context.getString(R.string.progress_dialog_title_install),
                            process = packageInstaller.getSessionInfo(sessionId)?.appPackageName
                                ?: "",
                            tasks = 100,
                            shouldBeShown = true
                        )
                    }

                    val session = packageInstaller.openSession(sessionId)

                    val length = DocumentFile.fromSingleUri(context, apkUri)?.length() ?: -1

                    session.openWrite("install_apk_session_$sessionId", 0, length)
                        .use { outputStream ->
                            apkStream.copyTo(outputStream)
                            session.fsync(outputStream)
                        }

                    val pendingIntent = Intent(context, InstallBroadcastReceiver::class.java).let {
                        PendingIntent.getBroadcast(
                            context, sessionId, it, PendingIntent.FLAG_MUTABLE
                        )
                    }

                    session.commit(pendingIntent.intentSender)
                    session.close()
                }
            }
        }
    }

    /**
     * Update ProgressDialogState for APK installations
     * @param progress current progress of installation
     * @param packageName name of package being installed
     */
    fun updateInstallApkStatus(progress: Float, packageName: String? = "") {
        _progressDialogState.update {
            it.copy(
                title = context.getString(R.string.progress_dialog_title_install),
                process = packageName,
                progress = (progress * 100).toInt(),
            )
        }
    }
     */

    /**
     * Load apps from device
     */
    private suspend fun loadApks(): List<PackageArchiveModel> =
        withContext(Dispatchers.IO) {
            // Do an asynchronous operation to fetch users.
            return@withContext ListOfAPKs(context).apkFiles()
        }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(Application::class.java)
                    .newInstance(getApplication())
            }
        }
    }
}
