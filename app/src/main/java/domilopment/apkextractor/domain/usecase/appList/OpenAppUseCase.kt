package domilopment.apkextractor.domain.usecase.appList

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import domilopment.apkextractor.data.model.appList.ApplicationModel

interface OpenAppUseCase {
    suspend operator fun invoke(appDetailsModel: ApplicationModel)
}

class OpenAppUseCaseImpl(
    private val context: Context, private val removeAppUseCase: RemoveAppUseCase
) : OpenAppUseCase {
    /**
     * Launch App via Intent
     */
    private suspend fun openApp(launchIntent: Intent) {
        try {
            context.startActivity(launchIntent)
        } catch (_: ActivityNotFoundException) {
            launchIntent.`package`?.let { removeAppUseCase.invoke(it) }
        } catch (_: SecurityException) {
            // Permission denial
        }
    }

    override suspend fun invoke(appDetailsModel: ApplicationModel) {
        appDetailsModel.launchIntent?.let { openApp(it) }
    }
}