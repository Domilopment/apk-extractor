package domilopment.apkextractor.dependencyInjection.useCase

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.installer.InstallUseCase
import domilopment.apkextractor.domain.usecase.installer.InstallUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object InstallerUseCaseModule {
    @Provides
    @Reusable
    fun getAddAppUseCase(
        @ApplicationContext context: Context, addAppUseCase: AddAppUseCase
    ): InstallUseCase {
        return InstallUseCaseImpl(context, addAppUseCase)
    }
}
