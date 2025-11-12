package domilopment.apkextractor.utils

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.RemoteException
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import domilopment.apkextractor.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtil {
    data class DocumentFile(
        val uri: Uri,
        val documentId: String?,
        val displayName: String?,
        val lastModified: Long?,
        val size: Long?,
        val mimeType: String?
    )

    enum class FileInfo(val mimeType: String, val suffix: String) {
        APK("application/vnd.android.package-archive", "apk"),
        XAPK("application/octet-stream", "xapk"),
        APKS("application/octet-stream", "apks");

        companion object {
            fun fromSuffix(suffix: String): FileInfo? {
                return entries.find { it.suffix == suffix }
            }
        }
    }

    /**
     * Create a new Document file
     * @param context
     * App Context
     * @param to
     * Uri of location file should be created in
     * @param fileName
     * Name of newly created file
     * @param mimeType
     * MIME type for file
     * @param suffix
     * suffix for file type (without '.')
     */
    fun createDocumentFile(
        context: Context, to: Uri, fileName: String, mimeType: String, suffix: String
    ): Uri? {
        // Create new file in destination folder
        return DocumentsContract.createDocument(
            context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(
                to, DocumentsContract.getTreeDocumentId(to)
            ), mimeType, "$fileName.$suffix"
        )
    }

    /**
     * Delete the given document.
     * @param context
     * @param uri
     * Uri of document that shall be deleted
     * @return
     * If document was deleted or not
     */
    suspend fun deleteDocument(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (_: FileNotFoundException) {
            // File is already gone, consider it successfully deleted
            true
        } catch (e: Exception) {
            // expected exception types: SecurityException, IllegalArgumentException, UnsupportedOperationException, IllegalStateException, NullPointerException, SQLiteException, RemoteException, IOException
            Timber.tag("FileUtil.deleteDocument: ${e.javaClass.simpleName}").e(e)

            // Final check: if file doesn't exist after exception, consider it deleted
            !doesDocumentExist(context, uri)
        }
    }

    /**
     * Provider for utility functions to hande different zip file related operations
     */
    object ZipUtil {
        /**
         * Creates a new file of type zip in Persistent Storage
         * @param context
         * App Context
         * @param fileName
         * The name for the file
         * @param mimeType
         * MIME type of new file, default will be 'application/zip'
         * @param suffix
         * suffix for file (without '.'), default will be 'zip'
         */
        fun createPersistentZip(
            context: Context,
            to: Uri,
            fileName: String,
            mimeType: String = "application/zip",
            suffix: String = "zip"
        ): Uri? {
            return createDocumentFile(
                context, to, fileName, mimeType = mimeType, suffix = suffix
            )
        }

        /**
         * Copies a collection of files to public storage and packs them into a zip file
         * @param context
         * App context
         * @param files
         * List of sources of existing files
         * @param to
         * Destination Folder for file
         * @param fileName
         * Name for newly Saved File
         * @param mimeType
         * MIME type for the new File
         * @param suffix
         * suffix for file type (without '.'), if MIME type is unknown and suffix can't be inferred
         * @return
         * Uri of newly created file
         */
        fun copy(
            context: Context,
            files: Array<String>,
            to: Uri,
            fileName: String,
            mimeType: String = "application/zip",
            suffix: String = "zip"
        ): Uri {
            val copy: Uri
            createDocumentFile(
                context, to, fileName, mimeType = mimeType, suffix = suffix
            ).let { outputFile ->
                // Create Output Stream for target file
                copy = outputFile!!
                ZipOutputStream(context.contentResolver.openOutputStream(outputFile)?.buffered())
            }.use {
                for (file in files) writeToZip(it, file)
            }
            return copy
        }

        /**
         * Creates a new Temp file of type zip
         * @param context
         * App Context
         * @param fileName
         * The name for the file
         * @param suffix
         * suffix for file (without '.'), default will be 'zip'
         */
        fun createTempZip(context: Context, fileName: String, suffix: String = "zip"): File {
            return createTempFile(context, fileName, suffix)
        }

        /**
         * Open OutputStream to existing Zip file
         * @param file
         * File to location of Zip file
         * @return
         * ZipOutputStream
         */
        fun openZipOutputStream(file: File): ZipOutputStream {
            return ZipOutputStream(file.outputStream().buffered())
        }

        /**
         * Open OutputStream to existing Zip file
         * @param context
         * App Context
         * @param uri
         * Uri location of Zip file
         * @return
         * ZipOutputStream
         */
        fun openZipOutputStream(context: Context, uri: Uri): ZipOutputStream {
            return ZipOutputStream(context.contentResolver.openOutputStream(uri)?.buffered())
        }

        /**
         * Function for writing File to an existing Zip
         * @param output
         * OutputStream for writing to Zip file
         * @param file
         * file that should be written to Zip
         */
        fun writeToZip(output: ZipOutputStream, file: File) {
            FileInputStream(file).buffered().use { input ->
                val entry = ZipEntry(file.name).apply {
                    size = file.length()
                }
                output.putNextEntry(entry)
                val bufferedOutputStream = output.buffered()
                input.copyTo(bufferedOutputStream)
                bufferedOutputStream.flush()
                output.closeEntry()
            }
        }

        /**
         * Function for writing File to an existing Zip
         * @param output
         * OutputStream for writing to Zip file
         * @param filePath
         * path of existing file that should be written to Zip
         */
        fun writeToZip(output: ZipOutputStream, filePath: String) {
            writeToZip(output, File(filePath))
        }
    }


    /**
     * Copy file from Source to Chosen Save Director with Name
     * @param context
     * App context
     * @param from
     * Source of existing file
     * @param to
     * Destination Folder for file
     * @param fileName
     * Name for newly Saved File
     * @param mimeType
     * MIME type for the new File
     * @param suffix
     * suffix for file type (without '.'), if MIME type is unknown and suffix can't be inferred
     * @return
     * Uri of newly created file
     */
    fun copy(
        context: Context, from: String, to: Uri, fileName: String, mimeType: String, suffix: String
    ): Uri {
        val fileUri: Uri
        // Create Input Stream from source file
        FileInputStream(from).use { input ->
            // Create new file in destination folder
            createDocumentFile(context, to, fileName, mimeType, suffix).let { outputFile ->
                fileUri = outputFile!!
                // Create Output Stream for target file
                context.contentResolver.openOutputStream(outputFile)
            }.use { output ->
                // Copy from Input to Output Stream
                input.copyTo(output!!)
            }
        }
        return fileUri
    }

    /**
     * creates a new Temp file in app cache, tries to apply the name for new file, if file exist create new Temp File instead
     * @param context app context
     * @param fileName Name for the temp file
     * @param suffix suffix for file type (without '.')
     */
    fun createTempFile(context: Context, fileName: String, suffix: String): File {
        // Ensure the cache directory exists. If not, create it.
        context.cacheDir.mkdirs()
        return File(context.cacheDir, "$fileName.$suffix").let {
            if (it.createNewFile()) it else File.createTempFile(
                fileName, ".$suffix", context.cacheDir
            )
        }
    }

    /**
     * Takes in File size in Bytes and returns size in MB
     * @param length File size as Long
     */
    fun getBytesSizeInMB(length: Long): Float {
        return length / (1000.0F * 1000.0F) // Calculate MB Size
    }

    /**
     * Creates a Uri for Provider
     * @param file File for sharing
     * @return Shareable Uri of file
     */
    fun createShareUriForFile(context: Context, file: File): Uri = FileProvider.getUriForFile(
        context, "${BuildConfig.APPLICATION_ID}.provider", file,
    )

    /**
     * Takes uri from Document file, and checks if Document exists
     * @param uri Document uri
     * @return true if document exist else false
     */
    fun doesDocumentExist(context: Context, uri: Uri): Boolean {
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
        } catch (_: Exception) {
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
        context: Context, uri: Uri, vararg projection: String = arrayOf(
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
                return DocumentFile(
                    uri = uri,
                    documentId = cursor.getStringOrNull(documentIdIndex),
                    displayName = cursor.getStringOrNull(displayNameIndex),
                    lastModified = cursor.getLongOrNull(lastModifiedIndex),
                    size = cursor.getLongOrNull(sizeIndex),
                    mimeType = cursor.getStringOrNull(mimeTypeIndex)
                )
            }
        }
        return null
    }
}