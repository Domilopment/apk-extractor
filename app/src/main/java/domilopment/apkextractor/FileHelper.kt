package domilopment.apkextractor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.*

class FileHelper(private val activity: Activity) {
    companion object {
        const val MIME_TYPE = "application/vnd.android.package-archive"
        const val CHOOSE_SAVE_DIR_RESULT = 9999
        const val PREFIX = ".apk"
    }

    /**
     * Copy APK file from Source to Choosen Save Director with Name
     * @param from
     * Source of existing APK from App
     * @param to
     * Destination Folder for APK
     * @param fileName
     * Name for Saved APK File
     * @return
     * True if copy was Succsessfull else False
     */
    fun copy (
        from: String,
        to: String,
        fileName: String
    ): Boolean {
        return try {
            val pickedDir = DocumentFile.fromTreeUri(activity, Uri.parse(to + fileName))
                ?.createFile(MIME_TYPE, fileName)
            FileInputStream(from).use { input ->
                activity.contentResolver.openOutputStream(pickedDir!!.uri).use { output ->
                    input.copyTo(output!!)
                }
            }
            true
        } catch (fnf_e: FileNotFoundException) {
            fnf_e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Select a Destination Directory for APK files
     * Shows Android Directory Chooser
     */
    fun chooseDir() {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addCategory(Intent.CATEGORY_DEFAULT)
        }.also {
            activity.startActivityForResult(Intent.createChooser(it, "Choose directory"), CHOOSE_SAVE_DIR_RESULT)
        }
    }
}