package domilopment.apkextractor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.*

class FileHelper(private val activity: Activity) {
    companion object{
        const val MIME_TYPE = "application/vnd.android.package-archive"
        const val CHOOSE_SAVE_DIR_RESULT = 9999
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
    fun copy(
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
     * Shows Android Directory Chosser
     */
    fun chooseDir(){
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        activity.startActivityForResult(Intent.createChooser(i, "Choose directory"), CHOOSE_SAVE_DIR_RESULT)
    }
}
