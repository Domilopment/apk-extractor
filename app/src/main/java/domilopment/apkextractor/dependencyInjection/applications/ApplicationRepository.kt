package domilopment.apkextractor.dependencyInjection.applications

import domilopment.apkextractor.data.appList.ApplicationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ApplicationRepository {
    val apps: Flow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>>
    suspend fun updateApps()
    suspend fun addApp(app: ApplicationModel)
    suspend fun removeApp(app: ApplicationModel)
}

class MyApplicationRepository @Inject constructor(
    private val applicationService: ListOfApps
) : ApplicationRepository {
    override val apps: Flow<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        applicationService.apps

    override suspend fun updateApps() {
        withContext(Dispatchers.IO) {
            applicationService.updateData()
        }
    }

    override suspend fun addApp(app: ApplicationModel) {
        withContext(Dispatchers.IO) {
            applicationService.add(app)
        }
    }

    override suspend fun removeApp(app: ApplicationModel) {
        withContext(Dispatchers.IO) {
            applicationService.remove(app)
        }
    }
}