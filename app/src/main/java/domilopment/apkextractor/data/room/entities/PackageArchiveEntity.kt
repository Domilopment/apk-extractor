package domilopment.apkextractor.data.room.entities

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_apks")
data class PackageArchiveEntity(
    @PrimaryKey(autoGenerate = false) val fileUri: Uri,
    @ColumnInfo(index = true) val fileName: String,
    @ColumnInfo val fileType: String,
    @ColumnInfo val fileLastModified: Long,
    @ColumnInfo val fileSize: Long,
    @ColumnInfo(index = true) val appName: String? = null,
    @ColumnInfo(index = true) val appPackageName: String? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val appIcon: ImageBitmap? = null,
    @ColumnInfo(index = true) val appVersionName: String? = null,
    @ColumnInfo(index = true) val appVersionCode: Long? = null,
    @ColumnInfo val appMinSdkVersion: Int? = null,
    @ColumnInfo val appTargetSdkVersion: Int? = null,
    @ColumnInfo val loaded: Boolean = false
)
