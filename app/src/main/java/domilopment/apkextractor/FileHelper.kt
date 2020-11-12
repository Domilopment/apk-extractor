package domilopment.apkextractor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import java.io.*

class FileHelper(private val context: Context) {
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
    fun copy(
        from: String,
        to: Uri,
        fileName: String
    ): Boolean {
        return try {
            val pickedDir = DocumentsContract.createDocument(
                context.contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(
                    to, DocumentsContract.getTreeDocumentId(to)
                ), MIME_TYPE, fileName
            )

            FileInputStream(from).use { input ->
                context.contentResolver.openOutputStream(pickedDir!!).use { output ->
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
    fun chooseDir(activity: Activity) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addCategory(Intent.CATEGORY_DEFAULT)
        }.also {
            activity.startActivityForResult(
                Intent.createChooser(it, "Choose directory"),
                CHOOSE_SAVE_DIR_RESULT
            )
        }
    }
}