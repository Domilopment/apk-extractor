package domilopment.apkextractor.dependencyInjection.files

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class FilesModule {
    @Binds
    @Singleton
    abstract fun bindFilesRepository(
        filesRepository: FilesRepositoryImpl
    ): FilesRepository

    companion object {
        @Singleton
        @Provides
        fun provideFilesDataSource(@ApplicationContext context: Context): FilesService {
            return FilesService(context)
        }
    }
}