package domilopment.apkextractor.utils.apkActions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.utils.ExtractionResult
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils

enum class ApkActionsOptions(val preferenceValue: String, val title: Int, val icon: ImageVector) {
    SAVE("save_apk", R.string.action_bottom_sheet_save, Icons.Default.Save) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            if (params.callbackFun == null || params.errorCallback == null || params.saveFunction == null) return
            ApkActionsManager(context, app).actionSave(
                params.saveFunction, params.callbackFun, params.errorCallback
            )
        }
    },
    SHARE("share_apk", R.string.action_bottom_sheet_share, Icons.Default.Share) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            if (params.appNameBuilder == null || params.shareResult == null) return
            ApkActionsManager(context, app).actionShare(params.shareResult, params.appNameBuilder)
        }
    },
    ICON("save_icon", R.string.action_bottom_sheet_save_image, Icons.Default.Image) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.callbackFun?.let {
                ApkActionsManager(context, app).actionSaveImage(it)
            }
        }
    },
    SETTINGS(
        "open_settings", R.string.action_bottom_sheet_settings, Icons.Default.Settings
    ) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            ApkActionsManager(context, app).actionShowSettings()
        }
    },
    OPEN(
        "open_app", R.string.action_bottom_sheet_open, Icons.Default.Android
    ) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            ApkActionsManager(context, app).actionOpenApp()
        }
    },
    UNINSTALL(
        "uninstall_app", R.string.action_bottom_sheet_uninstall, Icons.Default.Delete
    ) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.deleteResult?.let { ApkActionsManager(context, app).actionUninstall(it) }
        }
    },
    NONE(
        "none", R.string.action_none, ImageVector.Builder(
            defaultWidth = 1.dp, defaultHeight = 1.dp, viewportWidth = 1f, viewportHeight = 1f
        ).build()
    ) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            // None
        }
    };

    abstract fun getAction(
        context: Context, app: ApplicationModel, params: ApkActionOptionParams
    )

    companion object {
        fun isOptionSupported(app: ApplicationModel, action: ApkActionsOptions): Boolean {
            return (action != OPEN || app.launchIntent != null) && (action != UNINSTALL || (!Utils.isSystemApp(
                app
            ) || (app.appUpdateTime > app.appInstallTime)))
        }
    }

    class ApkActionOptionParams private constructor(
        val appNameBuilder: ((ApplicationModel) -> String)?,
        val saveFunction: ((ApplicationModel, (String, ExtractionResult) -> Unit, Boolean) -> Unit)?,
        val callbackFun: ((MySnackbarVisuals) -> Unit)?,
        val errorCallback: ((String?, String?) -> Unit)?,
        val shareResult: ActivityResultLauncher<Intent>?,
        val deleteResult: ActivityResultLauncher<Intent>?
    ) {
        data class Builder(
            private var appNameBuilder: ((ApplicationModel) -> String)? = null,
            private var saveFunction: ((ApplicationModel, (String, ExtractionResult) -> Unit, Boolean) -> Unit)? = null,
            private var callbackFun: ((MySnackbarVisuals) -> Unit)? = null,
            private var errorCallback: ((String?, String?) -> Unit)? = null,
            private var shareResult: ActivityResultLauncher<Intent>? = null,
            private var deleteResult: ActivityResultLauncher<Intent>? = null
        ) {
            fun setAppNameBuilder(appNameBuilder: (ApplicationModel) -> String) =
                apply { this.appNameBuilder = appNameBuilder }

            fun saveFunction(saveFunction: (ApplicationModel, (String, ExtractionResult) -> Unit, Boolean) -> Unit) =
                apply { this.saveFunction = saveFunction }

            fun setCallbackFun(showSnackbar: (MySnackbarVisuals) -> Unit) =
                apply { this.callbackFun = showSnackbar }

            fun setErrorCallBack(errorDialogCallback: (String?, String?) -> Unit) =
                apply { this.errorCallback = errorDialogCallback }

            fun setShareResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.shareResult = activityResultLauncher }

            fun setDeleteResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.deleteResult = activityResultLauncher }

            fun build() = ApkActionOptionParams(
                appNameBuilder, saveFunction, callbackFun, errorCallback, shareResult, deleteResult
            )
        }
    }
}