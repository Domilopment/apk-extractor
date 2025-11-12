package domilopment.apkextractor.di.useCase

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.prefs.SaveDirUriUseCase
import domilopment.apkextractor.domain.usecase.prefs.SaveDirUriUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object PreferenceUseCaseModule {
    @Provides
    @Reusable
    fun getSaveDirUriUseCase(
        @ApplicationContext context: Context, settings: PreferenceRepository
    ): SaveDirUriUseCase {
        return SaveDirUriUseCaseImpl(context.contentResolver, settings)
    }
}