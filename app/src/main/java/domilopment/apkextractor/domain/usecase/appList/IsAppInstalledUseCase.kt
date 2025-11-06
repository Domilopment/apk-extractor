package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.Utils

interface IsAppInstalledUseCase {
    suspend operator fun invoke(packageName: PackageName): Boolean
}

class IsAppInstalledUseCaseImpl(private val context: Context) : IsAppInstalledUseCase {
    override suspend fun invoke(packageName: PackageName): Boolean {
        return Utils.isPackageInstalled(context.packageManager, packageName)
    }
}