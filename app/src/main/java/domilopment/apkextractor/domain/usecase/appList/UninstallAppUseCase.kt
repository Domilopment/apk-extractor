package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import domilopment.apkextractor.utils.Utils
import javax.inject.Inject

interface UninstallAppUseCase {
    suspend operator fun invoke(packageName: String)
}

class UninstallAppUseCaseImpl @Inject constructor(
    private val context: Context,
    private val appsRepository: ApplicationRepository
): UninstallAppUseCase {
    override suspend operator fun invoke(packageName: String) {
        if (Utils.isPackageInstalled(context.packageManager, packageName)) return

        appsRepository.removeApp(ApplicationModel(context.packageManager, packageName))
    }
}