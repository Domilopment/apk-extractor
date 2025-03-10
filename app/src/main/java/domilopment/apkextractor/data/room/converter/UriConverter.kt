package domilopment.apkextractor.data.room.converter

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

class UriConverter {
    @TypeConverter
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(uriString: String): Uri {
        return uriString.toUri()
    }
}