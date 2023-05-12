package domilopment.apkextractor.installApk

import android.content.pm.PackageInstaller
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.ProgressDialogFragment
import domilopment.apkextractor.ui.fragments.MainFragment
import domilopment.apkextractor.ui.fragments.MainViewModel

class PackageInstallerSessionCallback(
    private val mainFragment: MainFragment,
    private val model: MainViewModel
) : PackageInstaller.SessionCallback() {
    private val packageInstaller =
        mainFragment.requireContext().applicationContext.packageManager.packageInstaller
    private var packageName: String? = null
    var initialSessionId: Int = -1

    override fun onCreated(sessionId: Int) {
        if (sessionId != initialSessionId) return

        val progressDialog =
            ProgressDialogFragment.newInstance(R.string.progress_dialog_title_install)
        progressDialog.show(mainFragment.parentFragmentManager, "ProgressDialogFragment")
    }

    override fun onBadgingChanged(sessionId: Int) {
        // Not used
    }

    override fun onActiveChanged(sessionId: Int, active: Boolean) {
        // Not used
    }

    override fun onProgressChanged(sessionId: Int, progress: Float) {
        if (sessionId != initialSessionId) return

        packageName = packageInstaller.getSessionInfo(sessionId)?.appPackageName
        //model.updateInstallApkStatus(progress, packageName)
    }

    override fun onFinished(sessionId: Int, success: Boolean) {
        if (sessionId != initialSessionId) return

        packageInstaller.unregisterSessionCallback(this)
        model.resetProgress()
        MaterialAlertDialogBuilder(mainFragment.requireContext()).apply {
            if (success) {
                setMessage(mainFragment.getString(R.string.installation_result_dialog_success_message, packageName))
                setTitle(R.string.installation_result_dialog_success_title)
                model.updateApps()
            } else {
                setMessage(mainFragment.getString(R.string.installation_result_dialog_failed_message, packageName))
                setTitle(R.string.installation_result_dialog_failed_title)
            }
            setPositiveButton(R.string.installation_result_dialog_ok) { alert, _ ->
                alert.dismiss()
            }
        }.show()
    }
}