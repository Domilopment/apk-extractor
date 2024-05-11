package domilopment.apkextractor.dependencyInjection.packageArchive

import domilopment.apkextractor.data.apkList.PackageArchiveModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PackageArchiveRepository {
    val apks: Flow<List<PackageArchiveModel>>
    suspend fun updateApps()
    suspend fun addApk(apk: PackageArchiveModel)
    suspend fun removeApk(apk: PackageArchiveModel)
}

class MyPackageArchiveRepository @Inject constructor(
    private val packageArchiveService: ListOfAPKs
) : PackageArchiveRepository {
    override val apks: Flow<List<PackageArchiveModel>> = packageArchiveService.apks

    override suspend fun updateApps() {
        packageArchiveService.updateData()
    }

    override suspend fun addApk(apk: PackageArchiveModel) {
        packageArchiveService.add(apk)
    }

    override suspend fun removeApk(apk: PackageArchiveModel) {
        packageArchiveService.remove(apk)
    }
}