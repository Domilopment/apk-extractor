package domilopment.apkextractor.di.preferenceDataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.repository.preferences.MyPreferenceRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val PREFERENCES = "settings"
private const val SHARED_PREFERENCES = "${BuildConfig.APPLICATION_ID}_preferences"

@InstallIn(SingletonComponent::class)
@Module
abstract class DataStoreModule {
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        myPreferencesRepository: MyPreferenceRepository
    ): PreferenceRepository

    companion object {
        @Singleton
        @Provides
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }),
                migrations = listOf(SharedPreferencesMigration(context, SHARED_PREFERENCES)),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { context.preferencesDataStoreFile(PREFERENCES) })
        }
    }
}