package domilopment.apkextractor.domain.usecase.installer

import android.content.Context
import android.content.pm.PackageInstaller
import android.net.Uri
import android.provider.DocumentsContract
import domilopment.apkextractor.InstallerActivity
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.InstallApkResult
import domilopment.apkextractor.utils.InstallationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

interface InstallUseCase {
    operator fun invoke(fileUri: Uri): Flow<InstallApkResult>
}

class InstallUseCaseImpl(
    private val context: Context, private val addApp: AddAppUseCase
) : InstallUseCase {
    override operator fun invoke(fileUri: Uri) = callbackFlow {
        val packageInstaller = context.packageManager.packageInstaller
        val contentResolver = context.applicationContext.contentResolver
        val (session, initialSessionId) = InstallationUtil.createSession(context)

        val sessionCallback = object : PackageInstaller.SessionCallback() {
            private var packageName: String? = null

            override fun onCreated(sessionId: Int) {
                if (sessionId != initialSessionId) return
                packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName

                trySend(InstallApkResult.OnProgress(packageName, 0F))
            }

            override fun onBadgingChanged(sessionId: Int) {
                // Not used
            }

            override fun onActiveChanged(sessionId: Int, active: Boolean) {
                // Not used
            }

            override fun onProgressChanged(sessionId: Int, progress: Float) {
                if (sessionId != initialSessionId) return

                packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName
                trySend(InstallApkResult.OnProgress(packageName, progress))
            }

            override fun onFinished(sessionId: Int, success: Boolean) {
                if (sessionId != initialSessionId) return

                if (success) {
                    packageName?.let {
                        runBlocking(coroutineContext) {
                            addApp(it)
                        }
                    }
                    trySend(InstallApkResult.OnSuccess(packageName))
                } else {
                    trySend(InstallApkResult.OnFail(packageName))
                }
                close()
            }
        }

        withContext(Dispatchers.Main) {
            packageInstaller.registerSessionCallback(sessionCallback)
        }

        trySend(InstallApkResult.OnPrepare(session, initialSessionId))

        val mime = FileUtil.getDocumentInfo(
            context, fileUri, DocumentsContract.Document.COLUMN_MIME_TYPE
        )?.mimeType
        try {
            when (mime) {
                FileUtil.FileInfo.APK.mimeType -> contentResolver.openInputStream(fileUri)
                    ?.use { apkStream ->
                        send(InstallApkResult.OnProgress("Read file: base.apk", 0f))

                        val length = FileUtil.getDocumentInfo(
                            context, fileUri, DocumentsContract.Document.COLUMN_SIZE
                        )?.size ?: -1

                        if (isActive) InstallationUtil.addFileToSession(
                            session, apkStream, "base.apk", length
                        )
                    }

                "application/octet-stream" -> contentResolver.openInputStream(fileUri)
                    ?.use { xApkStream ->
                        ZipInputStream(BufferedInputStream(xApkStream)).use { input ->
                            var currentProcess = 1
                            generateSequence { input.nextEntry }.filter { it.name.endsWith(".apk") }
                                .forEach { entry ->
                                    send(
                                        InstallApkResult.OnProgress(
                                            "Read file: ${entry.name}",
                                            calculateProgress(currentProcess - 1)
                                        )
                                    )

                                    if (isActive) InstallationUtil.addFileToSession(
                                        session, input, entry.name, entry.size
                                    )

                                    send(
                                        InstallApkResult.OnProgress(
                                            "Read file: ${entry.name}",
                                            calculateProgress(currentProcess)
                                        )
                                    )

                                    currentProcess += 1
                                    input.closeEntry()
                                }
                        }
                    }
            }
        } catch (e: IOException) {
            // Thrown if Session is abandoned
        }

        if (isActive) InstallationUtil.finishSession(
            context, session, initialSessionId, InstallerActivity::class.java
        )

        awaitClose { packageInstaller.unregisterSessionCallback(sessionCallback) }
    }.flowOn(Dispatchers.IO)

    private fun calculateProgress(process: Int) = maxProgress * process / (process + 2)

    companion object {
        private const val maxProgress = 0.80f
    }
}