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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils

enum class ApkActionsOptions(val preferenceValue: String, val title: Int, val icon: ImageVector) {
    SAVE("save_apk", R.string.action_bottom_sheet_save, Icons.Default.Save) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            val dialogHeight = params.dialogHeight ?: 0.dp
            params.callbackFun?.let { ApkActionsManager(context, app).actionSave(dialogHeight, it) }
        }

    },
    SHARE("share_apk", R.string.action_bottom_sheet_share, Icons.Default.Share) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.shareResult?.also { ApkActionsManager(context, app).actionShare(it) }
        }
    },

    ICON("save_icon", R.string.action_bottom_sheet_save_image, Icons.Default.Image) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            val dialogHeight = params.dialogHeight ?: 0.dp
            params.callbackFun?.let {
                ApkActionsManager(context, app).actionSaveImage(dialogHeight, it)
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
        val dialogHeight: Dp?,
        val callbackFun: ((MySnackbarVisuals) -> Unit)? = null,
        val shareResult: ActivityResultLauncher<Intent>?,
        val deleteResult: ActivityResultLauncher<Intent>?
    ) {
        data class Builder(
            private var dialogHeight: Dp? = null,
            private var callbackFun: ((MySnackbarVisuals) -> Unit)? = null,
            private var shareResult: ActivityResultLauncher<Intent>? = null,
            private var deleteResult: ActivityResultLauncher<Intent>? = null
        ) {
            fun setDialogHeight(dialogHeight: Dp) =
                apply { this.dialogHeight = dialogHeight }

            fun setCallbackFun(showSnackbar: (MySnackbarVisuals) -> Unit) =
                apply { this.callbackFun = showSnackbar }

            fun setShareResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.shareResult = activityResultLauncher }

            fun setDeleteResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.deleteResult = activityResultLauncher }

            fun build() = ApkActionOptionParams(dialogHeight, callbackFun, shareResult, deleteResult)
        }
    }
}