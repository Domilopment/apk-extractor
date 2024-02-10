package domilopment.apkextractor.utils.apkActions

import android.content.Context
import android.content.Intent
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
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils

enum class ApkActionsOptions(val preferenceValue: String, val title: Int, val icon: ImageVector) {
    SAVE("save_apk", R.string.action_bottom_sheet_save, Icons.Default.Save) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            if (params.saveFunction == null) return
            ApkActionsManager(context, app).actionSave(params.saveFunction)
        }
    },
    SHARE("share_apk", R.string.action_bottom_sheet_share, Icons.Default.Share) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            if (params.shareFunction == null) return
            ApkActionsManager(context, app).actionShare(params.shareFunction)
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
        val saveFunction: ((List<ApplicationModel>) -> Unit)?,
        val callbackFun: ((MySnackbarVisuals) -> Unit)?,
        val errorCallback: ((String?, String?) -> Unit)?,
        val shareResult: ActivityResultLauncher<Intent>?,
        val shareFunction: ((List<ApplicationModel>) -> Unit)?,
        val deleteResult: ActivityResultLauncher<Intent>?
    ) {
        data class Builder(
            private var saveFunction: ((List<ApplicationModel>) -> Unit)? = null,
            private var callbackFun: ((MySnackbarVisuals) -> Unit)? = null,
            private var errorCallback: ((String?, String?) -> Unit)? = null,
            private var shareResult: ActivityResultLauncher<Intent>? = null,
            private var shareFunction: ((List<ApplicationModel>) -> Unit)? = null,
            private var deleteResult: ActivityResultLauncher<Intent>? = null
        ) {
            fun saveFunction(saveFunction: (List<ApplicationModel>) -> Unit) =
                apply { this.saveFunction = saveFunction }

            fun setCallbackFun(showSnackbar: (MySnackbarVisuals) -> Unit) =
                apply { this.callbackFun = showSnackbar }

            fun setErrorCallBack(errorDialogCallback: (String?, String?) -> Unit) =
                apply { this.errorCallback = errorDialogCallback }

            fun setShareResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.shareResult = activityResultLauncher }

            fun setShareFunction(shareFunction: (List<ApplicationModel>) -> Unit) =
                apply { this.shareFunction = shareFunction }

            fun setDeleteResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.deleteResult = activityResultLauncher }

            fun build() = ApkActionOptionParams(
                saveFunction, callbackFun, errorCallback, shareResult, shareFunction, deleteResult
            )
        }
    }
}