package domilopment.apkextractor.ui.dialogs

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.databinding.ApkOptionsBottomSheetBinding
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.MainViewModel
import domilopment.apkextractor.utils.*
import kotlinx.coroutines.launch

class ApkOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ApkOptionsBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var apk: PackageArchiveModel

    private val uninstallApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (apk.appPackageName.isNullOrBlank()) return@registerForActivityResult

            val isAppUninstalled =
                !Utils.isPackageInstalled(requireContext().packageManager, apk.appPackageName!!)
            if (isAppUninstalled) {
                appListModel.removeApp(apk.appPackageName!!)
                binding.actionUninstallApp.isVisible = false
            }
        }

    private val model by activityViewModels<ApkListViewModel>()
    private val appListModel by activityViewModels<MainViewModel>()
    private val progressDialogViewModel by activityViewModels<ProgressDialogViewModel>()

    companion object {
        const val TAG = "app_options_bottom_sheet"

        @JvmStatic
        fun newInstance(): ApkOptionsBottomSheet {
            return ApkOptionsBottomSheet()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                model.akpOptionsBottomSheetUIState.collect { uiState ->
                    uiState.packageArchiveModel?.also {
                        apk = it
                    } ?: dismiss()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.apk_options_bottom_sheet, container, false
        )
        binding.lifecycleOwner = this
        binding.bottomSheet = this
        binding.apk = apk
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BottomSheetBehavior.from(view.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            peekHeight = 0
        }
    }

    override fun onStart() {
        super.onStart()
        // Set or Update Application Info
        if (!FileUtil(requireContext()).doesDocumentExist(apk.fileUri)) {
            model.remove(apk)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        model.selectPackageArchive(null)
    }

    fun forceRefresh(apk: PackageArchiveModel) {
        model.forceRefresh(apk)
    }

    fun shareApk(apk: PackageArchiveModel) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = FileUtil.MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, apk.fileUri)
        }, getString(R.string.share_intent_title)))
    }

    fun installApk(apk: PackageArchiveModel) {/*
        progressDialogViewModel.installApk(
            apk.fileUri,
            PackageInstallerSessionCallback(this, model, progressDialogViewModel)
        )
         */
    }

    fun deleteApk(apk: PackageArchiveModel) {
        DocumentsContract.deleteDocument(
            requireContext().contentResolver, apk.fileUri
        ).let { deleted ->
            Toast.makeText(
                context, getString(
                    if (deleted) {
                        model.remove(apk)
                        dismiss()
                        R.string.apk_action_delete_success
                    } else R.string.apk_action_delete_failed
                ), Toast.LENGTH_SHORT
            )
        }.show()
    }

    fun uninstallApp(apk: PackageArchiveModel) {
        uninstallApp.launch(
            Intent(
                Intent.ACTION_DELETE, Uri.fromParts("package", apk.appPackageName, null)
            )
        )
    }
}