package domilopment.apkextractor.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApkDao {
    @Query("SELECT * FROM saved_apks")
    fun getApks(): Flow<List<PackageArchiveEntity>>

    @Upsert
    suspend fun upsertApk(apk: PackageArchiveEntity)

    @Upsert
    suspend fun upsertApks(list: List<PackageArchiveEntity>)

    @Delete
    suspend fun deleteApk(apk: PackageArchiveEntity)

    @Delete
    suspend fun deleteApks(list: List<PackageArchiveEntity>)

    @Transaction
    suspend fun update(insert: List<PackageArchiveEntity>, delete: List<PackageArchiveEntity>) {
        upsertApks(insert)
        deleteApks(delete)
    }
}