package domilopment.apkextractor.di.analytics

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.analytics.AnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.FirebaseAnalyticsHelper
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AnalyticsHelperMModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsHelper: FirebaseAnalyticsHelper
    ): AnalyticsHelper

    companion object {
        @Singleton
        @Provides
        fun provideAnalyticsHelper(@ApplicationContext context: Context): FirebaseAnalyticsHelper {
            return FirebaseAnalyticsHelper(context)
        }
    }
}