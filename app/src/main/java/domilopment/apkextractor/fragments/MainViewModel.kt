package domilopment.apkextractor.fragments

import android.os.AsyncTask
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.data.ListOfAPKs

class MainViewModel(@NonNull private val mainActivity: MainActivity): ViewModel() {
    private val applications: MutableLiveData<List<Application>> by lazy {
        MutableLiveData<List<Application>>().also {
            it.value = loadApps()
        }
    }

    fun getApps(): LiveData<List<Application>> {
        return applications
    }

    fun updateApps() {
        object : AsyncTask<Void, Void, List<Application>>() {
            override fun doInBackground(vararg p0: Void?): List<Application> {
                ListOfAPKs(mainActivity.packageManager).updateData()
                return SettingsManager(mainActivity).selectedAppTypes()
            }

            override fun onPostExecute(result: List<Application>?) {
                super.onPostExecute(result)
                applications.value = result
            }
        }.execute()
    }

    private fun loadApps(): List<Application> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(mainActivity).selectedAppTypes()
    }
}