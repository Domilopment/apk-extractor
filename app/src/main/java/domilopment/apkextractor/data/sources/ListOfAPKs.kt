package domilopment.apkextractor.data.sources

import android.content.Context
import android.provider.DocumentsContract
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.FileUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ListOfAPKs private constructor(
    context: Context, private val preferences: PreferenceRepository
) {
    private val contentResolver = context.contentResolver

    val apks = flow {
        val packageArchiveFiles = mutableListOf<PackageArchiveEntity>()

        preferences.saveDir.first()?.let { uri ->
            DocumentsContract.getTreeDocumentId(uri)?.let { documentId ->
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    uri, documentId
                )
            }
        }?.also { childrenUri ->
            contentResolver.query(
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

                    val model = when {
                        mimeType == FileUtil.FileInfo.APK.mimeType || displayName.endsWith(".${FileUtil.FileInfo.APKS.suffix}") || displayName.endsWith(
                            ".${FileUtil.FileInfo.XAPK.suffix}"
                        ) -> PackageArchiveEntity(
                            fileUri = documentUri,
                            fileName = displayName,
                            fileType = mimeType,
                            fileLastModified = lastModified,
                            fileSize = size
                        )

                        else -> continue
                    }

                    packageArchiveFiles.add(model)
                }
            }
        }

        emit(packageArchiveFiles)
    }


    companion object {
        private lateinit var INSTANCE: ListOfAPKs

        fun getPackageArchives(
            context: Context, preferenceRepository: PreferenceRepository
        ): ListOfAPKs {
            synchronized(ListOfApps::class.java) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = ListOfAPKs(context.applicationContext, preferenceRepository)
                }
            }
            return INSTANCE
        }
    }
}
