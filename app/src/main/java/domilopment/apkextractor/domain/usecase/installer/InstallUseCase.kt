package domilopment.apkextractor.domain.usecase.installer

import android.net.Uri
import domilopment.apkextractor.data.model.install.InstallStrategy
import domilopment.apkextractor.data.repository.installation.InstallationRepository
import domilopment.apkextractor.data.model.install.InstallationCallback
import kotlinx.coroutines.flow.Flow

interface InstallUseCase {
    operator fun invoke(fileUri: Uri, strategy: InstallStrategy): Flow<InstallationCallback>
}

class InstallUseCaseImpl(
    private val installationRepository: InstallationRepository,
) : InstallUseCase {
    override operator fun invoke(
        fileUri: Uri, strategy: InstallStrategy
    ): Flow<InstallationCallback> {
        return strategy.install(installationRepository, fileUri)
    }
}
