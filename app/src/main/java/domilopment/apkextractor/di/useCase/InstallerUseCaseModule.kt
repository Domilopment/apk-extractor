package domilopment.apkextractor.di.useCase

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import domilopment.apkextractor.data.repository.installation.InstallationRepository
import domilopment.apkextractor.domain.usecase.installer.InstallUseCase
import domilopment.apkextractor.domain.usecase.installer.InstallUseCaseImpl
import domilopment.apkextractor.domain.usecase.installer.UninstallUseCase
import domilopment.apkextractor.domain.usecase.installer.UninstallUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object InstallerUseCaseModule {
    @Provides
    @Reusable
    fun getInstallUseCase(
        installationsRepository: InstallationRepository
    ): InstallUseCase {
        return InstallUseCaseImpl(installationsRepository)
    }

    @Provides
    @Reusable
    fun getUninstallUseCase(
        installationsRepository: InstallationRepository
    ): UninstallUseCase {
        return UninstallUseCaseImpl(installationsRepository)
    }
}
