package domilopment.apkextractor.di.logger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.data.repository.logger.CrashlyticsLogger
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class LoggerModule {
    @Binds
    @Singleton
    abstract fun bindLogger(
        logger: CrashlyticsLogger
    ): Timber.Tree

    companion object {
        @Singleton
        @Provides
        fun provideLogger(): CrashlyticsLogger {
            return CrashlyticsLogger()
        }
    }
}