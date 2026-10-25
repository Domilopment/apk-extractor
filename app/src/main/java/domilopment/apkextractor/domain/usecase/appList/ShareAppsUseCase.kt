package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.net.Uri
import domilopment.apkextractor.R
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.repository.tasks.TaskExecutionPolicy
import domilopment.apkextractor.data.repository.tasks.TaskState
import domilopment.apkextractor.domain.usecase.tasks.LaunchTaskUseCase
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShareAppsUseCase {
    suspend operator fun invoke(list: List<ApplicationModel>): String
}

class ShareAppsUseCaseImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: PreferenceRepository,
    private val appDetailsUseCase: GetAppDetailsUseCase,
    private val launchTask: LaunchTaskUseCase,
) : ShareAppsUseCase {
    override suspend operator fun invoke(list: List<ApplicationModel>): String {
        return launchTask.invoke(
            initialState = TaskState.Progress(
                title = UiText(R.string.progress_dialog_title_share)
            ),
            executionPolicy = TaskExecutionPolicy.IMMEDIATE
        ) {
            if (list.isEmpty()) {
                fail(ShareResult.ShareError.None)
            }

            val files = ArrayList<Uri>()
            val backupMode = settings.backupModeApkBundle.first()
            val bundleFileInfo = settings.bundleFileInfo.first()

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
                    val splits = arrayListOf(app.appSourceDirectory)
                    if (!app.appSplitSourceDirectories.isNullOrEmpty() && backupMode) splits.addAll(
                        app.appSplitSourceDirectories
                    )
                    val name =
                        Utils.getPackageInfo(context.packageManager, app.appPackageName).let {
                            ApplicationUtil.appName(
                                context.packageManager,
                                it,
                                settings.appSaveName.first(),
                                settings.appSaveNameSpacer.first().symbol
                            )
                        }

                    val uri = if (splits.size == 1) {
                        val shareUri = ApplicationUtil.shareApk(context, app, name)
                        updateProgress { state ->
                            state.copy(progress = state.progress + 1)
                        }
                        shareUri
                    } else ApplicationUtil.shareApkBundle(
                        context, app, name, bundleFileInfo.suffix
                    ) {
                        updateProgress { state ->
                            state.copy(progress = state.progress + 1)
                        }
                    }
                    files.add(uri)
                }
            }

            if (files.size == 1) ShareResult.ShareSuccess.SuccessSingle(files[0])
            else ShareResult.ShareSuccess.SuccessMultiple(files)
        }
    }
}
