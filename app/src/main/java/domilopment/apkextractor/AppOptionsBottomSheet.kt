package domilopment.apkextractor

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.databinding.AppOptionsBottomSheetBinding

class AppOptionsBottomSheet(private val app: Application) : BottomSheetDialogFragment() {
    private var _binding: AppOptionsBottomSheetBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        const val TAG = "app_options_bottom_sheet"
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
        binding.actionSaveApk.setOnClickListener {
            val path = SettingsManager(requireContext()).saveDir()

            if (FileHelper(requireActivity()).copy(
                    app.appSourceDirectory,
                    path!!,
                    SettingsManager(requireContext()).appName(app)
                )
            )
                Snackbar.make(
                    it,
                    getString(R.string.snackbar_successful_extracted).format(app.appName),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).show()
            else
                Snackbar.make(
                    it,
                    getString(R.string.snackbar_extraction_failed).format(app.appName),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(this.view).setTextColor(Color.RED).show()
        }

        // Share APK
        binding.actionShare.setOnClickListener {
            val file = FileHelper(requireContext()).shareURI(app)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = FileHelper.MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, file)
            }.let {
                Intent.createChooser(it, getString(R.string.action_share))
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
                startActivity(it)
            }
            dismiss()
        }

        // If App is User App make Uninstall Option visible
        if (app.appFlags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM)
            binding.actionUninstall.isVisible = true

        // Save App Image
        binding.actionSaveImage.setOnClickListener { view ->
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, app.appName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let {
                resolver.openOutputStream(it)
            }.use {
                if (app.appIcon.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
                    Snackbar.make(
                        view,
                        getString(R.string.snackbar_successful_saved_image),
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(this.view).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}