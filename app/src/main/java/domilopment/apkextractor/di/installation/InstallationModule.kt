package domilopment.apkextractor.di.installation

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.installation.InstallationRepository
import domilopment.apkextractor.data.repository.installation.InstallationRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class InstallationModule {
    @Binds
    @Singleton
    abstract fun bindInstallationRepository(
        myInstallationRepository: InstallationRepositoryImpl
    ): InstallationRepository

    companion object {
        @Singleton
        @Provides
        fun provideInstallationDataSource(@ApplicationContext context: Context): InstallationService {
            return InstallationService.getInstallationService(context)
        }
    }
}