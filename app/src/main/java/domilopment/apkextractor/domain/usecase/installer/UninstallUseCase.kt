package domilopment.apkextractor.domain.usecase.installer

import android.app.Activity
import domilopment.apkextractor.dependencyInjection.installation.InstallationRepository

interface UninstallUseCase {
    suspend operator fun <T : Activity> invoke(packageName: String, cls: Class<T>)
}

class UninstallUseCaseImpl(
    private val uninstallRepository: InstallationRepository
) : UninstallUseCase {
    override suspend fun <T : Activity> invoke(packageName: String, cls: Class<T>) {
        uninstallRepository.uninstall(packageName, cls)
    }
}