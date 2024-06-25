package domilopment.apkextractor.di.applications

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.applications.MyApplicationRepository
import domilopment.apkextractor.data.sources.ListOfApps
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ApplicationModule {
    @Binds
    @Singleton
    abstract fun bindApplicationRepository(
        myPreferencesRepository: MyApplicationRepository
    ): ApplicationRepository

    companion object {
        @Singleton
        @Provides
        fun provideApplicationDataSource(@ApplicationContext context: Context): ListOfApps {
            return ListOfApps.getApplications(context)
        }
    }
}