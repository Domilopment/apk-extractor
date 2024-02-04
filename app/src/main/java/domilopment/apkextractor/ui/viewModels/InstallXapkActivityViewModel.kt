package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.InstallXapkActivity
import domilopment.apkextractor.MySessionCallback
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.utils.InstallationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class InstallXapkActivityViewModel(application: Application) : AndroidViewModel(application) {
    val context get() = getApplication<Application>().applicationContext

    var uiState by mutableStateOf(
        ProgressDialogUiState(
            title = context.getString(R.string.progress_dialog_title_install_xapk),
            process = null,
            progress = 0F,
            tasks = 100,
            shouldBeShown = true
        )
    )
        private set

    fun updateState(packageName: String?, progress: Float) {
        uiState = uiState.copy(process = packageName, progress = progress * 100)
    }

    fun setProgressDialogActive(active: Boolean) {
        uiState = uiState.copy(shouldBeShown = active)
    }

    fun installXAPK(xApkInputStream: InputStream, sessionCallback: MySessionCallback) {
        viewModelScope.launch(Dispatchers.IO) {

            val (session, sessionId) = InstallationUtil.createSession(context)
            sessionCallback.initialSessionId = sessionId
            ZipInputStream(BufferedInputStream(xApkInputStream)).use { input ->
                generateSequence { input.nextEntry }.filter { it.name.endsWith(".apk") }
                    .forEach { file ->
                        val bytes = input.readBytes()
                        InstallationUtil.addFileToSession(
                            session, bytes.inputStream(), file.name, bytes.size.toLong()
                        )
                    }
            }
            InstallationUtil.finishSession(
                context, session, sessionId, InstallXapkActivity::class.java
            )
        }
    }
}
