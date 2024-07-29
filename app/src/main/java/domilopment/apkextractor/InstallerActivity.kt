package domilopment.apkextractor

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
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
import dagger.hilt.android.AndroidEntryPoint
import domilopment.apkextractor.data.ApkInstallationResult
import domilopment.apkextractor.data.InstallationResultType
import domilopment.apkextractor.ui.dialogs.InstallationResultDialog
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.InstallerActivityViewModel
import timber.log.Timber

@AndroidEntryPoint
class InstallerActivity : ComponentActivity() {
    private val model by viewModels<InstallerActivityViewModel>()

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
                                this@InstallerActivity.finish()
                            },
                            onCancel = {
                                model.cancel()
                                this@InstallerActivity.finish()
                            },
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )

                        installationResult?.let {
                            InstallationResultDialog(
                                onDismissRequest = {
                                    installationResult = null
                                    this@InstallerActivity.finish()
                                },
                                result = it.result,
                            )
                        }
                    }
                }
            }
        }
        this.setFinishOnTouchOutside(true)

        intent.data?.let { uri ->
            when (uri.scheme) {
                "content", "file" -> model.installXAPK(uri)
                "package" -> model.uninstallApp(uri)
                else -> super.finish()
            }
        } ?: super.finish()
    }

    private fun onNewIntent(intent: Intent, result: (InstallationResultType) -> Unit) {
        if (intent.action == MainActivity.PACKAGE_INSTALLATION_ACTION || intent.action == MainActivity.PACKAGE_UNINSTALLATION_ACTION) {
            when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val activityIntent = intent.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            it.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            it.getParcelableExtra(Intent.EXTRA_INTENT)
                        }
                    }?.let { userActionIntent ->
                        val sanitizer = IntentSanitizer.Builder().allowAnyComponent().apply {
                            if (intent.action == MainActivity.PACKAGE_UNINSTALLATION_ACTION) {
                                allowAction("android.intent.action.UNINSTALL_PACKAGE").allowExtra(
                                    "android.content.pm.extra.CALLBACK", Parcelable::class.java
                                ).allowData { it.scheme == "package" }
                            } else if (intent.action == MainActivity.PACKAGE_INSTALLATION_ACTION) {
                                allowAction("android.content.pm.action.CONFIRM_INSTALL").allowAction(
                                    "android.content.pm.action.CONFIRM_PERMISSIONS"
                                ).allowPackage(
                                    "com.google.android.packageinstaller"
                                ).allowExtra(
                                    "android.content.pm.extra.SESSION_ID", Integer::class.java
                                )
                            }
                        }

                        return@let try {
                            try {
                                sanitizer.allowExtra(
                                    "android.content.pm.extra.CALLBACK", IBinder::class.java
                                ).build().sanitizeByThrowing(userActionIntent)
                            } catch (e: IllegalArgumentException) {
                                // On android versions lower than API 34 uninstall action returns an BinderProxy extra.
                                // Intent Sanitizer can't handle Binder extras yet, so we have to use a workaround.
                                val testIntent = (userActionIntent.clone() as Intent).apply {
                                    removeExtra("android.content.pm.extra.CALLBACK")
                                }
                                sanitizer.build().sanitizeByThrowing(testIntent)
                                userActionIntent
                            }
                        } catch (e: SecurityException) {
                            Timber.tag("Pending User Action Security Exception").e(e)
                            result(InstallationResultType.Failure.Security(e.message))
                            null
                        }
                    }
                    activityIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

                    if (intent.action == MainActivity.PACKAGE_UNINSTALLATION_ACTION) {
                        packageName?.let { model.removeApp(packageName) }
                        result(InstallationResultType.Success.Uninstalled(packageName))
                    } else if (intent.action == MainActivity.PACKAGE_INSTALLATION_ACTION) {
                        packageName?.let { model.addApp(packageName) }
                        result(InstallationResultType.Success.Installed(packageName))
                    }
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    val errorMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "No Error message provided"

                    Timber.tag("Package Installer: ${intent.action}").e(Exception(errorMessage))
                    when (intent.action) {
                        MainActivity.PACKAGE_UNINSTALLATION_ACTION -> result(
                            InstallationResultType.Failure.Uninstall(
                                packageName, errorMessage
                            )
                        )

                        MainActivity.PACKAGE_INSTALLATION_ACTION -> result(
                            InstallationResultType.Failure.Install(
                                packageName, errorMessage
                            )
                        )
                    }
                }
            }
        }
    }
}