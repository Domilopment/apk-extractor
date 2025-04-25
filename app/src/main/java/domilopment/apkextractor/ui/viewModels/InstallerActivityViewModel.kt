package domilopment.apkextractor.ui.viewModels

import android.content.pm.PackageInstaller
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.InstallerActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.data.InstallationResultType
import domilopment.apkextractor.data.InstallationScreenState
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCase
import domilopment.apkextractor.domain.usecase.installer.InstallUseCase
import domilopment.apkextractor.domain.usecase.installer.UninstallUseCase
import domilopment.apkextractor.utils.InstallApkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstallerActivityViewModel @Inject constructor(
    private val installUseCase: InstallUseCase,
    private val addAppUseCase: AddAppUseCase,
    private val uninstallUseCase: UninstallUseCase,
    private val removeAppUseCase: RemoveAppUseCase,
) : ViewModel() {
    var uiState by mutableStateOf(InstallationScreenState(null, null))
        private set

    private var session: PackageInstaller.Session? = null
    private var task: Job? = null

    override fun onCleared() {
        cancel()
        super.onCleared()
    }

    private fun updateState(
        packageName: String? = uiState.progressState?.process,
        progress: Float = uiState.progressState?.progress?.div(100) ?: 0F
    ) {
        uiState = uiState.copy(
            progressState = uiState.progressState?.copy(
                process = packageName, progress = progress * 100
            )
        )
    }

    fun installApkBundle(fileUri: Uri) {
        task = viewModelScope.launch {
            installUseCase(fileUri, InstallerActivity::class.java).collect {
                when (it) {
                    is InstallApkResult.OnPrepare -> {
                        session = it.session
                        uiState = uiState.copy(
                            progressState = ProgressDialogUiState(
                                title = UiText(R.string.progress_dialog_title_install),
                                process = it.packageName,
                                progress = 0F,
                                tasks = 100
                            )
                        )
                    }

                    is InstallApkResult.OnProgress -> updateState(
                        packageName = it.packageName, progress = it.progress
                    )

                    is InstallApkResult.OnFinish -> {
                        uiState = uiState.copy(
                            progressState = null,
                            result = when (it) {
                                is InstallApkResult.OnFinish.OnError -> InstallationResultType.Failure.Install(
                                    it.packageName, it.error
                                )

                                else -> uiState.result
                            })
                    }
                }
            }
        }
    }

    fun cancel() {
        task?.cancel()
        task = null
        session?.abandon()
        session = null
    }

    fun uninstallApp(packageUri: Uri) {
        val packageName = packageUri.schemeSpecificPart
        viewModelScope.launch {
            uninstallUseCase(packageName, InstallerActivity::class.java)
        }
    }

    fun removeApp(packageName: String) {
        viewModelScope.launch {
            removeAppUseCase(packageName)
        }
    }

    fun addApp(packageName: String) {
        viewModelScope.launch {
            addAppUseCase(packageName)
        }
    }

    fun showInstallationResult(result: InstallationResultType?) {
        uiState = uiState.copy(progressState = null, result = result)
    }
}
