package domilopment.apkextractor.utils.apkActions

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
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.Utils

enum class ApkActionsOptions(val preferenceValue: String) {
    SAVE("save_apk"),
    SHARE("share_apk"),
    ICON("save_icon"),
    SETTINGS("open_settings"),
    OPEN("open_app"),
    UNINSTALL("uninstall_app"),
    NONE("none");
}

fun ApkActionsOptions.isOptionSupported(app: ApplicationModel): Boolean {
    return (this != ApkActionsOptions.OPEN || app.launchIntent != null) && (this != ApkActionsOptions.UNINSTALL || (!Utils.isSystemApp(
        app
    ) || (app.appUpdateTime > app.appInstallTime)))
}

fun ApkActionsOptions.visuals(): ApkActionVisuals = when (this) {
    ApkActionsOptions.SAVE -> ApkActionVisuals(
        R.string.action_bottom_sheet_save, Icons.Default.Save
    )

    ApkActionsOptions.SHARE -> ApkActionVisuals(
        R.string.action_bottom_sheet_share, Icons.Default.Share
    )

    ApkActionsOptions.ICON -> ApkActionVisuals(
        R.string.action_bottom_sheet_save_image, Icons.Default.Image
    )

    ApkActionsOptions.SETTINGS -> ApkActionVisuals(
        R.string.action_bottom_sheet_settings, Icons.Default.Settings
    )

    ApkActionsOptions.OPEN -> ApkActionVisuals(
        R.string.action_bottom_sheet_open, Icons.Default.Android
    )

    ApkActionsOptions.UNINSTALL -> ApkActionVisuals(
        R.string.action_bottom_sheet_uninstall, Icons.Default.Delete
    )

    ApkActionsOptions.NONE -> ApkActionVisuals(
        R.string.action_none, ImageVector.Builder(
            defaultWidth = 1.dp, defaultHeight = 1.dp, viewportWidth = 1f, viewportHeight = 1f
        ).build()
    )
}

fun ApkActionsOptions.intent(app: ApplicationModel, intentParams: ApkActionIntentParams? = null): ApkActionIntent = when (this) {
    ApkActionsOptions.SAVE -> ApkActionIntent.Save(app)
    ApkActionsOptions.SHARE -> ApkActionIntent.Share(app)
    ApkActionsOptions.ICON -> ApkActionIntent.Icon(app, intentParams?.showSnackbar ?: {})
    ApkActionsOptions.SETTINGS -> ApkActionIntent.Settings(app)
    ApkActionsOptions.OPEN -> ApkActionIntent.Open(app)
    ApkActionsOptions.UNINSTALL -> ApkActionIntent.Uninstall(app)
    ApkActionsOptions.NONE -> ApkActionIntent.None
}
