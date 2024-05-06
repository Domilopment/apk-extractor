package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface LoadApkInfoUseCase {
    suspend operator fun invoke(
        apk: PackageArchiveModel, forceRefresh: Boolean = false
    ): PackageArchiveModel
}

class LoadApkInfoUseCaseImpl(private val context: Context) : LoadApkInfoUseCase {
    override suspend operator fun invoke(
        apk: PackageArchiveModel, forceRefresh: Boolean
    ): PackageArchiveModel {
        return withContext(Dispatchers.IO) {
            if (forceRefresh) apk.forceRefresh(context) else apk.packageArchiveInfo(context)
        }
    }
}