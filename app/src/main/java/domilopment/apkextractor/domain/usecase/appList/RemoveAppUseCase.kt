package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.ApplicationUtil
import javax.inject.Inject

interface RemoveAppUseCase {
    suspend operator fun invoke(packageName: String)
}

class RemoveAppUseCaseImpl @Inject constructor(
    private val context: Context,
    private val appsRepository: ApplicationRepository
): RemoveAppUseCase {
    override suspend operator fun invoke(packageName: String) {
        if (Utils.isPackageInstalled(context.packageManager, packageName)) return

        ApplicationUtil.appModelFromPackageName(packageName, context.packageManager)?.let {
            appsRepository.removeApp(it)
        }

    }
}