package domilopment.apkextractor.utils

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

    fun createDocumentFile(
        context: Context, to: Uri, fileName: String, mimeType: String, suffix: String
    ): Uri? {
        // Create new APK file in destination folder
        return DocumentsContract.createDocument(
            context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(
                to, DocumentsContract.getTreeDocumentId(to)
            ), mimeType, "$fileName.$suffix"
        )
    }

    object ZipUtil {
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
                // Create Output Stream for target APK file
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

        fun createTempZip(context: Context, appName: String, suffix: String = "zip"): File {
            return createTempFile(context, appName, suffix)
        }

        fun openZipOutputStream(file: File): ZipOutputStream {
            return ZipOutputStream(BufferedOutputStream(file.outputStream()))
        }

        fun openZipOutputStream(context: Context, uri: Uri): ZipOutputStream {
            return ZipOutputStream(
                BufferedOutputStream(
                    context.contentResolver.openOutputStream(
                        uri
                    )
                )
            )
        }

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

        fun writeToZip(output: ZipOutputStream, filePath: String) {
            writeToZip(output, File(filePath))
        }
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
        context: Context, from: String, to: Uri, fileName: String, mimeType: String, suffix: String
    ): Uri {
        val extractedApk: Uri
        // Create Input Stream from APK source file
        FileInputStream(from).use { input ->
            // Create new APK file in destination folder
            createDocumentFile(context, to, fileName, mimeType, suffix).let { outputFile ->
                extractedApk = outputFile!!
                // Create Output Stream for target APK file
                context.contentResolver.openOutputStream(outputFile)
            }.use { output ->
                // Copy from Input to Output Stream
                input.copyTo(output!!)
            }
        }
        return extractedApk
    }

    fun createTempFile(context: Context, appName: String, suffix: String): File {
        return File(context.cacheDir, "$appName.$suffix").let {
            if (it.createNewFile()) it else File.createTempFile(
                appName, ".$suffix", context.cacheDir
            )
        }
    }

    /**
     * Creates a Uri for Provider
     * @param app Application for sharing
     * @return Shareable Uri of Application APK
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