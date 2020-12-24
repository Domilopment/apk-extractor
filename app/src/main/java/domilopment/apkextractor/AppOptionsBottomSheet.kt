package domilopment.apkextractor

import android.Manifest
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.databinding.AppOptionsBottomSheetBinding
import domilopment.apkextractor.fragments.MainViewModel
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import java.io.File

class AppOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: AppOptionsBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var app: Application

    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireContext()).defaultViewModelProviderFactory
    }

    companion object {
        const val TAG = "app_options_bottom_sheet"
        const val UNINSTALL_APP_RESULT = 777

        @JvmStatic
        fun newInstance(
            app: ApplicationInfo
        ): AppOptionsBottomSheet {
            val appOptionsBottomSheet = AppOptionsBottomSheet()
            val args: Bundle = Bundle().apply {
                putParcelable("app", app)
            }
            appOptionsBottomSheet.arguments = args
            return appOptionsBottomSheet
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<ApplicationInfo>("app")?.let {
            app = Application(it, requireContext().packageManager)
        } ?: return dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AppOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Save Apk
        binding.actionSaveApk.setOnClickListener { view ->
            val settingsManager = SettingsManager(requireContext())

            FileHelper(requireActivity()).copy(
                app.appSourceDirectory,
                settingsManager.saveDir()!!,
                settingsManager.appName(app)
            )?.let {
                Snackbar.make(
                    view,
                    getString(R.string.snackbar_successful_extracted, app.appName),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).show()
            } ?: run {
                Snackbar.make(
                    view,
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
            requireActivity().startActivityForResult(shareIntent, MainActivity.SHARE_APP_RESULT)
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
        binding.actionOpenApp.setOnClickListener {
            requireContext().packageManager.getLaunchIntentForPackage(app.appPackageName)?.also {
                startActivity(it)
            }
        }

        // Uninstall App
        binding.actionUninstall.setOnClickListener {
            Intent(
                Intent.ACTION_DELETE,
                Uri.fromParts("package", app.appPackageName, null)
            ).also {
                startActivityForResult(it, UNINSTALL_APP_RESULT)
            }
        }

        // If App is User App make Uninstall Option visible
        if (::app.isInitialized && (app.appFlags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM))
            binding.actionUninstall.isVisible = true

        // Save App Image
        binding.actionSaveImage.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
                return@setOnClickListener
            }
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, app.appName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name)
                    )
            }
            // Find all image files on the primary external storage device.
            val imageCollection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            resolver.insert(imageCollection, contentValues)?.let {
                resolver.openOutputStream(it)
            }.use {
                if (app.appIcon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
                    Snackbar.make(
                        view,
                        getString(R.string.snackbar_successful_save_image),
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(this.view).show()
            }
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
            requireContext().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Checks if All Permissions Granted on Runtime
     * @param requestCode
     * @param permissions
     * All Permissions the App needs
     * @param grantResults
     * Array of grant values from permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    binding.actionSaveImage.callOnClick()
                else
                    Snackbar.make(
                        binding.actionSaveImage,
                        getString(R.string.snackbar_need_permission_save_image),
                        Snackbar.LENGTH_LONG
                    ).setTextColor(Color.RED).setAnchorView(this.view).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UNINSTALL_APP_RESULT ->
                if (!isPackageInstalled(app.appPackageName)) {
                    dismiss()
                    model.removeApp(app)
                }
        }
    }
}