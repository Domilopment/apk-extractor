package domilopment.apkextractor.data.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.room.dao.ApkDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideApkDao(apkDatabase: ApkDatabase): ApkDao {
        return apkDatabase.apkDao()
    }

    @Provides
    @Singleton
    fun getDatabase(@ApplicationContext context: Context): ApkDatabase {
        return Room.databaseBuilder(context, ApkDatabase::class.java, "apk_db").build()
    }
}