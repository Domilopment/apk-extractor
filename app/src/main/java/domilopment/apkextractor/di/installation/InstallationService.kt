package domilopment.apkextractor.di.installation

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInstaller
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.sources.ListOfApps
import domilopment.apkextractor.domain.mapper.AppModelToApplicationModelMapper
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.InstallApkResult
import domilopment.apkextractor.utils.InstallationUtil
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class InstallationService private constructor(@ApplicationContext private val context: Context) {
    fun <T : Activity> install(
        fileUri: Uri, statusReceiver: Class<T>
    ): Flow<InstallApkResult> = callbackFlow {
        val packageInstaller = context.packageManager.packageInstaller
        val contentResolver = context.applicationContext.contentResolver

        var packageName: String? = null

        val (session, initialSessionId) = try {
            InstallationUtil.createSession(context)
        } catch (e: IOException) {
            trySend(
                InstallApkResult.OnFinish.OnError(
                    packageName, e.message ?: "Unknown create session IO Exception"
                )
            )
            cancel()
            return@callbackFlow
        }

        val sessionCallback = object : PackageInstaller.SessionCallback() {
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
                    val app = packageName?.let {
                        ApplicationUtil.appModelFromPackageName(it, context.packageManager)
                    }?.let {
                        AppModelToApplicationModelMapper(context.packageManager).map(it)
                    }
                    trySend(InstallApkResult.OnFinish.OnSuccess(app))
                } else {
                    trySend(InstallApkResult.OnFinish.OnFinished(packageName))
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

                FileUtil.FileInfo.APKS.mimeType, FileUtil.FileInfo.XAPK.mimeType -> contentResolver.openInputStream(
                    fileUri
                )?.use { splitApkStream ->
                        ZipInputStream(BufferedInputStream(splitApkStream)).use { input ->
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
                                    input.closeEntry()

                                    send(
                                        InstallApkResult.OnProgress(
                                            "Read file: ${entry.name}",
                                            calculateProgress(currentProcess)
                                        )
                                    )

                                    currentProcess += 1
                                }
                        }
                    }
            }
        } catch (e: IOException) {
            // Also thrown if Session is abandoned
            if (isActive) {
                Timber.tag("InstallationService:finishSession-SecurityException").e(e)
                trySend(
                    InstallApkResult.OnFinish.OnError(
                        packageName, e.message ?: "Error while reading or adding installation file"
                    )
                )

                session.abandon()
                cancel()
            }
        }

        if (isActive) try {
            InstallationUtil.finishSession(
                context, session, initialSessionId, statusReceiver
            )
        } catch (e: SecurityException) {
            Timber.tag("InstallationService:finishSession-SecurityException").e(e)
            trySend(
                InstallApkResult.OnFinish.OnError(
                    packageName, e.message ?: "Unknown finish session Security Exception"
                )
            )
            session.abandon()
            cancel()
        }

        awaitClose {
            runBlocking {
                withContext(Dispatchers.Main) {
                    packageInstaller.unregisterSessionCallback(
                        sessionCallback
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>) {
        InstallationUtil.uninstallApp(context, packageName, statusReceiver)
    }

    private fun calculateProgress(process: Int) = MAX_PROGRESS * process / (process + 2)

    companion object {
        private const val MAX_PROGRESS = 0.80f

        private lateinit var INSTANCE: InstallationService

        fun getInstallationService(context: Context): InstallationService {
            synchronized(ListOfApps::class.java) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = InstallationService(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }
}