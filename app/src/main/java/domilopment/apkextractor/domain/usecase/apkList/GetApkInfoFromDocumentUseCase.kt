package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveDetailLoader
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.FileUtil

interface GetApkInfoFromDocumentUseCase {
    suspend operator fun invoke(uri: Uri): PackageArchiveEntity?
}

class GetApkInfoFromDocumentUseCaseImpl(private val context: Context): GetApkInfoFromDocumentUseCase {
    override suspend operator fun invoke(uri: Uri): PackageArchiveEntity? {
        return FileUtil.getDocumentInfo(
            context,
            uri,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE
        )?.let {
            PackageArchiveDetailLoader.load(context, it)
        }
    }
}