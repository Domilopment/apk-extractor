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
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.R
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.utils.ExtractionResult
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import java.io.File

class ApkActionsManager(private val context: Context, private val app: ApplicationModel) {
    /**
     * Saves selected apk to public storage dir (previously selected by user)
     * @param view reference for Snackbar view
     * @param anchorView Anchor View for Snackbar
     */
    fun actionSave(
        saveDir: Uri,
        appNameBuilder: (ApplicationModel) -> String,
        showSnackbar: (MySnackbarVisuals) -> Unit,
        showErrorDialog: (String, String?) -> Unit
    ) {
        when (val result =
            FileUtil(context).copy(app.appSourceDirectory, saveDir, appNameBuilder(app))) {
            is ExtractionResult.Success -> {
                EventDispatcher.emitEvent(Event(EventType.SAVED, result.uri))
                showSnackbar(
                    MySnackbarVisuals(
                        duration = SnackbarDuration.Short, message = context.getString(
                            R.string.snackbar_successful_extracted, app.appName
                        )
                    )
                )
            }

            is ExtractionResult.Failure -> showErrorDialog(app.appName, result.errorMessage)
        }
    }

    /**
     * Creates an share Intent for apk source file of selected app
     * @param shareApp ActivityResultLauncher to launch Intent
     */
    fun actionShare(
        shareApp: ActivityResultLauncher<Intent>, appName: (ApplicationModel) -> String
    ) {
        val file = FileUtil(context).shareURI(app, appName(app))
        Intent(Intent.ACTION_SEND).apply {
            setDataAndType(file, FileUtil.MIME_TYPE)
            putExtra(Intent.EXTRA_STREAM, file)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.let {
            Intent.createChooser(it, context.getString(R.string.share_intent_title))
        }.also {
            shareApp.launch(it)
        }
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
        context.startActivity(app.launchIntent)
    }

    /**
     * Creates an Intent to delete selected app
     * @param uninstallApp ActivityResultLauncher to start Intent from
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
     * @param view reference for Snackbar view
     * @param anchorView Anchor View for Snackbar
     */
    fun actionOpenShop(showSnackbar: (MySnackbarVisuals) -> Unit) {
        app.installationSource?.also {
            try {
                val shopIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(Utils.listOfKnownStores.getValue(it) + app.appPackageName)
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