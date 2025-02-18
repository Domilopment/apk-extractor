package domilopment.apkextractor.data.repository.applications

import domilopment.apkextractor.data.model.appList.AppModel
import domilopment.apkextractor.data.sources.ListOfApps
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ApplicationRepository {
    val apps: Flow<List<AppModel>>
    val systemApps: Flow<List<AppModel.SystemApp>>
    val updatedSystemApps: Flow<List<AppModel.UpdatedSystemApps>>
    val userApps: Flow<List<AppModel.UserApp>>
    suspend fun updateApps()
    suspend fun addApp(app: AppModel)
    suspend fun removeApp(app: AppModel)
}

class MyApplicationRepository @Inject constructor(
    private val applicationService: ListOfApps
) : ApplicationRepository {
    override val apps: Flow<List<AppModel>> = applicationService.apps
    override val systemApps: Flow<List<AppModel.SystemApp>> = applicationService.systemApps
    override val updatedSystemApps: Flow<List<AppModel.UpdatedSystemApps>> =
        applicationService.updatedSystemApps
    override val userApps: Flow<List<AppModel.UserApp>> = applicationService.userApps

    override suspend fun updateApps() {
        applicationService.updateData()
    }

    override suspend fun addApp(app: AppModel) {
        applicationService.add(app)
    }

    override suspend fun removeApp(app: AppModel) {
        applicationService.remove(app)
    }
}