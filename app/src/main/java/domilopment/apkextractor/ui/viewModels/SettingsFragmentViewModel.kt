package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import domilopment.apkextractor.dependencyInjection.applications.ListOfApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    application: Application, private val appsRepository: ApplicationRepository
) : AndroidViewModel(application) {
    private val _applications: MutableStateFlow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        MutableStateFlow(
            Triple(listOf(), listOf(), listOf())
        )

    val applications: StateFlow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        _applications.asStateFlow()

    init {
        viewModelScope.launch {
            appsRepository.apps.collect {
                _applications.value = it
            }
        }
    }
}
