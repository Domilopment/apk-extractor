package domilopment.apkextractor.dependencyInjection.packageArchive

import android.content.Context
import android.provider.DocumentsContract
import domilopment.apkextractor.data.apkList.AppPackageArchiveModel
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.data.apkList.ZipPackageArchiveModel
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.NonComparingMutableStateFlow
import domilopment.apkextractor.dependencyInjection.applications.ListOfApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ListOfAPKs private constructor(
    context: Context, private val preferences: PreferenceRepository
) {
    private val contentResolver = context.contentResolver

    private val _apks =
        NonComparingMutableStateFlow<MutableList<PackageArchiveModel>>(mutableListOf())

    val apks: Flow<List<PackageArchiveModel>> = _apks.asStateFlow()

    init {
        runBlocking { updateData() }
    }

    /**
     * Update Installed APK lists
     */
    suspend fun updateData() {
        val packageArchiveModels = mutableListOf<PackageArchiveModel>()

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
                        mimeType == FileUtil.FileInfo.APK.mimeType -> AppPackageArchiveModel(
                            documentUri, displayName, mimeType, lastModified, size
                        )

                        displayName.endsWith(".xapk") || displayName.endsWith(".apks") -> ZipPackageArchiveModel(
                            documentUri, displayName, mimeType, lastModified, size
                        )

                        else -> continue
                    }

                    packageArchiveModels.add(model)
                }
            }
        }
        _apks.value = packageArchiveModels
    }

    suspend fun add(apk: PackageArchiveModel) = withContext(Dispatchers.IO) {
        _apks.update { apks ->
            apks.toMutableList().apply {
                add(apk)
            }
        }
    }

    suspend fun remove(apk: PackageArchiveModel) = withContext(Dispatchers.IO) {
        _apks.update { apks ->
            apks.toMutableList().apply {
                removeIf { it.fileUri == apk.fileUri }
            }
        }
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
