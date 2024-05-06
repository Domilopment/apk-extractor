package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import javax.inject.Inject

interface AddAppUseCase {
    suspend operator fun invoke(packageName: String)
}

class AddAppUseCaseImpl @Inject constructor(
    private val context: Context,
    private val appsRepository: ApplicationRepository
): AddAppUseCase {
    override suspend operator fun invoke(packageName: String) {
        appsRepository.addApp(ApplicationModel(context.packageManager, packageName))
    }
}