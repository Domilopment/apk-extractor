package domilopment.apkextractor.di.useCase

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.files.FilesRepository
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCase
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCase
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCase
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCaseImpl
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
        isAppInstalledUseCase: IsAppInstalledUseCase,
        appsRepository: ApplicationRepository,
        settings: PreferenceRepository
    ): GetAppListUseCase {
        return GetAppListUseCaseImpl(isAppInstalledUseCase, appsRepository, settings)
    }

    @Provides
    @Reusable
    fun getIsAppInstalledUseCase(
        @ApplicationContext context: Context
    ): IsAppInstalledUseCase {
        return IsAppInstalledUseCaseImpl(context)
    }

    @Provides
    @Reusable
    fun getSaveAppsUseCase(
        filesRepository: FilesRepository,
        apkRepository: PackageArchiveRepository,
        settings: PreferenceRepository
    ): SaveAppsUseCase {
        return SaveAppsUseCaseImpl(filesRepository, apkRepository, settings)
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
    ): RemoveAppUseCase {
        return RemoveAppUseCaseImpl(context, appsRepository)
    }

    @Provides
    @Reusable
    fun getUpdateAppsUseCase(appsRepository: ApplicationRepository): UpdateAppsUseCase {
        return UpdateAppsUseCaseImpl(appsRepository)
    }
}