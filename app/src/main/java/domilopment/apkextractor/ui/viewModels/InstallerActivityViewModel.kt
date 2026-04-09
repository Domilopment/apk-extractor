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
import domilopment.apkextractor.ui.model.installation.InstallationResultType
import domilopment.apkextractor.data.InstallationScreenState
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.data.model.install.InstallStrategy
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCase
import domilopment.apkextractor.domain.usecase.installer.InstallUseCase
import domilopment.apkextractor.domain.usecase.installer.UninstallUseCase
import domilopment.apkextractor.data.model.install.InstallationCallback
import domilopment.apkextractor.data.model.install.InstallationError
import domilopment.apkextractor.ui.model.installation.InstallApkError
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
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

    fun installApkBundle(fileUri: Uri, installStrategy: InstallStrategy = InstallStrategy.Internal(InstallerActivity::class.java)) {
        task = viewModelScope.launch {
            installUseCase(fileUri, installStrategy).collect {
                when (it) {
                    is InstallationCallback.OnPrepare -> {
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

                    is InstallationCallback.OnProgress -> updateState(
                        packageName = it.packageName, progress = it.progress
                    )

                    is InstallationCallback.InstallationResult -> {
                        uiState = uiState.copy(
                            progressState = null, result = when (it) {
                                is InstallationCallback.InstallationResult.OnError -> {
                                    val failure = when (it.error) {
                                        is InstallationError.SessionCreationException -> InstallApkError.SessionCreationFailure(
                                            it.fileUri, it.error.throwable.message
                                        )

                                        is InstallationError.IOException -> InstallApkError.FileReadError(
                                            it.fileUri, it.error.throwable.message
                                        )

                                        is InstallationError.ActivityNotFoundException -> InstallApkError.ExternNotFoundError(it.error.throwable.message)

                                        is InstallationError.UnknownException -> InstallApkError.Generic(it.error.throwable.message)
                                    }

                                    Timber.tag("InstallApkResult; ${it.packageName}").e(it.error.throwable)
                                    InstallationResultType.Failure.Install(it.packageName, fileUri = it.fileUri, failure)
                                }

                                is InstallationCallback.InstallationResult.OnExtern -> InstallationResultType.Success.Installed(it.packageName)

                                else -> uiState.result
                            }
                        )
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

    fun tryExternalInstall(fileUri: Uri) {
        Timber.d("Trying external install with $fileUri")
        installApkBundle(fileUri, InstallStrategy.External)
    }
}
