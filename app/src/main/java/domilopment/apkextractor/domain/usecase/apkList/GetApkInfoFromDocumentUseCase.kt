package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveDetailLoader
import domilopment.apkextractor.domain.mapper.PackageArchiveEntityToApkDetailModelMapper
import domilopment.apkextractor.utils.FileUtil

interface GetApkInfoFromDocumentUseCase {
    suspend operator fun invoke(uri: Uri): ApkModel.ApkDetailModel?
}

class GetApkInfoFromDocumentUseCaseImpl(private val context: Context): GetApkInfoFromDocumentUseCase {
    override suspend operator fun invoke(uri: Uri): ApkModel.ApkDetailModel? {
        if (!FileUtil.doesDocumentExist(context, uri)) return null

        return FileUtil.getDocumentInfo(
            context,
            uri,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE
        )?.let {
            PackageArchiveDetailLoader.load(context, it)
        }?.let {
            PackageArchiveEntityToApkDetailModelMapper.map(it)
        }
    }
}