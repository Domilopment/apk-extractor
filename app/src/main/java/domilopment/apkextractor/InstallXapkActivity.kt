package domilopment.apkextractor

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentSanitizer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.InstallXapkActivityViewModel
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType

abstract class MySessionCallback : PackageInstaller.SessionCallback() {
    abstract var initialSessionId: Int
}

class InstallXapkActivity : ComponentActivity() {
    private val model by viewModels<InstallXapkActivityViewModel>()

    private lateinit var sessionCallback: MySessionCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState = model.uiState
            APKExtractorTheme(dynamicColor = true) {
                Surface {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.shouldBeShown) ProgressDialog(
                            state = uiState,
                            title = stringResource(id = R.string.progress_dialog_title_install_xapk),
                            onDismissRequest = { this@InstallXapkActivity.finish() },
                            onCancel = { this@InstallXapkActivity.finish() },
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )
                    }
                }
            }

        }
        this.setFinishOnTouchOutside(true)
        val packageInstaller = applicationContext.packageManager.packageInstaller
        sessionCallback = object : MySessionCallback() {
            private var packageName: String? = null
            override var initialSessionId: Int = -1

            override fun onCreated(sessionId: Int) {
                if (sessionId != initialSessionId) return
                packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName

                model.updateState(packageName, 0F)
            }

            override fun onBadgingChanged(sessionId: Int) {
                // Not used
            }

            override fun onActiveChanged(sessionId: Int, active: Boolean) {
                if (sessionId != initialSessionId) return

                model.setProgressDialogActive(active)
            }

            override fun onProgressChanged(sessionId: Int, progress: Float) {
                if (sessionId != initialSessionId) return

                packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName
                model.updateState(packageName, progress)
            }

            override fun onFinished(sessionId: Int, success: Boolean) {
                if (sessionId != initialSessionId) return

                packageInstaller.unregisterSessionCallback(this)
                model.setProgressDialogActive(false)
                if (success && packageName != null) EventDispatcher.emitEvent(
                    Event(EventType.INSTALLED, packageName)
                )
            }
        }
        packageInstaller.registerSessionCallback(sessionCallback)

        intent.data?.let { xApkUri ->
            contentResolver.openInputStream(xApkUri)
        }?.run {
            model.installXAPK(this, sessionCallback)
        } ?: super.finish()
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.action == MainActivity.PACKAGE_INSTALLATION_ACTION) {
            when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val activityIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT)
                        }?.let {
                            IntentSanitizer.Builder().allowAnyComponent()
                                .allowAction("android.content.pm.action.CONFIRM_INSTALL")
                                .allowPackage("com.google.android.packageinstaller").allowExtra(
                                    "android.content.pm.extra.SESSION_ID", Integer::class.java
                                ).build().sanitize(it) {
                                    this.finish()
                                }
                        }
                    startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    MaterialAlertDialogBuilder(this).setTitle(R.string.installation_result_dialog_success_title)
                        .setMessage(
                            getString(
                                R.string.installation_result_dialog_success_message, packageName
                            )
                        ).setPositiveButton(R.string.installation_result_dialog_ok) { dialog, _ ->
                            dialog.dismiss()
                        }.setOnDismissListener { this.finish() }.show()
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    val errorMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "No Error message provided"
                    MaterialAlertDialogBuilder(this).setTitle(R.string.installation_result_dialog_failed_title)
                        .setMessage(
                            getString(
                                R.string.installation_result_dialog_failed_message,
                                packageName,
                                errorMessage
                            )
                        ).setPositiveButton(R.string.installation_result_dialog_ok) { dialog, _ ->
                            dialog.dismiss()
                        }.setOnDismissListener { this.finish() }.show()
                }
            }
        } else super.onNewIntent(intent)
    }
}