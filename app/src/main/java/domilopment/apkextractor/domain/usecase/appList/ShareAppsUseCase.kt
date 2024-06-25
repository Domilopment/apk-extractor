package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.net.Uri
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
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

        list.forEach { app ->
            val splits = arrayListOf(app.appSourceDirectory)
            if (!app.appSplitSourceDirectories.isNullOrEmpty() && backupMode) splits.addAll(
                app.appSplitSourceDirectories!!
            )
            val name = ApplicationUtil.appName(app, settings.appSaveName.first())

            val uri = if (splits.size == 1) {
                val shareUri = ApplicationUtil.shareApk(context, app, name)
                trySend(ShareResult.Progress)
                shareUri
            } else ApplicationUtil.shareXapk(
                context, app, name
            ) { trySend(ShareResult.Progress) }
            files.add(uri)
        }
        if (files.size == 1) trySend(ShareResult.SuccessSingle(files[0]))
        else trySend(ShareResult.SuccessMultiple(files))
        close()
    }.flowOn(Dispatchers.IO)
}