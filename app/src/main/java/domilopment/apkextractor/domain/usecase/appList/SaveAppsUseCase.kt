package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.R
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.repository.files.FilesRepository
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.repository.tasks.TaskState
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.domain.usecase.tasks.LaunchTaskUseCase
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

interface SaveAppsUseCase {
    suspend operator fun invoke(list: List<ApplicationModel>): String
}

class SaveAppsUseCaseImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val filesRepository: FilesRepository,
    private val apkRepository: PackageArchiveRepository,
    private val settingsRepository: PreferenceRepository,
    private val appDetailsUseCase: GetAppDetailsUseCase,
    private val launchTask: LaunchTaskUseCase,
) : SaveAppsUseCase {
    override suspend operator fun invoke(list: List<ApplicationModel>): String {
        return launchTask.invoke(
            initialState = TaskState.Progress(
                title = UiText(R.string.progress_dialog_title_save)
            )
        ) {
            if (list.isEmpty()) {
                return@invoke ExtractionResult.None
            }

            val saveDir =
                settingsRepository.saveDir.first() ?: return@invoke ExtractionResult.NoSaveDir

            val backupMode = settingsRepository.backupModeApkBundle.first()
            val appNameConfig = settingsRepository.appSaveName.first()
            val appNameSpacer = settingsRepository.appSaveNameSpacer.first()
            val bundleFileInfo = settingsRepository.bundleFileInfo.first()
            var application: ApplicationModel? = null
            var errorMessage: String? = null

            val tasks = withContext(Dispatchers.IO) {
                list.mapNotNull { appDetailsUseCase.invoke(it) }
            }

            val taskSize = if (backupMode) tasks.fold(0) { acc, applicationModel ->
                acc + (applicationModel.appSplitSourceDirectories?.size ?: 0) + 1
            } else list.size

            updateProgress { it.copy(tasks = taskSize) }

            withContext(Dispatchers.IO) {
                tasks.forEach { app ->
                    ensureActive()
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

                    updateProgress { state ->
                        state.copy(process = application.appPackageName, progress = state.progress)
                    }

                    val newFile = filesRepository.save(
                        splits, saveDir, appName, bundleFileInfo.mimeType, bundleFileInfo.suffix
                    ) {
                        updateProgress { state ->
                            state.copy(progress = state.progress + 1)
                        }
                    }
                    when (newFile) {
                        is SaveApkResult.Failure -> {
                            Timber.tag("Save Apps Error").e(Exception(newFile.errorMessage))
                            errorMessage = newFile.errorMessage
                            return@withContext
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
                }
            }

            if (errorMessage != null) ExtractionResult.Failure(application!!, errorMessage)
            else if (list.size == 1) ExtractionResult.SuccessSingle(application!!)
            else ExtractionResult.SuccessMultiple(application!!, list.size)
        }
    }
}
