package domilopment.apkextractor.domain.usecase.appList

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.repository.files.FilesRepository
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
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
    private val filesRepository: FilesRepository,
    private val apkRepository: PackageArchiveRepository,
    private val settingsRepository: PreferenceRepository,
) : SaveAppsUseCase {
    override operator fun invoke(list: List<ApplicationModel>) = callbackFlow {
        if (list.isEmpty()) {
            trySend(ExtractionResult.None)
            close()
        }

        val backupMode = settingsRepository.backupModeXapk.first()
        val appNameConfig = settingsRepository.appSaveName.first()
        val saveDir = settingsRepository.saveDir.first()
        var application: ApplicationModel? = null
        var errorMessage: String? = null

        list.forEach { app ->
            application = app
            val splits = arrayListOf(app.appSourceDirectory)
            if (!app.appSplitSourceDirectories.isNullOrEmpty() && backupMode) splits.addAll(
                app.appSplitSourceDirectories!!
            )
            val appName = ApplicationUtil.appName(app, appNameConfig)

            trySend(ExtractionResult.Progress(app, 0))

            val newFile = filesRepository.save(splits, saveDir!!, appName) {
                trySend(ExtractionResult.Progress(app, 1))
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
                            appVersionName = app.appVersionName,
                            appVersionCode = app.appVersionCode,
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
                        application!!, errorMessage!!
                    )
                )
                close()
            }
        }
        if (list.size == 1) trySend(ExtractionResult.SuccessSingle(application!!))
        else trySend(ExtractionResult.SuccessMultiple(application!!, list.size))
        close()
    }.flowOn(Dispatchers.IO)
}