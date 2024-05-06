package domilopment.apkextractor.dependencyInjection.useCase

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCase
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.UninstallAppUseCase
import domilopment.apkextractor.domain.usecase.appList.UninstallAppUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.UpdateAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.UpdateAppsUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object AppListUseCaseModule {
    @Provides
    @Reusable
    fun getAddAppUseCase(
        @ApplicationContext context: Context, appsRepository: ApplicationRepository
    ): AddAppUseCase {
        return AddAppUseCaseImpl(context, appsRepository)
    }

    @Provides
    @Reusable
    fun getGetAppListUseCase(
        @ApplicationContext context: Context,
        appsRepository: ApplicationRepository,
        settings: PreferenceRepository
    ): GetAppListUseCase {
        return GetAppListUseCaseImpl(context, appsRepository, settings)
    }

    @Provides
    @Reusable
    fun getSaveAppsUseCase(
        @ApplicationContext context: Context,
        apkRepository: PackageArchiveRepository,
        settings: PreferenceRepository
    ): SaveAppsUseCase {
        return SaveAppsUseCaseImpl(context, apkRepository, settings)
    }

    @Provides
    @Reusable
    fun getShareAppsUseCase(
        @ApplicationContext context: Context, settings: PreferenceRepository
    ): ShareAppsUseCase {
        return ShareAppsUseCaseImpl(context, settings)
    }

    @Provides
    @Reusable
    fun getUninstallAppUseCase(
        @ApplicationContext context: Context, appsRepository: ApplicationRepository
    ): UninstallAppUseCase {
        return UninstallAppUseCaseImpl(context, appsRepository)
    }

    @Provides
    @Reusable
    fun getUpdateAppsUseCase(appsRepository: ApplicationRepository): UpdateAppsUseCase {
        return UpdateAppsUseCaseImpl(appsRepository)
    }
}