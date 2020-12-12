package domilopment.apkextractor.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import domilopment.apkextractor.data.Application
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
            // Create Input Stream from APK source file
            FileInputStream(from).use { input ->
                // Create new APK file in destination folder
                DocumentsContract.createDocument(
                    context.contentResolver,
                    DocumentsContract.buildDocumentUriUsingTree(
                        to, DocumentsContract.getTreeDocumentId(to)
                    ),
                    MIME_TYPE,
                    fileName
                ).let { outputFile ->
                    // Create Output Stream for target APK file
                    context.contentResolver.openOutputStream(outputFile!!)
                }.use { output ->
                    // Copy from Input to Output Stream
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

    /**
     * Creates a Uri for Provider
     * @param app Application for sharing
     * @return Shareable Uri of Application APK
     */
    fun shareURI(app: Application): Uri {
        return FileProvider.getUriForFile(
            context,
            context.applicationInfo.packageName + ".provider",
            File(app.appSourceDirectory).copyTo(
                File(
                    context.cacheDir,
                    SettingsManager(context).appName(app)
                ),
                true
            )
        )
    }
}