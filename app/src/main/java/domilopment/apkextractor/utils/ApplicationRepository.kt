package domilopment.apkextractor.utils

import domilopment.apkextractor.data.ApplicationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class ApplicationRepository(private val applicationService: ListOfApps) {
    val apps: Flow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        applicationService.apps

    suspend fun updateApps() {
        withContext(Dispatchers.IO) {
            applicationService.updateData()
        }
    }
}