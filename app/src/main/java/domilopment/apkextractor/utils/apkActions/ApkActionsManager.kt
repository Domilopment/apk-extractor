package domilopment.apkextractor.utils.apkActions

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import java.io.File

class ApkActionsManager(private val context: Context, private val app: ApplicationModel) {
    /**
     * Saves selected apk to public storage dir (previously selected by user)
     * @param view reference for Snackbar view
     * @param anchorView Anchor View for Snackbar
     */
    fun actionSave(view: View, anchorView: View = view) {
        val settingsManager = SettingsManager(context)
        FileHelper(context).copy(
            app.appSourceDirectory, settingsManager.saveDir()!!, settingsManager.appName(app)
        )?.let {
            Snackbar.make(
                view,
                context.getString(R.string.snackbar_successful_extracted, app.appName),
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).show()
        } ?: run {
            Snackbar.make(
                view,
                context.getString(R.string.snackbar_extraction_failed, app.appName),
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).setTextColor(Color.RED).show()
        }
    }

    /**
     * Creates an share Intent for apk source file of selected app
     * @return Intent with mime type and stream info of apk file
     */
    fun actionShare(shareApp: ActivityResultLauncher<Intent>) {
        val file = FileHelper(context).shareURI(app)
        Intent(Intent.ACTION_SEND).apply {
            type = FileHelper.MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, file)
        }.let {
            Intent.createChooser(it, context.getString(R.string.share_intent_title))
        }.also {
            shareApp.launch(it)
        }
    }

    /**
     * Creates an Intent to open settings page of app
     * @return Intent for ACTION_APPLICATION_DETAILS_SETTINGS for selected app
     */
    fun actionShowSettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", app.appPackageName, null)
            )
        )
    }

    /**
     * Returns launch Intent for app
     * @return launchIntent for app ;P
     */
    fun actionOpenApp() {
        context.startActivity(app.launchIntent)
    }

    /**
     * Creates an Intent to delete selected app
     * @return ACTION_DELETE Intent with package uri information
     */
    fun actionUninstall(uninstallApp: ActivityResultLauncher<Intent>) {
        uninstallApp.launch(
            Intent(
                Intent.ACTION_DELETE, Uri.fromParts("package", app.appPackageName, null)
            )
        )
    }

    /**
     * Saves app icon as Bitmap to Gallery
     * @param view reference for Snackbar view
     * @param anchorView Anchor View for Snackbar
     */
    fun actionSaveImage(view: View, anchorView: View = view) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, app.appName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + context.getString(R.string.app_name)
            )
        }
        // Find all image files on the primary external storage device.
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        resolver.insert(imageCollection, contentValues)?.let {
            resolver.openOutputStream(it)
        }.use {
            val result = app.appIcon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            if (result) Snackbar.make(
                view,
                context.getString(R.string.snackbar_successful_save_image),
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).show()
        }
    }

    /**
     * Creates ACTION_VIEW intent for app, opening its store page
     * Supports Google Play Store, Samsung Galaxy Store and Amazon Appstore for now
     * other installation Sources just call market uri
     */
    fun actionOpenShop(view: View, anchorView: View = view) {
        try {
            val shopIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    when (app.installationSource) {
                        "com.android.vending" -> {
                            setPackage(app.installationSource)
                            "https://play.google.com/store/apps/details?id="
                        }
                        "com.sec.android.app.samsungapps" -> "samsungapps://ProductDetail/"
                        "com.amazon.venezia" -> "amzn://apps/android?p="
                        else -> "market://details?id="
                    } + app.appPackageName
                )
            }
            context.startActivity(shopIntent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                view,
                context.getString(R.string.snackbar_no_activity_for_market_intent),
                Snackbar.LENGTH_LONG
            ).setAnchorView(anchorView).show()
        }
    }
}