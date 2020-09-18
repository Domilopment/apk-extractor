package domilopment.apkextractor.fragments

import androidx.annotation.NonNull
import androidx.lifecycle.*
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    @NonNull private val mainActivity: MainActivity
) :
    ViewModel(),
    HasDefaultViewModelProviderFactory {
    private val applications: MutableLiveData<List<Application>> by lazy {
        MutableLiveData<List<Application>>().also {
            it.value = loadApps()
        }
    }

    fun getApps(): LiveData<List<Application>> {
        return applications
    }

    fun updateApps() {
        viewModelScope.launch(Dispatchers.IO) {
            ListOfAPKs(mainActivity.packageManager).updateData()
            applications.postValue(SettingsManager(mainActivity).selectedAppTypes())
        }
    }

    private fun loadApps(): List<Application> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(mainActivity).selectedAppTypes()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(MainActivity::class.java).newInstance(mainActivity)
            }
        }
    }
}