package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.model.apkList.ApkDetailScreenState
import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCase
import domilopment.apkextractor.domain.usecase.apkList.GetApkDetailsUseCase
import domilopment.apkextractor.domain.usecase.apkList.GetApkInfoFromDocumentUseCase
import domilopment.apkextractor.domain.usecase.apkList.LoadApkInfoUseCase
import domilopment.apkextractor.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = ApkDetailViewModel.Factory::class)
class ApkDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: Route.ApkDetails,
    private val apkDetails: GetApkDetailsUseCase,
    private val deleteApk: DeleteApkUseCase,
    private val loadApkInfo: LoadApkInfoUseCase,
    private val loadApkInfoFromDocument: GetApkInfoFromDocumentUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: Route.ApkDetails): ApkDetailViewModel
    }

    private val _uiState = MutableStateFlow(ApkDetailScreenState())
    val uiState: StateFlow<ApkDetailScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apkDetails(navKey.fileUri).distinctUntilChanged().collect { apk ->
                val displayApk = apk ?: withContext(Dispatchers.IO) {
                    loadApkInfoFromDocument(navKey.fileUri)
                }
                _uiState.update { state ->
                    state.copy(app = displayApk, isLoading = false)
                }
                if (displayApk?.loaded == false) withContext(Dispatchers.IO) {
                    loadApkInfo(displayApk)
                }
            }
        }
    }

    fun remove(apk: ApkModel.ApkDetailModel) {
        viewModelScope.launch {
            deleteApk(apk)
        }
    }

    fun loadPackageArchiveInfo(apk: ApkModel.ApkDetailModel) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(app = state.app?.copy(loaded = false))
            }
            loadApkInfo(apk)
        }
    }
}
