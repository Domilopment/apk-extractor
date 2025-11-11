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
import domilopment.apkextractor.domain.usecase.appList.GetAppDetailsUseCase
import domilopment.apkextractor.domain.usecase.appList.GetAppDetailsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCase
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCase
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.OpenAppShopDetailsUseCase
import domilopment.apkextractor.domain.usecase.appList.OpenAppShopDetailsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.OpenAppUseCase
import domilopment.apkextractor.domain.usecase.appList.OpenAppUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCase
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.SaveImageUseCase
import domilopment.apkextractor.domain.usecase.appList.SaveImageUseCaseImpl
import domilopment.apkextractor.domain.usecase.appList.ShowAppSettingsUseCase
import domilopment.apkextractor.domain.usecase.appList.ShowAppSettingsUseCaseImpl
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
    fun getAppDetailsUseCase(
        @ApplicationContext context: Context, settings: PreferenceRepository
    ): GetAppDetailsUseCase {
        return GetAppDetailsUseCaseImpl(context.packageManager, settings)
    }

    @Provides
    @Reusable
    fun getGetAppListUseCase(
        @ApplicationContext context: Context,
        isAppInstalledUseCase: IsAppInstalledUseCase,
        appsRepository: ApplicationRepository,
        settings: PreferenceRepository
    ): GetAppListUseCase {
        return GetAppListUseCaseImpl(
            context.packageManager, isAppInstalledUseCase, appsRepository, settings
        )
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
    fun getOpenAppShopDetailsUseCase(
        @ApplicationContext context: Context
    ): OpenAppShopDetailsUseCase {
        return OpenAppShopDetailsUseCaseImpl(context)
    }

    @Provides
    @Reusable
    fun getOpenAppUseCase(
        @ApplicationContext context: Context, removeAppUseCase: RemoveAppUseCase
    ): OpenAppUseCase {
        return OpenAppUseCaseImpl(context, removeAppUseCase)
    }

    @Provides
    @Reusable
    fun getRemoveAppUseCase(
        @ApplicationContext context: Context, appsRepository: ApplicationRepository
    ): RemoveAppUseCase {
        return RemoveAppUseCaseImpl(context, appsRepository)
    }

    @Provides
    @Reusable
    fun getSaveAppsUseCase(
        @ApplicationContext context: Context,
        filesRepository: FilesRepository,
        apkRepository: PackageArchiveRepository,
        settings: PreferenceRepository,
        getAppDetailsUseCase: GetAppDetailsUseCase
    ): SaveAppsUseCase {
        return SaveAppsUseCaseImpl(
            context, filesRepository, apkRepository, settings, getAppDetailsUseCase
        )
    }

    @Provides
    @Reusable
    fun getSaveImageUseCase(
        @ApplicationContext context: Context
    ): SaveImageUseCase {
        return SaveImageUseCaseImpl(context)
    }

    @Provides
    @Reusable
    fun getShareAppsUseCase(
        @ApplicationContext context: Context,
        settings: PreferenceRepository,
        getAppDetailsUseCase: GetAppDetailsUseCase
    ): ShareAppsUseCase {
        return ShareAppsUseCaseImpl(context, settings, getAppDetailsUseCase)
    }

    @Provides
    @Reusable
    fun getShowAppSettingsUseCase(
        @ApplicationContext context: Context
    ): ShowAppSettingsUseCase {
        return ShowAppSettingsUseCaseImpl(context)
    }

    @Provides
    @Reusable
    fun getUninstallAppUseCase(
        @ApplicationContext context: Context
    ): UninstallAppUseCase {
        return UninstallAppUseCaseImpl(context)
    }

    @Provides
    @Reusable
    fun getUpdateAppsUseCase(appsRepository: ApplicationRepository): UpdateAppsUseCase {
        return UpdateAppsUseCaseImpl(appsRepository)
    }
}