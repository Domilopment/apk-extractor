package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.repository.files.FilesRepository
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

interface SaveAppsUseCase {
    operator fun invoke(list: List<ApplicationModel>): Flow<ExtractionResult>
}

class SaveAppsUseCaseImpl @Inject constructor(
    private val context: Context,
    private val filesRepository: FilesRepository,
    private val apkRepository: PackageArchiveRepository,
    private val settingsRepository: PreferenceRepository,
    private val appDetailsUseCase: GetAppDetailsUseCase
) : SaveAppsUseCase {
    override operator fun invoke(list: List<ApplicationModel>) = callbackFlow {
        if (list.isEmpty()) {
            trySend(ExtractionResult.None)
            close()
            return@callbackFlow
        }

        val saveDir = settingsRepository.saveDir.first()
        if (saveDir == null) {
            trySend(ExtractionResult.NoSaveDir)
            close()
            return@callbackFlow
        }

        val backupMode = settingsRepository.backupModeApkBundle.first()
        val appNameConfig = settingsRepository.appSaveName.first()
        val appNameSpacer = settingsRepository.appSaveNameSpacer.first()
        val bundleFileInfo = settingsRepository.bundleFileInfo.first()
        var application: ApplicationModel? = null
        var errorMessage: String? = null

        val tasks = list.mapNotNull { appDetailsUseCase.invoke(it) }

        val taskSize = if (backupMode) tasks.fold(0) { acc, applicationModel ->
            acc + (applicationModel.appSplitSourceDirectories?.size ?: 0) + 1
        } else list.size

        trySend(ExtractionResult.Init(taskSize))

        tasks.forEach { app ->
            application = app

            val splits = mutableListOf(app.appSourceDirectory)
            val splitSources = app.appSplitSourceDirectories
            if (!splitSources.isNullOrEmpty() && backupMode) splits.addAll(splitSources)

            val packageInfo = Utils.getPackageInfo(context.packageManager, app.appPackageName)
            val appName = packageInfo.let {
                ApplicationUtil.appName(
                    context.packageManager, it, appNameConfig, appNameSpacer.symbol
                )
            }

            trySend(ExtractionResult.Progress(application, 0))

            val newFile = filesRepository.save(
                splits, saveDir, appName, bundleFileInfo.mimeType, bundleFileInfo.suffix
            ) {
                trySend(ExtractionResult.Progress(application, 1))
            }
            when (newFile) {
                is SaveApkResult.Failure -> {
                    Timber.tag("Save Apps Error").e(Exception(newFile.errorMessage))
                    errorMessage = newFile.errorMessage
                }

                is SaveApkResult.Success -> newFile.uri.let { uri ->
                    filesRepository.fileInfo(uri)?.let {
                        PackageArchiveEntity(
                            fileUri = it.uri,
                            fileName = it.displayName!!,
                            fileType = it.mimeType!!,
                            fileLastModified = it.lastModified!!,
                            fileSize = it.size!!,
                            appName = app.appName,
                            appPackageName = app.appPackageName,
                            appIcon = app.appIcon.toBitmap().asImageBitmap(),
                            appVersionName = packageInfo.versionName,
                            appVersionCode = Utils.versionCode(packageInfo),
                            appMinSdkVersion = app.minSdkVersion,
                            appTargetSdkVersion = app.targetSdkVersion,
                            loaded = true,
                        )
                    }?.also {
                        apkRepository.addApk(it)
                    }
                }
            }
            if (errorMessage != null) {
                trySend(
                    ExtractionResult.Failure(
                        application, errorMessage
                    )
                )
                cancel()
            }
        }
        if (list.size == 1) trySend(ExtractionResult.SuccessSingle(application!!))
        else trySend(ExtractionResult.SuccessMultiple(application!!, list.size))
        close()
    }.flowOn(Dispatchers.IO)
}