package domilopment.apkextractor.data.repository.packageArchive

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.room.dao.ApkDao
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.data.sources.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PackageArchiveRepository {
    val apks: Flow<List<PackageArchiveEntity>>
    suspend fun updateApps()
    suspend fun addApk(apk: PackageArchiveEntity)
    suspend fun removeApk(apk: PackageArchiveEntity)
    suspend fun updateApp(apk: PackageArchiveEntity)
}

class MyPackageArchiveRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val apkDao: ApkDao,
    private val packageArchiveService: ListOfAPKs,
    private val settings: PreferenceRepository,
) : PackageArchiveRepository {
    private val updateTrigger = MutableStateFlow(true)

    override val apks: Flow<List<PackageArchiveEntity>> =
        apkDao.getApks().combine(updateTrigger) { list, _ -> list }

    override suspend fun updateApps() = withContext(Dispatchers.IO) {
        val onDisk = packageArchiveService.apks.first()
        val inDb = apkDao.getApks().first()

        val mustAdd = onDisk.filter { disk -> disk.fileUri !in inDb.map { db -> db.fileUri } }
        val mustDelete = inDb.filter { db -> db.fileUri !in onDisk.map { disk -> disk.fileUri } }

        apkDao.update(mustAdd, mustDelete)

        val mustLoad = inDb.filter { !it.loaded && it !in mustDelete }

        val sortOrder = settings.apkSortOrder.first()
        val load = (mustAdd + mustLoad).toSortedSet(sortOrder.comparator)
        load.forEach {
            val loaded = PackageArchiveDetailLoader.load(context, it)
            apkDao.upsertApk(loaded)
        }

        updateTrigger.update { state -> !state }
    }

    override suspend fun addApk(apk: PackageArchiveEntity) {
        apkDao.upsertApk(apk)
    }

    override suspend fun removeApk(apk: PackageArchiveEntity) {
        apkDao.deleteApk(apk)
    }

    override suspend fun updateApp(apk: PackageArchiveEntity): Unit = withContext(Dispatchers.IO) {
        PackageArchiveDetailLoader.load(context, apk).also {
            apkDao.upsertApk(it)
        }
    }
}