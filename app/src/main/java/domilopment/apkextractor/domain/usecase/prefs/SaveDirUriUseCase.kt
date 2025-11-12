package domilopment.apkextractor.domain.usecase.prefs

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import kotlinx.coroutines.flow.first

interface SaveDirUriUseCase {
    suspend operator fun invoke(uri: Uri)
}

class SaveDirUriUseCaseImpl(
    private val contentResolver: ContentResolver,
    private val preferenceRepository: PreferenceRepository
) : SaveDirUriUseCase {
    /**
     * Take Uri Permission for Save Dir
     * @param newUri content uri for selected save path
     */
    private suspend fun takeUriPermission(
        oldUri: Uri?, newUri: Uri, saveUri: suspend (Uri) -> Unit
    ) {
        val takeFlags: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        oldUri?.let { oldPath ->
            if (oldPath in contentResolver.persistedUriPermissions.map { it.uri } && oldPath != newUri) contentResolver.releasePersistableUriPermission(
                oldPath, takeFlags
            )
        }
        saveUri(newUri)
        contentResolver.takePersistableUriPermission(newUri, takeFlags)
    }

    override suspend fun invoke(uri: Uri) {
        val oldUri = preferenceRepository.saveDir.first()
        takeUriPermission(oldUri, uri, preferenceRepository::setSaveDir)
    }
}