package domilopment.apkextractor.utils.apkActions

import domilopment.apkextractor.R

enum class ApkActionsOptions(val preferenceValue: String, val title: Int, val icon: Int) {
    SAVE("save_apk", R.string.action_bottom_sheet_save, R.drawable.ic_baseline_save_24),
    SHARE("share_apk", R.string.action_bottom_sheet_share, R.drawable.ic_share_24dp),
    ICON("save_icon", R.string.action_bottom_sheet_save_image, R.drawable.ic_baseline_image_24),
    SETTINGS("open_settings", R.string.action_bottom_sheet_settings, R.drawable.ic_baseline_settings_24);
}