package domilopment.apkextractor.ui

import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.databinding.ApkOptionsBottomSheetBinding
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ApkOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ApkOptionsBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var apk: PackageArchiveModel

    private val uninstallApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val isAppUninstalled =
                !Utils.isPackageInstalled(requireContext().packageManager, apk.appPackageName!!)
            if (isAppUninstalled) {
                // TODO: implement uninstall before restore old apk
            }
        }

    private val model by activityViewModels<ApkListViewModel> {
        ApkListViewModel(requireActivity().application).defaultViewModelProviderFactory
    }

    companion object {
        const val TAG = "app_options_bottom_sheet"

        @JvmStatic
        fun newInstance(
            packageName: String?
        ): ApkOptionsBottomSheet {
            val appOptionsBottomSheet = ApkOptionsBottomSheet()
            val args: Bundle = Bundle(1).apply {
                putString("package_name", packageName)
            }
            appOptionsBottomSheet.arguments = args
            return appOptionsBottomSheet
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                model.akpOptionsBottomSheetUIState.collect { uiState ->
                    uiState.selectedApplicationModel?.also {
                        apk = it
                        view?.also {
                            setupApplicationInfo()
                            setupApplicationActions()
                        }
                    } ?: dismiss()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ApkOptionsBottomSheetBinding.inflate(inflater, container, false)
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
        if (apk.exist()) {
            setupApplicationInfo()
            setupApplicationActions()
        } else {
            model.remove(apk)
            dismiss()
        }
    }

    /**
     * set up information from selected APK in bottom sheet layout
     */
    private fun setupApplicationInfo() {
        // Selected App Icon
        binding.selectedApkIcon.setImageDrawable(apk.appIcon)

        // Selected App Name on top of Bottom Sheet
        binding.selectedApkName.text = apk.appName

        // Selected App Package Name
        binding.selectedApkPackageName.text = apk.appPackageName

        binding.selectedApkFileUri.text =
            getString(R.string.apk_bottom_sheet_source_uri, apk.fileUri)

        binding.selectedApkFileName.text =
            getString(R.string.apk_bottom_sheet_file_name, apk.fileName)

        binding.selectedApkFileSize.text =
            getString(R.string.info_bottom_sheet_apk_size, apk.fileSize)

        binding.selectedApkFileLastModified.text = getString(
            R.string.apk_bottom_sheet_last_modified, getAsFormattedDate(apk.fileLastModified)
        )

        // Selected App version name
        binding.selectedApkVersionName.text =
            getString(R.string.info_bottom_sheet_version_name, apk.appVersionName)

        // Selected App version code
        binding.selectedApkVersionNumber.text =
            getString(R.string.info_bottom_sheet_version_number, apk.appVersionCode)
    }

    /**
     * set up actions for selected APK
     */
    private fun setupApplicationActions() {
        // Share APK
        binding.actionShareApk.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = FileHelper.MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, apk.fileUri)
            }, getString(R.string.share_intent_title)))
        }


        // Uninstall App
        binding.actionInstallApk.apply {
            visibility = View.GONE
            setOnClickListener {
                //model.installApk(data, PackageInstallerSessionCallback(this, model))
            }
        }

        // Open installer store
        binding.actionDeleteApk.setOnClickListener {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Formats a Date-Time string into default Locale format
     * @param mills milliseconds since January 1, 1970, 00:00:00 GMT
     * @return formatted date-time string
     */
    private fun getAsFormattedDate(mills: Long): String {
        return SimpleDateFormat.getDateTimeInstance().format(Date(mills))
    }
}