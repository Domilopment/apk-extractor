package domilopment.apkextractor.utils

import domilopment.apkextractor.data.PackageArchiveModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PackageArchiveRepository(private val packageArchiveService: ListOfAPKs) {
    val apks: Flow<List<PackageArchiveModel>> = packageArchiveService.apks

    suspend fun updateApps() {
        withContext(Dispatchers.IO) {
            packageArchiveService.updateData()
        }
    }

    suspend fun add(packageArchive: PackageArchiveModel) {
        withContext(Dispatchers.IO) {
            packageArchiveService.add(packageArchive)
        }
    }

    suspend fun remove(packageArchive: PackageArchiveModel) {
        withContext(Dispatchers.IO) {
            packageArchiveService.remove(packageArchive)
        }
    }
}