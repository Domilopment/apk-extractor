package domilopment.apkextractor.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import domilopment.apkextractor.data.room.converter.ImageConverter
import domilopment.apkextractor.data.room.converter.UriConverter
import domilopment.apkextractor.data.room.dao.ApkDao
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity

@Database(
    entities = [PackageArchiveEntity::class],
    version = 1
)
@TypeConverters(UriConverter::class, ImageConverter::class)
abstract class ApkDatabase: RoomDatabase() {
    abstract fun apkDao(): ApkDao
}