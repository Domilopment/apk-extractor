package domilopment.apkextractor.di.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepository
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepositoryImpl

import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepository: AnalyticsRepositoryImpl
    ): AnalyticsRepository
}