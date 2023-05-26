package domilopment.apkextractor.utils

import android.content.Context
import android.provider.DocumentsContract
import domilopment.apkextractor.data.PackageArchiveModel

class ListOfAPKs(private val context: Context) {
    /**
     * Update Installed APK lists
     */
    fun apkFiles(): List<PackageArchiveModel> {
        val packageArchiveModels = mutableListOf<PackageArchiveModel>()

        SettingsManager(context).saveDir()?.let { uri ->
            val documentUri = DocumentsContract.getTreeDocumentId(uri)?.let { documentId ->
                DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
            }
            return@let if (!DocumentsContract.isTreeUri(documentUri)) null
            else DocumentsContract.buildChildDocumentsUriUsingTree(
                documentUri, DocumentsContract.getDocumentId(documentUri)
            )
        }?.also { childrenUri ->
            context.contentResolver.query(
                childrenUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ), null, null, null
            )?.use { cursor ->
                val documentIdIndex =
                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val displayNameIndex =
                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val lastModifiedIndex =
                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val mimeTypeIndex =
                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(documentIdIndex)
                    val displayName = cursor.getString(displayNameIndex)
                    val lastModified = cursor.getLong(lastModifiedIndex)
                    val size = cursor.getLong(sizeIndex)
                    val mimeType = cursor.getString(mimeTypeIndex)

                    val documentUri =
                        DocumentsContract.buildDocumentUriUsingTree(childrenUri, documentId)

                    if (mimeType == FileUtil.MIME_TYPE) packageArchiveModels.add(
                        PackageArchiveModel(
                            documentUri, displayName, lastModified, size
                        )
                    )
                }
            }
        }
        return packageArchiveModels
    }
}
