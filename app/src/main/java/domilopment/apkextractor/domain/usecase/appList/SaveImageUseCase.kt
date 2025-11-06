package domilopment.apkextractor.domain.usecase.appList

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.material3.SnackbarDuration
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import java.io.File

interface SaveImageUseCase {
    operator fun invoke(appDetailModel: ApplicationModel, showSnackbar: (MySnackbarVisuals) -> Unit)
}

class SaveImageUseCaseImpl(private val context: Context) : SaveImageUseCase {
    /**
     * Saves app icon as Bitmap to Gallery
     * @param showSnackbar function triggering a snackbar host to show a message
     */
    private fun saveImage(appName: String, appIcon: Drawable, showSnackbar: (MySnackbarVisuals) -> Unit) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, appName)
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
            val result = appIcon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            if (result) showSnackbar(
                MySnackbarVisuals(
                    duration = SnackbarDuration.Short,
                    message = context.getString(R.string.snackbar_successful_save_image)
                )
            )
        }
    }

    override fun invoke(
        appDetailModel: ApplicationModel,
        showSnackbar: (MySnackbarVisuals) -> Unit
    ) {
        saveImage(appDetailModel.appName, appDetailModel.appIcon, showSnackbar)
    }
}