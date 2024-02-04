package domilopment.apkextractor.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.appList.ApplicationModel
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileUtil(private val context: Context) {

    data class DocumentFile(
        val uri: Uri,
        val documentId: String?,
        val displayName: String?,
        val lastModified: Long?,
        val size: Long?,
        val mimeType: String?
    )

    companion object {
        const val MIME_TYPE = "application/vnd.android.package-archive"
        const val SUFFIX = ".apk"
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
     * ExtractionResult with Uri if copy was Successfully else Error Message
     */
    fun copy(
        from: String, to: Uri, fileName: String
    ): ExtractionResult {
        return try {
            val extractedApk: Uri
            // Create Input Stream from APK source file
            FileInputStream(from).use { input ->
                // Create new APK file in destination folder
                DocumentsContract.createDocument(
                    context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(
                        to, DocumentsContract.getTreeDocumentId(to)
                    ), MIME_TYPE, "$fileName.apk"
                ).let { outputFile ->
                    extractedApk = outputFile!!
                    // Create Output Stream for target APK file
                    context.contentResolver.openOutputStream(outputFile)
                }.use { output ->
                    // Copy from Input to Output Stream
                    input.copyTo(output!!)
                }
            }
            ExtractionResult.Success(extractedApk)
        } catch (fnf_e: FileNotFoundException) {
            fnf_e.printStackTrace()
            ExtractionResult.Failure(fnf_e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            ExtractionResult.Failure(e.message)
        }
    }

    /**
     * Creates a Uri for Provider
     * @param app Application for sharing
     * @return Shareable Uri of Application APK
     */
    fun shareURI(app: ApplicationModel, appName: String): Uri {
        val file = try {
            File(app.appSourceDirectory).copyTo(
                File(
                    context.cacheDir, "$appName.apk"
                ), true
            )
        } catch (e: FileAlreadyExistsException) {
            File.createTempFile(appName, SUFFIX, context.cacheDir)
        }
        return FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.provider", file
        )
    }

    fun createZip(files: Array<String>, to: Uri, fileName: String): ExtractionResult {
        return try {
            val extractedApk: Uri
            DocumentsContract.createDocument(
                context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(
                    to, DocumentsContract.getTreeDocumentId(to)
                ), "application/octet-stream", "$fileName.xapk"
            ).let { outputFile ->
                extractedApk = outputFile!!
                // Create Output Stream for target APK file
                ZipOutputStream(context.contentResolver.openOutputStream(outputFile))
            }.use { output ->
                for (file in files) FileInputStream(file).use { input ->
                    val entry = ZipEntry(file.substring(file.lastIndexOf("/") + 1))
                    output.putNextEntry(entry)
                    input.copyTo(output)
                }
            }
            ExtractionResult.Success(extractedApk)
        } catch (fnf_e: FileNotFoundException) {
            fnf_e.printStackTrace()
            ExtractionResult.Failure(fnf_e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            ExtractionResult.Failure(e.message)
        }
    }

    /**
     * Creates a Uri for Provider
     * @param app Application for sharing
     * @return Shareable Uri of Application APK
     */
    fun shareZip(app: ApplicationModel, appName: String): Uri {
        val files = arrayOf(app.appSourceDirectory, *app.appSplitSourceDirectories!!)

        val outFile = File(context.cacheDir, "$appName.xapk").let {
            if (it.createNewFile()) it else File.createTempFile(appName, ".xapk", context.cacheDir)
        }
        ZipOutputStream(outFile.outputStream()).use { output ->
            for (file in files) FileInputStream(file).use { input ->
                val entry = ZipEntry(file.substring(file.lastIndexOf("/") + 1))
                output.putNextEntry(entry)
                input.copyTo(output)
            }
        }
        return FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.provider", outFile
        )
    }

    /**
     * Takes uri from Document file, and checks if Document exists
     * @param uri Document uri
     * @return true if document exist else false
     */
    fun doesDocumentExist(uri: Uri): Boolean {
        val documentUri = if (DocumentsContract.isTreeUri(uri)) {
            val documentId = if (DocumentsContract.isDocumentUri(
                    context, uri
                )
            ) DocumentsContract.getDocumentId(
                uri
            ) else DocumentsContract.getTreeDocumentId(uri)
            DocumentsContract.buildDocumentUriUsingTree(
                uri, documentId
            )
        } else uri
        return try {
            context.contentResolver.query(
                documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                null,
                null,
                null
            )?.use { cursor -> cursor.count > 0 } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Takes uri from Document file and queries file info
     * @param uri Document file uri
     * @param projection DocumentContract.Document.COLUMN_... of information to retrieve from file
     * @return DocumentFile with requested information or null if failed
     */
    fun getDocumentInfo(
        uri: Uri, vararg projection: String = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
    ): DocumentFile? {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val documentIdIndex =
                cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val displayNameIndex =
                cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val lastModifiedIndex =
                cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            if (cursor.moveToFirst()) {
                val documentId =
                    if (documentIdIndex != -1) cursor.getString(documentIdIndex) else null
                val name = if (displayNameIndex != -1) cursor.getString(displayNameIndex) else null
                val lastModified =
                    if (lastModifiedIndex != -1) cursor.getLong(lastModifiedIndex) else null
                val size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
                val mimeType = if (mimeTypeIndex != -1) cursor.getString(mimeTypeIndex) else null

                return DocumentFile(uri, documentId, name, lastModified, size, mimeType)
            }
        }
        return null
    }
}