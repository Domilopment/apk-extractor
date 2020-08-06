package domilopment.apkextractor.fragments

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.fragment_main.*

class MainViewModel(private val mainActivity: MainActivity): ViewModel() {
    private val applications: MutableLiveData<List<Application>> by lazy {
        MutableLiveData<List<Application>>().also {
            it.value = loadApps()
        }
    }

    fun getApps(): LiveData<List<Application>> {
        return applications
    }

    fun updateApps(apps: List<Application>) {
        applications.value = apps
    }

    private fun loadApps(): List<Application> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(mainActivity).selectedAppTypes()
    }
}