package domilopment.apkextractor.data.room.converter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class ImageConverter {
    @TypeConverter
    fun fromDrawable(imageBitmap: ImageBitmap?): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            Bitmap.CompressFormat.PNG
        }
        return imageBitmap?.asAndroidBitmap()?.compress(compressFormat, 100, outputStream)?.let {
            if (it) outputStream.toByteArray() else null
        }
    }

    @TypeConverter
    fun toDrawable(byteArray: ByteArray?): ImageBitmap? {
        return byteArray?.let {
            BitmapFactory.decodeByteArray(it, 0, byteArray.size).asImageBitmap()
        }
    }
}