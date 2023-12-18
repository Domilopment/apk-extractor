package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.utils.ApplicationRepository
import domilopment.apkextractor.utils.dataSources.ListOfApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val appsRepository = ApplicationRepository(ListOfApps.getApplications(application))

    private val _applications: MutableStateFlow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> = MutableStateFlow(
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
