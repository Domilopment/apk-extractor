package domilopment.apkextractor.data.repository.applications

import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.sources.ListOfApps
import kotlinx.coroutines.flow.Flow
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
        applicationService.updateData()
    }

    override suspend fun addApp(app: ApplicationModel) {
        applicationService.add(app)
    }

    override suspend fun removeApp(app: ApplicationModel) {
        applicationService.remove(app)
    }
}