package domilopment.apkextractor.ui.viewModels

import android.app.Application
import android.content.Context
import android.content.pm.PackageInstaller
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.InstallXapkActivity
import domilopment.apkextractor.MySessionCallback
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.InstallationUtil
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class InstallXapkActivityViewModel(application: Application) : AndroidViewModel(application) {
    val context: Context get() = getApplication<Application>().applicationContext

    var uiState by mutableStateOf(
        ProgressDialogUiState(
            title = context.getString(R.string.progress_dialog_title_install, "XAPK"),
            process = null,
            progress = 0F,
            tasks = 100,
            shouldBeShown = true
        )
    )
        private set

    private val sessionCallback = object : MySessionCallback() {
        val packageInstaller = context.packageManager.packageInstaller
        private var packageName: String? = null
        override var initialSessionId: Int = -1

        override fun onCreated(sessionId: Int) {
            if (sessionId != initialSessionId) return
            packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName

            updateState(packageName, 0F)
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
            updateState(packageName, progress)
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            if (sessionId != initialSessionId) return

            packageInstaller.unregisterSessionCallback(this)
            setProgressDialogActive(false)
            if (success && packageName != null) EventDispatcher.emitEvent(
                Event(EventType.INSTALLED, packageName)
            )
        }
    }

    private var session: PackageInstaller.Session? = null
    private var task: Job? = null

    fun updateState(packageName: String? = uiState.process, progress: Float = uiState.progress) {
        uiState = uiState.copy(process = packageName, progress = progress * 100)
    }

    fun setProgressDialogActive(active: Boolean) {
        uiState = uiState.copy(shouldBeShown = active)
    }

    fun installXAPK(fileUri: Uri) {
        task = viewModelScope.launch {
            val packageInstaller = context.applicationContext.packageManager.packageInstaller
            val contentResolver = context.applicationContext.contentResolver
            packageInstaller.registerSessionCallback(sessionCallback)

            withContext(Dispatchers.IO) {
                val (session, sessionId) = InstallationUtil.createSession(context)
                this@InstallXapkActivityViewModel.session = session
                sessionCallback.initialSessionId = sessionId

                val mime = FileUtil.getDocumentInfo(
                    context, fileUri, DocumentsContract.Document.COLUMN_MIME_TYPE
                )?.mimeType
                try {
                    when (mime) {
                        FileUtil.FileInfo.APK.mimeType -> contentResolver.openInputStream(fileUri)
                            ?.use { apkStream ->
                                withContext(Dispatchers.Main) {
                                    updateState("Read file: base.apk")
                                }

                                val length = FileUtil.getDocumentInfo(
                                    context, fileUri, DocumentsContract.Document.COLUMN_SIZE
                                )?.size ?: -1

                                if (this@InstallXapkActivityViewModel.session != null) InstallationUtil.addFileToSession(
                                    session, apkStream, "base.apk", length
                                )
                            }

                        "application/octet-stream" -> contentResolver.openInputStream(fileUri)
                            ?.use { xApkStream ->
                                ZipInputStream(BufferedInputStream(xApkStream)).use { input ->
                                    val maxProgress = 0.80f
                                    var currentProgress = 1
                                    generateSequence { input.nextEntry }.filter { it.name.endsWith(".apk") }
                                        .forEach { entry ->
                                            val progress =
                                                maxProgress * currentProgress / (currentProgress + 2)
                                            withContext(Dispatchers.Main) {
                                                updateState("Read file: ${entry.name}")
                                            }
                                            if (this@InstallXapkActivityViewModel.session != null) InstallationUtil.addFileToSession(
                                                session, input, entry.name, entry.size
                                            )
                                            withContext(Dispatchers.Main) {
                                                updateState(progress = progress)
                                            }
                                            currentProgress += 1
                                            input.closeEntry()
                                        }
                                }
                            }
                    }
                } catch (e: IOException) {
                    // Thrown if Session is abandoned
                }

                if (this@InstallXapkActivityViewModel.session != null) InstallationUtil.finishSession(
                    context, session, sessionId, InstallXapkActivity::class.java
                )
            }
        }
    }

    fun cancel() {
        task?.cancel()
        task = null
        val temp = session
        session = null
        temp?.abandon()
    }
}
