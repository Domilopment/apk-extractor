package domilopment.apkextractor.utils.apkActions

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel

enum class ApkActionsOptions(val preferenceValue: String, val title: Int, val icon: Int) {
    SAVE("save_apk", R.string.action_bottom_sheet_save, R.drawable.ic_baseline_save_24) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.views?.let { ApkActionsManager(context, app).actionSave(it.first, it.second) }
        }

    },
    SHARE("share_apk", R.string.action_bottom_sheet_share, R.drawable.ic_share_24dp) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.shareResult?.also { ApkActionsManager(context, app).actionShare(it) }
        }
    },

    ICON("save_icon", R.string.action_bottom_sheet_save_image, R.drawable.ic_baseline_image_24) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            params.views?.let {
                ApkActionsManager(context, app).actionSaveImage(
                    it.first, it.second
                )
            }
        }
    },
    SETTINGS(
        "open_settings", R.string.action_bottom_sheet_settings, R.drawable.ic_baseline_settings_24
    ) {
        override fun getAction(
            context: Context, app: ApplicationModel, params: ApkActionOptionParams
        ) {
            ApkActionsManager(context, app).actionShowSettings()
        }
    };

    abstract fun getAction(
        context: Context, app: ApplicationModel, params: ApkActionOptionParams
    )

    class ApkActionOptionParams private constructor(
        val views: Pair<View, View>?,
        val shareResult: ActivityResultLauncher<Intent>?,
        val deleteResult: ActivityResultLauncher<Intent>?
    ) {
        data class Builder(
            private var views: Pair<View, View>? = null,
            private var shareResult: ActivityResultLauncher<Intent>? = null,
            private var deleteResult: ActivityResultLauncher<Intent>? = null
        ) {
            fun setViews(view: View, anchorView: View = view) =
                apply { this.views = Pair(view, anchorView) }

            fun setShareResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.shareResult = activityResultLauncher }

            fun setDeleteResult(activityResultLauncher: ActivityResultLauncher<Intent>) =
                apply { this.deleteResult = activityResultLauncher }

            fun build() = ApkActionOptionParams(views, shareResult, deleteResult)
        }
    }
}