package domilopment.apkextractor.di.analytics

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.analytics.AnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.DebugAnalyticsHelper
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AnalyticsHelperModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsHelper(
        analyticsHelper: DebugAnalyticsHelper
    ): AnalyticsHelper

    companion object {
        @Singleton
        @Provides
        fun provideAnalyticsHelper(): DebugAnalyticsHelper {
            return DebugAnalyticsHelper()
        }
    }
}
