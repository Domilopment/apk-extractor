package domilopment.apkextractor.fragments

import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.*
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs
import kotlinx.coroutines.Dispatchers
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

    fun getApps(): LiveData<List<Application>> {
        return applications
    }

    fun updateApps() {
        viewModelScope.launch(Dispatchers.IO) {
            ListOfAPKs(context.packageManager).updateData()
            applications.postValue(SettingsManager(context).selectedAppTypes())
        }
    }

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