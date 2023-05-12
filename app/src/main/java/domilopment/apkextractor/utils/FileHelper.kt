package domilopment.apkextractor.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import domilopment.apkextractor.data.ApplicationModel
import java.io.*

class FileHelper(private val context: Context) {
    companion object {
        const val MIME_TYPE = "application/vnd.android.package-archive"
        const val PREFIX = ".apk"
    }

    /**
     * Copy APK file from Source to Chosen Save Director with Name
     * @param from
     * Source of existing APK from App
     * @param to
     * Destination Folder for APK
     * @param fileName
     * Name for Saved APK File
     * @return
     * True if copy was Successfully else False
     */
    fun copy(
        from: String, to: Uri, fileName: String
    ): Uri? {
        return try {
            val extractedApk: Uri?
            // Create Input Stream from APK source file
            FileInputStream(from).use { input ->
                // Create new APK file in destination folder
                DocumentsContract.createDocument(
                    context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(
                        to, DocumentsContract.getTreeDocumentId(to)
                    ), MIME_TYPE, fileName
                ).let { outputFile ->
                    extractedApk = outputFile
                    // Create Output Stream for target APK file
                    context.contentResolver.openOutputStream(outputFile!!)
                }.use { output ->
                    // Copy from Input to Output Stream
                    input.copyTo(output!!)
                }
            }
            extractedApk
        } catch (fnf_e: FileNotFoundException) {
            fnf_e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Select a Destination Directory for APK files
     * Shows Android Directory Chooser
     */
    fun chooseDir(activityResultLauncher: ActivityResultLauncher<Intent>) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            SettingsManager(context).saveDir()?.let {
                val pickerInitialUri = DocumentsContract.buildDocumentUriUsingTree(
                    it, DocumentsContract.getTreeDocumentId(it)
                )
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
        }.also {
            activityResultLauncher.launch(
                Intent.createChooser(it, "Choose directory")
            )
        }
    }

    /**
     * Creates a Uri for Provider
     * @param app Application for sharing
     * @return Shareable Uri of Application APK
     */
    fun shareURI(app: ApplicationModel): Uri {
        return FileProvider.getUriForFile(
            context,
            context.applicationInfo.packageName + ".provider",
            File(app.appSourceDirectory).copyTo(
                File(
                    context.cacheDir, SettingsManager(context).appName(app)
                ), true
            )
        )
    }
}