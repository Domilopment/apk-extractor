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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.core.content.IntentSanitizer
import androidx.core.util.Consumer
import domilopment.apkextractor.data.ApkInstallationResult
import domilopment.apkextractor.data.ApkInstallationResultType
import domilopment.apkextractor.ui.dialogs.InstallationResultDialog
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.InstallXapkActivityViewModel

abstract class MySessionCallback : PackageInstaller.SessionCallback() {
    abstract var initialSessionId: Int
}

class InstallXapkActivity : ComponentActivity() {
    private val model by viewModels<InstallXapkActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState = model.uiState
            var installationResult: ApkInstallationResult? by remember {
                mutableStateOf(null)
            }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { intent ->
                    onNewIntent(intent) { resultType ->
                        installationResult = ApkInstallationResult(resultType)
                    }
                }
                addOnNewIntentListener(listener)
                onDispose {
                    removeOnNewIntentListener(listener)
                }
            }

            APKExtractorTheme(dynamicColor = true) {
                Surface {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.shouldBeShown) ProgressDialog(state = uiState,
                            onDismissRequest = {
                                model.cancel()
                                this@InstallXapkActivity.finish()
                            },
                            onCancel = {
                                model.cancel()
                                this@InstallXapkActivity.finish()
                            },
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )

                        installationResult?.let {
                            InstallationResultDialog(
                                onDismissRequest = {
                                    installationResult = null
                                    this@InstallXapkActivity.finish()
                                },
                                result = it.result,
                            )
                        }
                    }
                }
            }

        }
        this.setFinishOnTouchOutside(true)

        intent.data?.let { xApkUri ->
            model.installXAPK(xApkUri)
        } ?: super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun onNewIntent(intent: Intent, result: (ApkInstallationResultType) -> Unit) {
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
                    result(ApkInstallationResultType.Success(packageName))
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    val errorMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "No Error message provided"
                    result(ApkInstallationResultType.Failure(packageName, errorMessage))
                }
            }
        }
    }
}