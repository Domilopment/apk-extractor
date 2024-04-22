package domilopment.apkextractor.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import domilopment.apkextractor.BuildConfig
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
        APK("application/vnd.android.package-archive", "apk"), XAPK(
            "application/octet-stream", "xapk"
        )
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
     * @param contentResolver
     * @param uri
     * Uri of document that shall be deleted
     * @return
     * If document was deleted or not
     */
    fun deleteDocument(context: Context, uri: Uri): Boolean {
        return DocumentsContract.deleteDocument(context.contentResolver, uri)
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
                ZipOutputStream(
                    BufferedOutputStream(
                        context.contentResolver.openOutputStream(
                            outputFile
                        )
                    )
                )
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
            return ZipOutputStream(BufferedOutputStream(file.outputStream()))
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
            return ZipOutputStream(
                BufferedOutputStream(
                    context.contentResolver.openOutputStream(
                        uri
                    )
                )
            )
        }

        /**
         * Function for writing File to an existing Zip
         * @param output
         * OutputStream for writing to Zip file
         * @param file
         * file that should be written to Zip
         */
        fun writeToZip(output: ZipOutputStream, file: File) {
            FileInputStream(file).use { input ->
                val entry = ZipEntry(file.name).apply {
                    size = file.length()
                }
                output.putNextEntry(entry)
                input.copyTo(output)
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