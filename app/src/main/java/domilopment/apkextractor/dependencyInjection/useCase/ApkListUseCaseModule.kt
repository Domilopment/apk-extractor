package domilopment.apkextractor.dependencyInjection.useCase

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCase
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCaseImpl
import domilopment.apkextractor.domain.usecase.apkList.GetApkListUseCase
import domilopment.apkextractor.domain.usecase.apkList.GetApkListUseCaseImpl
import domilopment.apkextractor.domain.usecase.apkList.LoadApkInfoUseCase
import domilopment.apkextractor.domain.usecase.apkList.LoadApkInfoUseCaseImpl
import domilopment.apkextractor.domain.usecase.apkList.UpdateApksUseCase
import domilopment.apkextractor.domain.usecase.apkList.UpdateApksUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object ApkListUseCaseModule {
    @Provides
    @Reusable
    fun getDeleteApkUseCase(
        @ApplicationContext context: Context, apksRepository: PackageArchiveRepository
    ): DeleteApkUseCase {
        return DeleteApkUseCaseImpl(context, apksRepository)
    }

    @Provides
    @Reusable
    fun getGetApkListUseCase(
        @ApplicationContext context: Context,
        apksRepository: PackageArchiveRepository,
        settings: PreferenceRepository
    ): GetApkListUseCase {
        return GetApkListUseCaseImpl(context, apksRepository, settings)
    }

    @Provides
    @Reusable
    fun getLoadApkInfoUseCase(apksRepository: PackageArchiveRepository): LoadApkInfoUseCase {
        return LoadApkInfoUseCaseImpl(apksRepository)
    }

    @Provides
    @Reusable
    fun getUpdateApksUseCase(apksRepository: PackageArchiveRepository): UpdateApksUseCase {
        return UpdateApksUseCaseImpl(apksRepository)
    }
}