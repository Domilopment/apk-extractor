package domilopment.apkextractor.fragments

import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.*
import domilopment.apkextractor.utils.SettingsManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel(
    @NonNull private val context: Context
) :
    ViewModel(),
    HasDefaultViewModelProviderFactory {
    private val applications: MutableLiveData<List<Application>> by lazy {
        MutableLiveData<List<Application>>().also {
            it.value = loadApps()
        }
    }

    /**
     * Get app list from ViewModel
     * @return
     * List of APKs
     */
    fun getApps(): LiveData<List<Application>> {
        return applications
    }

    /**
     * Update App list
     */
    fun updateApps() {
        viewModelScope.launch {
            val load = async(Dispatchers.IO) {
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
    fun removeApp(app: Application) {
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
            val load = async(Dispatchers.IO) {
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
    private fun loadApps(): List<Application> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(context).selectedAppTypes()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(Context::class.java).newInstance(context)
            }
        }
    }
}