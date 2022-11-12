package domilopment.apkextractor.fragments

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.*
import domilopment.apkextractor.utils.SettingsManager
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.data.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application), HasDefaultViewModelProviderFactory {
    private val applications: MutableLiveData<List<ApplicationModel>> by lazy {
        MutableLiveData<List<ApplicationModel>>().also {
            it.value = loadApps()
        }
    }

    private val context get() = getApplication<Application>().applicationContext

    private val ioDispatcher get() = Dispatchers.IO

    /**
     * Get app list from ViewModel
     * @return
     * List of APKs
     */
    fun getApps(): LiveData<List<ApplicationModel>> {
        return applications
    }

    /**
     * Update App list
     */
    fun updateApps() {
        viewModelScope.launch {
            val load = async(ioDispatcher) {
                ListOfAPKs(context.packageManager).updateData()
                return@async loadApps()
            }
            applications.postValue(load.await())
        }
    }

    /**
     * Remove app from app list, for example on uninstall
     * @param app
     * uninstalled app
     */
    fun removeApp(app: ApplicationModel) {
        applications.value?.toMutableList()
            ?.apply {
                remove(app)
            }?.also {
                applications.postValue(it)
            }
    }

    /**
     * Sorts data on Call after Selected Sort type
     */
    fun sortApps() {
        viewModelScope.launch {
            val load = async(ioDispatcher) {
                applications.value?.let {
                    return@async SettingsManager(context).sortData(it)
                }
            }
            applications.postValue(load.await())
        }
    }

    /**
     * Load apps from device
     */
    private fun loadApps(): List<ApplicationModel> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(context).selectedAppTypes()
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