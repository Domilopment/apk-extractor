package domilopment.apkextractor.ui.viewModels

import android.app.Application
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

class InstallXapkActivityViewModel(application: Application) : AndroidViewModel(application) {
    val context get() = getApplication<Application>().applicationContext

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

    fun updateState(packageName: String?, progress: Float) {
        uiState = uiState.copy(process = packageName, progress = progress * 100)
    }

    fun setProgressDialogActive(active: Boolean) {
        uiState = uiState.copy(shouldBeShown = active)
    }

    fun installXAPK(fileUri: Uri) {
        viewModelScope.launch(Dispatchers.Main) {
            val packageInstaller = context.applicationContext.packageManager.packageInstaller
            val contentResolver = context.applicationContext.contentResolver
            packageInstaller.registerSessionCallback(sessionCallback)

            withContext(Dispatchers.IO) {
                val (session, sessionId) = InstallationUtil.createSession(context)
                this@InstallXapkActivityViewModel.session = session
                sessionCallback.initialSessionId = sessionId

                val fileUtil = FileUtil(context)

                val mime = fileUtil.getDocumentInfo(
                    fileUri, DocumentsContract.Document.COLUMN_MIME_TYPE
                )?.mimeType
                when (mime) {
                    FileUtil.MIME_TYPE -> contentResolver.openInputStream(fileUri)
                        ?.use { apkStream ->
                            val length = FileUtil(context).getDocumentInfo(
                                fileUri, DocumentsContract.Document.COLUMN_SIZE
                            )?.size ?: -1

                            InstallationUtil.addFileToSession(
                                session, apkStream, "base.apk", length
                            )
                        }

                    "application/octet-stream" -> contentResolver.openInputStream(fileUri)
                        ?.use { xApkStream ->
                            ZipInputStream(BufferedInputStream(xApkStream)).use { input ->
                                generateSequence { input.nextEntry }.filter { it.name.endsWith(".apk") }
                                    .forEach { file ->
                                        val bytes = input.readBytes()
                                        InstallationUtil.addFileToSession(
                                            session,
                                            bytes.inputStream(),
                                            file.name,
                                            bytes.size.toLong()
                                        )
                                        input.closeEntry()
                                    }
                            }
                        }
                }

                InstallationUtil.finishSession(
                    context, session, sessionId, InstallXapkActivity::class.java
                )
            }
        }
    }
}
