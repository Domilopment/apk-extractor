package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.TaskUiState
import domilopment.apkextractor.domain.usecase.tasks.CancelTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.DismissTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.GetTaskProgressUseCase
import domilopment.apkextractor.ui.navigation.Route
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProgressDialogViewModel.Factory::class)
class ProgressDialogViewModel @AssistedInject constructor(
    @Assisted private val navKey: Route.ProgressDialog,
    private val getTask: GetTaskProgressUseCase,
    private val cancelTask: CancelTaskUseCase,
    private val dismissTask: DismissTaskUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: Route.ProgressDialog): ProgressDialogViewModel
    }

    private val _progressState = MutableStateFlow<TaskUiState?>(null)
    val progressDialogState: StateFlow<TaskUiState?> = _progressState.asStateFlow()

    init {
        viewModelScope.launch {
            getTask(navKey.taskId).collect { state ->
                _progressState.update { state }
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            cancelTask(navKey.taskId)
            dismissTask(navKey.taskId)
        }
    }
}
