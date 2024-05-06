package domilopment.apkextractor.domain.usecase.appList

import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository

interface UpdateAppsUseCase {
    suspend operator fun invoke()
}

class UpdateAppsUseCaseImpl(private val repository: ApplicationRepository): UpdateAppsUseCase {
    override suspend fun invoke() {
        repository.updateApps()
    }
}