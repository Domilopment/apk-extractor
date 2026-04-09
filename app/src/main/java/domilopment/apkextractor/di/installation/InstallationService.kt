package domilopment.apkextractor.di.installation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.data.model.install.InstallationCallback
import domilopment.apkextractor.data.model.install.InstallationError
import domilopment.apkextractor.utils.InstallationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
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
import java.io.InputStream
import java.util.zip.ZipInputStream
import kotlin.jvm.Throws

class InstallationService private constructor(@param:ApplicationContext private val context: Context) {
    fun <T : Activity> install(
        fileUri: Uri, statusReceiver: Class<T>
    ): Flow<InstallationCallback> = callbackFlow {
        val sessionInfo = createInstallationSession(fileUri) ?: return@callbackFlow cancel()

        val sessionCallback = createSessionCallback(sessionInfo.initialSessionId)
        withContext(Dispatchers.Main) {
            context.packageManager.packageInstaller.registerSessionCallback(sessionCallback)
        }

        // Signal that the session is ready to receive files.
        send(InstallationCallback.OnPrepare(sessionInfo.session, sessionInfo.initialSessionId))

        try {
            // Read the file(s) and write them into the session.
            transferFilesToSession(sessionInfo.session, fileUri)
        } catch (e: IOException) {
            handleInstallationError(sessionInfo, fileUri, e)
        }

        try {
            // If still active, commit the session to start the installation prompt.
            if (isActive) {
                InstallationUtil.finishSession(
                    context, sessionInfo.session, sessionInfo.initialSessionId, fileUri, statusReceiver
                )
            }
        } catch (e: SecurityException) {
            handleInstallationError(sessionInfo, fileUri, e)
        }

        // Unregister the callback when the flow is closed.
        awaitClose {
            runBlocking {
                withContext(Dispatchers.Main) {
                    context.packageManager.packageInstaller.unregisterSessionCallback(
                        sessionCallback
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Provides a fallback mechanism using a generic ACTION_VIEW intent.
     */
    fun fallbackInstall(fileUri: Uri): Flow<InstallationCallback.InstallationResult> =
        callbackFlow {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
                trySend(InstallationCallback.InstallationResult.OnExtern(null))
                close()
            } catch (activityNotFound: ActivityNotFoundException) {
                trySend(
                    InstallationCallback.InstallationResult.OnError(
                        null, fileUri, InstallationError.ActivityNotFoundException(activityNotFound)
                    )
                )
                close()
            }
        }.flowOn(Dispatchers.Default)

    fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>) {
        InstallationUtil.uninstallApp(context, packageName, statusReceiver)
    }

    /**
     * Creates and opens a new PackageInstaller session.
     * Handles initial errors and the fallback mechanism.
     */
    private suspend fun ProducerScope<InstallationCallback>.createInstallationSession(fileUri: Uri): SessionInfo? {
        return try {
            val (session, sessionId) = InstallationUtil.createSession(context)
            SessionInfo(session, sessionId)
        } catch (e: Exception) {
            // Possibly IOException or SecurityException
            send(
                InstallationCallback.InstallationResult.OnError(
                    null, fileUri, InstallationError.SessionCreationException(e)
                )
            )
            null
        }
    }

    /**
     * Reads the provided file URI (APK, APKS, XAPK) and writes its contents into the session.
     */
    @Throws(IOException::class)
    private suspend fun ProducerScope<InstallationCallback>.transferFilesToSession(
        session: PackageInstaller.Session, fileUri: Uri
    ) {
        val mime = FileUtil.getDocumentInfo(
            context, fileUri, DocumentsContract.Document.COLUMN_MIME_TYPE
        )?.mimeType

        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            when (mime) {
                FileUtil.FileInfo.APK.mimeType -> transferSingleApk(session, inputStream, fileUri)
                FileUtil.FileInfo.APKS.mimeType, FileUtil.FileInfo.XAPK.mimeType -> transferSplitApks(
                    session, inputStream
                )
            }
        } ?: throw IOException("Could not open input stream for URI: $fileUri")
    }

    /**
     * Transfers a single APK file into the session.
     */
    private suspend fun ProducerScope<InstallationCallback>.transferSingleApk(
        session: PackageInstaller.Session, apkStream: InputStream, fileUri: Uri
    ) {
        send(InstallationCallback.OnProgress("Read file: base.apk", 0f))
        val length =
            FileUtil.getDocumentInfo(context, fileUri, DocumentsContract.Document.COLUMN_SIZE)?.size
                ?: -1
        if (isActive) InstallationUtil.addFileToSession(session, apkStream, "base.apk", length)
    }

    /**
     * Extracts and transfers multiple APKs from a ZIP-based file (APKS, XAPK) into the session.
     */
    private suspend fun ProducerScope<InstallationCallback>.transferSplitApks(
        session: PackageInstaller.Session, splitApkStream: InputStream
    ) {
        ZipInputStream(BufferedInputStream(splitApkStream)).use { input ->
            generateSequence { input.nextEntry }.filter { it.name.endsWith(".apk") }
                .forEachIndexed { index, entry ->
                    // Progress before reading
                    send(
                        InstallationCallback.OnProgress(
                            "Read file: ${entry.name}", calculateProgress(index)
                        )
                    )

                    if (isActive) InstallationUtil.addFileToSession(
                        session, input, entry.name, entry.size
                    )
                    input.closeEntry()

                    // Progress after reading
                    send(
                        InstallationCallback.OnProgress(
                            "Read file: ${entry.name}", calculateProgress(index + 1)
                        )
                    )
                }
        }
    }

    /**
     * Creates and returns a `PackageInstaller.SessionCallback` to handle installation events.
     */
    private fun ProducerScope<InstallationCallback>.createSessionCallback(initialSessionId: Int) =
        object : PackageInstaller.SessionCallback() {
            private var packageName: String? = null

            override fun onCreated(sessionId: Int) {
                if (sessionId != initialSessionId) return
                packageName =
                    context.packageManager.packageInstaller.getSessionInfo(sessionId)?.appPackageName

                trySend(InstallationCallback.OnProgress(packageName, 0F))
            }

            override fun onBadgingChanged(sessionId: Int) {
                // Not used
            }

            override fun onActiveChanged(sessionId: Int, active: Boolean) {
                // Not used
            }

            override fun onProgressChanged(sessionId: Int, progress: Float) {
                if (sessionId != initialSessionId) return

                packageName =
                    context.packageManager.packageInstaller.getSessionInfo(sessionId)?.appPackageName
                trySend(InstallationCallback.OnProgress(packageName, progress))
            }

            override fun onFinished(sessionId: Int, success: Boolean) {
                if (sessionId != initialSessionId) return
                trySend(InstallationCallback.InstallationResult.OnFinished(packageName, success))
                close()
            }
        }

    /**
     * Handles exceptions during the installation process, abandoning the session and canceling the flow.
     */
    private fun ProducerScope<InstallationCallback>.handleInstallationError(
        sessionInfo: SessionInfo, fileUri: Uri, e: Exception
    ) {
        if (isActive) {
            Timber.tag("InstallationService").e(e)

            // Get the SessionInfo from the PackageInstaller
            val info =
                context.packageManager.packageInstaller.getSessionInfo(sessionInfo.initialSessionId)

            val installationError = when (e) {
                is IOException -> InstallationError.IOException(e)
                else -> InstallationError.UnknownException(e)
            }

            trySend(
                InstallationCallback.InstallationResult.OnError(
                    info?.appPackageName, fileUri, installationError
                )
            )
            sessionInfo.session.abandon()
            cancel()
        }
    }

    /**
     * Calculates installation progress for split APKs based on a fixed percentage.
     */
    private fun calculateProgress(completedSteps: Int) =
        MAX_PROGRESS * completedSteps / (completedSteps + 2)

    private data class SessionInfo(
        val session: PackageInstaller.Session, val initialSessionId: Int
    )

    companion object {
        private const val MAX_PROGRESS = 0.80f

        @Volatile
        private lateinit var INSTANCE: InstallationService

        fun getInstallationService(context: Context): InstallationService {
            synchronized(this) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = InstallationService(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }
}