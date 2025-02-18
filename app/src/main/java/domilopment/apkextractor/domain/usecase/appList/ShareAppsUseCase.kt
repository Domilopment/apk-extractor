package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.net.Uri
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.mapper.AppModelToApplicationModelMapper
import domilopment.apkextractor.domain.mapper.ApplicationModelToAppModelMapper
import domilopment.apkextractor.domain.mapper.mapAll
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface ShareAppsUseCase {
    operator fun invoke(list: List<ApplicationModel>): Flow<ShareResult>
}

class ShareAppsUseCaseImpl @Inject constructor(
    private val context: Context, private val settings: PreferenceRepository
) : ShareAppsUseCase {
    override operator fun invoke(list: List<ApplicationModel>) = callbackFlow {
        if (list.isEmpty()) {
            trySend(ShareResult.None)
            close()
        }

        val files = ArrayList<Uri>()
        val backupMode = settings.backupModeXapk.first()

        val tasks =
            ApplicationModelToAppModelMapper(context.packageManager).mapAll(list).filterNotNull()

        val taskSize = if (backupMode) tasks.fold(0) { acc, applicationModel ->
            acc + (applicationModel.applicationInfo.splitSourceDirs?.size ?: 0) + 1
        } else list.size

        trySend(ShareResult.Init(taskSize))

        tasks.forEach { app ->
            val appInfo = AppModelToApplicationModelMapper(context.packageManager).map(app)

            val splits = arrayListOf(app.applicationInfo.sourceDir)
            if (!app.applicationInfo.splitSourceDirs.isNullOrEmpty() && backupMode) splits.addAll(
                app.applicationInfo.splitSourceDirs!!
            )
            val name =
                Utils.getPackageInfo(context.packageManager, app.applicationInfo.packageName).let {
                    ApplicationUtil.appName(
                        context.packageManager, it, settings.appSaveName.first()
                    )
                }

            val uri = if (splits.size == 1) {
                val shareUri = ApplicationUtil.shareApk(context, appInfo, name)
                trySend(ShareResult.Progress)
                shareUri
            } else ApplicationUtil.shareXapk(
                context, appInfo, name
            ) { trySend(ShareResult.Progress) }
            files.add(uri)
        }
        if (files.size == 1) trySend(ShareResult.SuccessSingle(files[0]))
        else trySend(ShareResult.SuccessMultiple(files))
        close()
    }.flowOn(Dispatchers.IO)
}