package domilopment.apkextractor

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.databinding.AppOptionsBottomSheetBinding
import domilopment.apkextractor.fragments.MainViewModel
import domilopment.apkextractor.utils.*
import domilopment.apkextractor.utils.apkActions.ApkActionsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: AppOptionsBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var app: ApplicationModel

    private val shareApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireContext().cacheDir.deleteRecursively()
        }

    private val uninstallApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val appUninstalled =
                !Utils.isPackageInstalled(requireContext().packageManager, app.appPackageName)
            if (appUninstalled || hasAppInfoChanged(app)) {
                model.removeApp(app)
                if (appUninstalled) dismiss()
            }
        }

    private val saveImage =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) binding.actionSaveImage.callOnClick()
            else Snackbar.make(
                binding.actionSaveImage,
                getString(R.string.snackbar_need_permission_save_image),
                Snackbar.LENGTH_LONG
            ).setTextColor(Color.RED).setAnchorView(this.view).show()
        }

    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireActivity().application).defaultViewModelProviderFactory
    }

    companion object {
        const val TAG = "app_options_bottom_sheet"

        @JvmStatic
        fun newInstance(
            packageName: String
        ): AppOptionsBottomSheet {
            val appOptionsBottomSheet = AppOptionsBottomSheet()
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
                model.appOptionsBottomSheetUIState.collect { uiState ->
                    uiState.selectedApplicationModel?.also {
                        app = it
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
        _binding = AppOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BottomSheetBehavior.from(view.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            peekHeight = 0
        }

        try {
            setupApplicationInfo()
            setupApplicationActions()
        } catch (e: Exception) {
            // Catch error if app info's not available
        }
    }

    override fun onStart() {
        super.onStart()
        // dismiss dialog and remove app from list, if it was uninstalled while apk extractor was in background
        if (!Utils.isPackageInstalled(requireContext().packageManager, app.appPackageName)) {
            model.removeApp(app)
            dismiss()
        }
    }

    /**
     * set up information from selected APK in bottom sheet layout
     */
    private fun setupApplicationInfo() {
        // Selected App Icon
        binding.selectedAppIcon.setImageDrawable(app.appIcon)

        // Selected App Name on top of Bottom Sheet
        binding.selectedAppName.text = app.appName

        // Selected App Package Name
        binding.selectedAppPackageName.text = app.appPackageName

        // Selected App source directory
        binding.selectedAppSourceDirectory.text =
            getString(R.string.info_bottom_sheet_source_directory, app.appSourceDirectory)

        // Selected App APK size
        binding.selectedAppApkSize.text =
            getString(R.string.info_bottom_sheet_apk_size, app.apkSize)

        // Selected App version name
        binding.selectedAppVersionName.text =
            getString(R.string.info_bottom_sheet_version_name, app.appVersionName)

        // Selected App version code
        binding.selectedAppVersionNumber.text =
            getString(R.string.info_bottom_sheet_version_number, app.appVersionCode)

        // Selected App installation time
        binding.selectedAppInstallTime.text = getString(
            R.string.info_bottom_sheet_install_time, getAsFormattedDate(app.appInstallTime)
        )

        // Selected App last update time
        binding.selectedAppUpdateTime.text =
            getString(R.string.info_bottom_sheet_update_time, getAsFormattedDate(app.appUpdateTime))

        // Selected app installation source
        binding.selectedAppInstaller.apply {
            app.installationSource?.runCatching {
                Utils.getPackageInfo(requireContext().packageManager, this).applicationInfo
            }?.onSuccess {
                val sourceName =
                    if (it.packageName in Utils.listOfKnownStores) "" else requireContext().packageManager.getApplicationLabel(
                        it
                    )
                text = getString(R.string.info_bottom_sheet_installation_source, sourceName)
                isVisible = true
            }
        }
    }

    /**
     * set up actions for selected APK
     */
    private fun setupApplicationActions() {
        val apkOptions = ApkActionsManager(requireContext(), app)

        // Save Apk
        binding.actionSaveApk.setOnClickListener { v ->
            apkOptions.actionSave(v, requireView())
        }

        // Share APK
        binding.actionShare.setOnClickListener {
            apkOptions.actionShare(shareApp)
        }

        // Show app Settings
        binding.actionShowAppSettings.setOnClickListener {
            apkOptions.actionShowSettings()
        }

        // Open App
        binding.actionOpenApp.apply {
            setOnClickListener {
                apkOptions.actionOpenApp()
            }
            if (app.launchIntent == null) {
                visibility = View.GONE
            }
        }

        // Uninstall App
        binding.actionUninstall.apply {
            setOnClickListener {
                apkOptions.actionUninstall(uninstallApp)
            }
            // If App is User App make Uninstall Option visible
            isVisible =
                (app.appFlags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM) || (app.appUpdateTime > app.appInstallTime)
        }

        // Save App Image
        binding.actionSaveImage.setOnClickListener { v ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                saveImage.launch(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                )
                return@setOnClickListener
            }
            apkOptions.actionSaveImage(v, requireView())
        }

        // Open installer store
        binding.actionOpenShop.apply {
            val packageManager = requireContext().packageManager
            app.installationSource?.runCatching {
                Utils.getPackageInfo(packageManager, this)
            }?.onSuccess { installationSource ->
                chipIcon = packageManager.getApplicationIcon(installationSource.applicationInfo)
                text = packageManager.getApplicationLabel(installationSource.applicationInfo)
                setOnClickListener {
                    apkOptions.actionOpenShop(it, this@AppOptionsBottomSheet.requireView())
                }
                isVisible = installationSource.packageName in Utils.listOfKnownStores
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * check for displayed app, if app info (update time, version-name or code, sourceDir) has changed
     * @return Boolean true if app info had changed
     */
    private fun hasAppInfoChanged(app: ApplicationModel): Boolean {
        val packageInfo = Utils.getPackageInfo(requireContext().packageManager, app.appPackageName)
        return packageInfo.lastUpdateTime != app.appUpdateTime || packageInfo.versionName != app.appVersionName || Utils.versionCode(
            packageInfo
        ) != app.appVersionCode || packageInfo.applicationInfo.sourceDir != app.appSourceDirectory
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