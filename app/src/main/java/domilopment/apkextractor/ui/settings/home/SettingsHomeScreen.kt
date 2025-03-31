package domilopment.apkextractor.ui.settings.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.DocumentsContract
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.material.color.DynamicColors
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.components.HyperlinkText
import domilopment.apkextractor.ui.components.Link
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale
import androidx.core.net.toUri
import domilopment.apkextractor.ui.navigation.Route

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsHomeScreen(
    model: SettingsScreenViewModel,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    onSaveFileSettings: () -> Unit,
    onAutoBackupSettings: () -> Unit,
    onSwipeActionSettings: () -> Unit,
    onDataCollectionSettings: () -> Unit,
    onAboutSettings: () -> Unit,
    onDonationSettings: () -> Unit,
    chooseSaveDir: ManagedActivityResultLauncher<Uri?, Uri?>,
    context: Context = LocalContext.current,
    appUpdateManager: AppUpdateManager,
    inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val uiState by model.uiState.collectAsStateWithLifecycle()

    var appUpdateInfo: AppUpdateInfo? by remember {
        mutableStateOf(null)
    }

    val isUpdateAvailable by remember {
        derivedStateOf {
            appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo?.isUpdateTypeAllowed(
                AppUpdateType.FLEXIBLE
            ) == true
        }
    }

    var language by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag() ?: "default")
    }

    var cacheSize by remember {
        mutableLongStateOf(context.cacheDir.walkTopDown().map { it.length() }.sum())
    }

    LaunchedEffect(key1 = appUpdateManager, block = {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            appUpdateInfo = info
        }
    })

    LaunchedEffect(key1 = Unit) {
        Route.Screen.SettingsHome.buttons.onEach { button ->
            when (button) {
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsHomeContent(
        appUpdateInfo = appUpdateInfo,
        isUpdateAvailable = isUpdateAvailable,
        onUpdateAvailable = {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo!!,
                inAppUpdateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
        },
        onChooseSaveDir = {
            val pickerInitialUri = uiState.saveDir?.let {
                DocumentsContract.buildDocumentUriUsingTree(
                    it, DocumentsContract.getTreeDocumentId(it)
                )
            }
            chooseSaveDir.launch(pickerInitialUri)
        },
        onSaveFileSettings = onSaveFileSettings,
        onAutoBackupSettings = onAutoBackupSettings,
        nightMode = uiState.nightMode,
        onNightMode = {
            model.setNightMode(it)
            SettingsManager.changeUIMode(it)
        },
        dynamicColors = uiState.useMaterialYou,
        isDynamicColors = remember { DynamicColors.isDynamicColorAvailable() },
        onDynamicColors = model::setUseMaterialYou,
        language = language,
        languageLocaleDisplayName = when (language) {
            "default" -> context.getString(R.string.locale_list_default)
            Locale.ENGLISH.toLanguageTag() -> context.getString(R.string.locale_list_en)
            Locale.GERMANY.toLanguageTag() -> context.getString(R.string.locale_list_de_de)
            else -> context.getString(
                R.string.locale_list_not_supported, Locale.forLanguageTag(language).displayName
            )
        },
        onLanguage = {
            language = it
            SettingsManager.setLocale(it)
        },
        onSwipeActionSettings = onSwipeActionSettings,
        checkUpdateOnStart = uiState.checkUpdateOnStart,
        onCheckUpdateOnStart = model::setCheckUpdateOnStart,
        cacheSize = Formatter.formatFileSize(context, cacheSize),
        onClearCache = {
            if (context.cacheDir?.deleteRecursively() == true) {
                cacheSize = context.cacheDir.walkTopDown().map { it.length() }.sum()
                Toast.makeText(
                    context, context.getString(R.string.clear_cache_success), Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                context, context.getString(R.string.clear_cache_failed), Toast.LENGTH_SHORT
            ).show()
        },
        onDataCollectionSettings = onDataCollectionSettings,
        dataCollectionDeleteDialogContent = {
            HyperlinkText(
                text = stringResource(id = R.string.data_collection_delete_summary),
                links = arrayOf(
                    Link(
                        text = "statement on deletion and retention",
                        href = "https://policies.google.com/technologies/retention"
                    ), Link(
                        text = "der Stellungnahme von Google zur Löschung und Aufbewahrung ausführlich beschrieben",
                        href = "https://policies.google.com/technologies/retention?hl=de"
                    )
                )
            )
        },
        onDeleteFirebaseInstallationsId = model::onDeleteFirebaseInstallationsId,
        onGitHub = {
            CustomTabsIntent.Builder().build().launchUrl(context, Constants.GITHUB_URL.toUri())
        },
        onGooglePlay = {
            try {
                Intent(Intent.ACTION_VIEW).apply {
                    try {
                        val packageInfo = Utils.getPackageInfo(
                            context.packageManager, "com.android.vending"
                        )
                        setPackage(packageInfo.packageName)
                    } catch (_: PackageManager.NameNotFoundException) {
                        // If Play Store is not installed
                    }
                    data =
                        "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri()
                }.also {
                    context.startActivity(it)
                }
            } catch (_: ActivityNotFoundException) { // If Play Store is Installed, but deactivated
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri()
                    )
                )
            }
        },
        onAboutSettings = onAboutSettings,
        onDonationSettings = onDonationSettings
    )
}
