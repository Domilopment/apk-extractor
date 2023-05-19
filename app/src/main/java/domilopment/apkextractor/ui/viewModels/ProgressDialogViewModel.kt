package domilopment.apkextractor.ui.viewModels

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.Event
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.installApk.InstallBroadcastReceiver
import domilopment.apkextractor.installApk.PackageInstallerSessionCallback
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressDialogViewModel(
    application: Application
) : AndroidViewModel(application), HasDefaultViewModelProviderFactory {
    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState> =
        MutableStateFlow(ProgressDialogUiState())
    val progressDialogState: StateFlow<ProgressDialogUiState> = _progressDialogState.asStateFlow()

    private val _extractionResult: MutableLiveData<Event<Triple<Boolean?, ApplicationModel?, Int>>> =
        MutableLiveData(null)
    val extractionResult: LiveData<Event<Triple<Boolean?, ApplicationModel?, Int>>> =
        _extractionResult

    private val _shareResult: MutableLiveData<Event<ArrayList<Uri>?>> = MutableLiveData(null)
    val shareResult: LiveData<Event<ArrayList<Uri>?>> = _shareResult

    private val context get() = getApplication<Application>().applicationContext

    /**
     * save multiple apps to filesystem
     * @param list of apps user wants to save
     */
    fun saveApps(list: List<ApplicationModel>) {
        viewModelScope.launch {
            val settingsManager = SettingsManager(context)
            val fileHelper = FileHelper(context)
            var application: ApplicationModel? = null
            var failure = false

            _progressDialogState.update {
                it.copy(
                    title = context.getString(R.string.progress_dialog_title_save),
                    tasks = list.size,
                    shouldBeShown = true
                )
            }

            val job = launch extract@{
                list.forEach { app ->
                    application = app
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                process = app.appPackageName, progress = state.progress
                            )
                        }
                    }
                    withContext(Dispatchers.IO) {
                        failure = fileHelper.copy(
                            app.appSourceDirectory,
                            settingsManager.saveDir()!!,
                            settingsManager.appName(app)
                        ) == null
                    }
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                process = app.appPackageName, progress = state.progress + 1
                            )
                        }
                    }
                    if (failure) {
                        this@extract.cancel()
                    }
                }
            }
            job.join()
            _extractionResult.value = Event(Triple(failure, application, list.size))
        }
    }

    /**
     * create temp files for apps user want to save and get share Uris for them
     * @param list list of all apps
     */
    fun createShareUrisForApps(list: List<ApplicationModel>) {
        viewModelScope.launch {
            val files = ArrayList<Uri>()
            val fileHelper = FileHelper(context)
            val jobList = ArrayList<Deferred<Any?>>()

            _progressDialogState.update {
                it.copy(
                    title = context.getString(R.string.progress_dialog_title_share),
                    tasks = list.size,
                    shouldBeShown = true
                )
            }

            list.filter {
                it.isChecked
            }.forEach { app ->
                jobList.add(async {
                    withContext(Dispatchers.IO) {
                        fileHelper.shareURI(app).also {
                            files.add(it)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                progress = state.progress + 1
                            )
                        }
                    }
                })
            }
            jobList.awaitAll()
            _shareResult.value = Event(files)
        }
    }

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

    /**
     * Reset Progress dialog state back to default
     */
    fun resetProgress() {
        _progressDialogState.value = ProgressDialogUiState()
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