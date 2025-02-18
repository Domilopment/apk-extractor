package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.utils.settings.ApplicationUtil
import javax.inject.Inject

interface AddAppUseCase {
    suspend operator fun invoke(packageName: String)
}

class AddAppUseCaseImpl @Inject constructor(
    private val context: Context, private val appsRepository: ApplicationRepository
) : AddAppUseCase {
    override suspend operator fun invoke(packageName: String) {
        ApplicationUtil.appModelFromPackageName(packageName, context.packageManager)?.let {
            appsRepository.addApp(it)
        }
    }
}