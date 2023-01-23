package domilopment.apkextractor

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlinx.coroutines.launch
import java.io.File
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
            if (!isPackageInstalled(app.appPackageName)) {
                model.removeApp(app)
                dismiss()
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
                    uiState.selectedApplicationModel?.let {
                        app = it
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

        setupApplicationInfo()
        setupApplicationActions()
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
            R.string.info_bottom_sheet_install_time, getAsFormatedDate(app.appInstallTime)
        )

        // Selected App last update time
        binding.selectedAppUpdateTime.text =
            getString(R.string.info_bottom_sheet_update_time, getAsFormatedDate(app.appUpdateTime))
    }

    /**
     * set up actions for selected APK
     */
    private fun setupApplicationActions() {
        // Save Apk
        binding.actionSaveApk.setOnClickListener { v ->
            val settingsManager = SettingsManager(requireContext())

            FileHelper(requireActivity()).copy(
                app.appSourceDirectory, settingsManager.saveDir()!!, settingsManager.appName(app)
            )?.let {
                Snackbar.make(
                    v,
                    getString(R.string.snackbar_successful_extracted, app.appName),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).show()
            } ?: run {
                Snackbar.make(
                    v,
                    getString(R.string.snackbar_extraction_failed, app.appName),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).setTextColor(Color.RED).show()
            }
        }

        // Share APK
        binding.actionShare.setOnClickListener {
            val file = FileHelper(requireContext()).shareURI(app)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = FileHelper.MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, file)
            }.let {
                Intent.createChooser(it, getString(R.string.share_intent_title))
            }
            shareApp.launch(shareIntent)
        }

        // Show app Settings
        binding.actionShowAppSettings.setOnClickListener {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.appPackageName, null)
            }.also {
                requireActivity().startActivity(it)
            }
        }

        // Open App
        binding.actionOpenApp.apply {
            app.launchIntent?.also { launchIntent ->
                setOnClickListener {
                    startActivity(launchIntent)
                }
            } ?: run {
                visibility = View.GONE
            }
        }

        // Uninstall App
        binding.actionUninstall.apply {
            setOnClickListener {
                Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", app.appPackageName, null)
                ).also {
                    uninstallApp.launch(it)
                }
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
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, app.appName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name)
                )
            }
            // Find all image files on the primary external storage device.
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            resolver.insert(imageCollection, contentValues)?.let {
                resolver.openOutputStream(it)
            }.use {
                if (app.appIcon.toBitmap()
                        .compress(Bitmap.CompressFormat.PNG, 100, it)
                ) Snackbar.make(
                    v, getString(R.string.snackbar_successful_save_image), Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).show()
            }
        }

        // Open installer store
        binding.actionOpenShop.apply {
            val packageManager = requireContext().packageManager
            app.installationSource?.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(
                    this, PackageManager.PackageInfoFlags.of(0L)
                )
                else requireContext().packageManager.getPackageInfo(this, 0)
            }?.onSuccess {
                text = packageManager.getApplicationLabel(it.applicationInfo)
            }
            setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${app.appPackageName}")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(
                        it,
                        getString(R.string.snackbar_no_activity_for_market_intent),
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(this@AppOptionsBottomSheet.view).show()
                }
            }
            isVisible = app.installationSource in listOf(
                "com.android.vending", "com.sec.android.app.samsungapps", "com.amazon.venezia"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Check if Application is Installed
     * @param packageName
     * Package Name of App
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requireContext().packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0L)
            )
            else requireContext().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Formats a Date-Time string into default Locale format
     * @param mills milliseconds since January 1, 1970, 00:00:00 GMT
     * @return formatted date-time string
     */
    private fun getAsFormatedDate(mills: Long): String {
        return SimpleDateFormat.getDateTimeInstance().format(Date(mills))
    }
}