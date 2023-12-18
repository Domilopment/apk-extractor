package domilopment.apkextractor.utils.dataSources

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.preference.PreferenceManager
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.NonComparingMutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow

class ListOfAPKs private constructor(context: Context) {
    private val contentResolver = context.contentResolver
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val _apks = NonComparingMutableStateFlow<MutableList<PackageArchiveModel>>(mutableListOf())

    val apks: Flow<List<PackageArchiveModel>> = _apks.asStateFlow()

    init {
        updateData()
    }

    /**
     * Update Installed APK lists
     */
    fun updateData() {
        val packageArchiveModels = mutableListOf<PackageArchiveModel>()

        sharedPreferences.getString(Constants.PREFERENCE_KEY_SAVE_DIR, null)?.let { Uri.parse(it) }
            ?.let { uri ->
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
                        val mimeType = cursor.getString(mimeTypeIndex)
                        if (mimeType != FileUtil.MIME_TYPE) continue

                        val documentId = cursor.getString(documentIdIndex)
                        val displayName = cursor.getString(displayNameIndex)
                        val lastModified = cursor.getLong(lastModifiedIndex)
                        val size = cursor.getLong(sizeIndex)

                        val documentUri =
                            DocumentsContract.buildDocumentUriUsingTree(childrenUri, documentId)

                        packageArchiveModels.add(
                            PackageArchiveModel(
                                documentUri, displayName, lastModified, size
                            )
                        )
                    }
                }
            }
        _apks.tryEmit(packageArchiveModels)
    }

    suspend fun add(apk: PackageArchiveModel) {
        val apks = _apks.value.toMutableList()
        apks.add(apk)
        _apks.value = apks
    }

    suspend fun remove(apk: PackageArchiveModel) {
        val apks = _apks.value.toMutableList()
        apks.removeIf { it.fileUri == apk.fileUri }
        _apks.value = apks
    }

    companion object {
        private lateinit var INSTANCE: ListOfAPKs

        fun getPackageArchives(context: Context): ListOfAPKs {
            synchronized(ListOfApps::class.java) {
                if (!Companion::INSTANCE.isInitialized) {
                    INSTANCE = ListOfAPKs(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }
}
