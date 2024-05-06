package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.provider.DocumentsContract
import domilopment.apkextractor.data.apkList.AppPackageArchiveModel
import domilopment.apkextractor.data.apkList.ZipPackageArchiveModel
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.data.appList.ExtractionResult
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface SaveAppsUseCase {
    operator fun invoke(list: List<ApplicationModel>): Flow<ExtractionResult>
}

class SaveAppsUseCaseImpl @Inject constructor(
    private val context: Context,
    private val apkRepository: PackageArchiveRepository,
    private val settingsRepository: PreferenceRepository,
): SaveAppsUseCase {
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

            val newFile = if (splits.size == 1) {
                val newApk = ApplicationUtil.saveApk(
                    context, app.appSourceDirectory, saveDir!!, appName
                )
                trySend(ExtractionResult.Progress(app, 1))
                newApk
            } else ApplicationUtil.saveXapk(
                context, splits.toTypedArray(), saveDir!!, appName
            ) {
                trySend(ExtractionResult.Progress(app, 1))
            }
            when (newFile) {
                is SaveApkResult.Failure -> errorMessage = newFile.errorMessage
                is SaveApkResult.Success -> newFile.uri.let { uri ->
                    FileUtil.getDocumentInfo(
                        context,
                        uri,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                        DocumentsContract.Document.COLUMN_SIZE
                    )?.let {
                        when {
                            it.displayName!!.endsWith(".apk") -> AppPackageArchiveModel(
                                it.uri, it.displayName, it.mimeType!!, it.lastModified!!, it.size!!
                            )

                            it.displayName.endsWith(".xapk") -> ZipPackageArchiveModel(
                                it.uri, it.displayName, it.mimeType!!, it.lastModified!!, it.size!!
                            )

                            else -> null
                        }
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