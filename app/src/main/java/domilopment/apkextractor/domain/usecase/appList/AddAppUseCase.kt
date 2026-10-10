package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.settings.ApplicationUtil
import javax.inject.Inject

interface AddAppUseCase {
    suspend operator fun invoke(packageName: PackageName)
}

class AddAppUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context, private val appsRepository: ApplicationRepository
) : AddAppUseCase {
    override suspend operator fun invoke(packageName: PackageName) {
        ApplicationUtil.appModelFromPackageName(packageName, context.packageManager)?.let {
            appsRepository.addApp(it)
        }
    }
}