package domilopment.apkextractor.utils.apkActions

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import java.io.File
import androidx.core.net.toUri

class ApkActionsManager(private val context: Context, private val app: ApplicationModel) {
    /**
     * Saves selected apk to public storage dir (previously selected by user)
     * @param saveFunction the function with implementation to save single apk
     */
    fun actionSave(
        saveFunction: (ApplicationModel) -> Unit,
    ) {
        saveFunction(app)
    }

    /**
     * Creates an share Intent for apk source file of selected app
     * @param shareFunction the Function with implementation to share apk
     */
    fun actionShare(
        shareFunction: (ApplicationModel) -> Unit,
    ) {
        shareFunction(app)
    }

    /**
     * Creates an Intent to open settings page of app and starts it
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
     * Launch App via Intent
     */
    fun actionOpenApp() {
        try {
            context.startActivity(app.launchIntent)
        } catch (_: SecurityException) {
            // Permission denial
        }
    }

    /**
     * Creates an Intent to delete selected app
     */
    fun actionUninstall(receiverCls: Class<*>) {
        Intent(context, receiverCls).apply {
            data = Uri.fromParts("package", app.appPackageName, null)
            setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let { intent ->
            context.startActivity(intent)
        }
    }

    /**
     * Saves app icon as Bitmap to Gallery
     * @param showSnackbar function triggering a snackbar host to show a message
     */
    fun actionSaveImage(showSnackbar: (MySnackbarVisuals) -> Unit) {
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
        }?.use {
            val result = app.appIcon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            if (result) showSnackbar(
                MySnackbarVisuals(
                    duration = SnackbarDuration.Short,
                    message = context.getString(R.string.snackbar_successful_save_image)
                )
            )
        }
    }

    /**
     * Creates ACTION_VIEW intent for app, opening its store page and launches it
     * if it could not be launched, creates Snackbar with error message
     * Supports Google Play Store, Samsung Galaxy Store and Amazon Appstore for now
     * other installation Sources just call market uri
     * @param showSnackbar function triggering a snackbar host to show a message
     */
    fun actionOpenShop(showSnackbar: (MySnackbarVisuals) -> Unit) {
        app.installationSource?.also {
            try {
                val shopIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "${Utils.listOfKnownStores.getValue(it)}${app.appPackageName}".toUri()
                    if (it in Utils.listOfKnownStores) setPackage(it)
                }
                context.startActivity(shopIntent)
            } catch (e: ActivityNotFoundException) {
                showSnackbar(
                    MySnackbarVisuals(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.snackbar_no_activity_for_market_intent),
                        messageColor = Color.Red
                    )
                )
            }
        }
    }
}