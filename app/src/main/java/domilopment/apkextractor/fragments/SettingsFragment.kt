package domilopment.apkextractor.fragments

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ListOfAPKs
import domilopment.apkextractor.apkNamePreferenceDialog.ApkNamePreference
import domilopment.apkextractor.apkNamePreferenceDialog.ApkNamePreferenceDialogFragmentCompat
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlinx.coroutines.*
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var settingsManager: SettingsManager

    private val chooseSaveDir =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK)
                it.data?.also { saveDirUri -> takeUriPermission(saveDirUri) }
        }

    private val allowNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            handleAutoBackupService(true)
            findPreference<SwitchPreferenceCompat>("auto_backup")?.isChecked = true
        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.auto_backup_notification_permission_request_rejected),
                Snackbar.LENGTH_LONG
            ).apply {
                (view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView)
                    .maxLines = 5
            }.show()
        }
    }

    private val ioDispatcher get() = Dispatchers.IO

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        settingsManager = SettingsManager(requireContext())
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        // Try if the preference is one of our custom Preferences
        var dialogFragment: DialogFragment? = null
        if (preference is ApkNamePreference) {
            // Create a new instance of ApkNamePreferenceDialogFragment with the key of the related Preference
            dialogFragment = ApkNamePreferenceDialogFragmentCompat.newInstance(preference.key)
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(
                parentFragmentManager,
                "androidx.preference.PreferenceFragment.DIALOG"
            )
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        // Shows Version Number in Settings
        findPreference<Preference>("version")!!
            .title = getString(R.string.version, BuildConfig.VERSION_NAME)

        // A Link to Projects Github Repo
        findPreference<Preference>("github")!!.setOnPreferenceClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(
                    requireContext(),
                    Uri.parse("https://github.com/domilopment/apkextractor")
                )
            return@setOnPreferenceClickListener true
        }
        // A Link to Apps Google Play Page
        findPreference<Preference>("googleplay")?.setOnPreferenceClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        requireContext().packageManager.getPackageInfo(
                            "com.android.vending",
                            PackageManager.PackageInfoFlags.of(0L)
                        )
                    else
                        requireContext().packageManager.getPackageInfo(
                            "com.android.vending",
                            0
                        )
                    setPackage(packageInfo.packageName)
                    Uri.parse("market://details?id=${requireContext().packageName}")
                } catch (e: PackageManager.NameNotFoundException) {
                    Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                }
            }.also {
                startActivity(it)
            }
            return@setOnPreferenceClickListener true
        }

        // A Link to my Privacy Policy Page
        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(
                    requireContext(),
                    Uri.parse("https://sites.google.com/view/domilopment/privacy-policy")
                )
            return@setOnPreferenceClickListener true
        }

        // Select a Dir to Save APKs
        findPreference<Preference>("choose_dir")?.setOnPreferenceClickListener {
            FileHelper(requireActivity()).chooseDir(chooseSaveDir)
            return@setOnPreferenceClickListener true
        }
        // Change between Day and Night Mode
        findPreference<ListPreference>("list_preference_ui_mode")?.setOnPreferenceChangeListener { _, newValue ->
            settingsManager.changeUIMode(newValue.toString())
            return@setOnPreferenceChangeListener true
        }
        findPreference<SwitchPreferenceCompat>("use_material_you")?.apply {
            setOnPreferenceChangeListener { _, _ ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.use_material_you_toast),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnPreferenceChangeListener true
            }
            isVisible = DynamicColors.isDynamicColorAvailable()
        }
        // Clear App cache
        findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            if (activity?.cacheDir!!.deleteRecursively())
                Toast.makeText(
                    activity,
                    getString(R.string.clear_cache_success),
                    Toast.LENGTH_SHORT
                ).show()
            else
                Toast.makeText(
                    activity,
                    getString(R.string.clear_cache_failed),
                    Toast.LENGTH_SHORT
                )
                    .show()
            return@setOnPreferenceClickListener true
        }
        // Fill List of Apps for Auto Backup with Installed or Updated Apps
        findPreference<MultiSelectListPreference>("app_list_auto_backup")?.apply {
            lifecycleScope.launch {
                val load = async(ioDispatcher) {
                    val appEntries = mutableListOf<String>()
                    val appValues = mutableListOf<String>()

                    settingsManager.sortData(
                        ListOfAPKs(context.packageManager).userApps
                                + ListOfAPKs(context.packageManager).updatedSystemApps,
                        SettingsManager.SORT_BY_NAME
                    ).forEach {
                        appEntries.add(it.appName)
                        appValues.add(it.appPackageName)
                    }

                    entries = appEntries.toTypedArray()
                    entryValues = appValues.toTypedArray()

                    return@async true
                }
                isEnabled = load.await()
            }
        }
        // Start and Stop AutoBackupService
        findPreference<SwitchPreferenceCompat>("auto_backup")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                newValue as Boolean

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (newValue && ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_DENIED
                    ) {
                        allowNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnPreferenceChangeListener false
                    }
                }

                handleAutoBackupService(newValue)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                return@setOnPreferenceChangeListener notificationManager.areNotificationsEnabled() || !newValue
            }
        }
        // Change App Language
        findPreference<ListPreference>("list_preference_locale_list")?.apply {
            val localeMap = mapOf(
                null to getString(R.string.locale_list_default),
                Locale.ENGLISH.toLanguageTag() to getString(R.string.locale_list_en),
                Locale.GERMANY.toLanguageTag() to getString(R.string.locale_list_de_de)
            ).withDefault {
                getString(
                    R.string.locale_list_not_supported,
                    Locale.forLanguageTag(it!!).displayName
                )
            }
            summary =
                getString(
                    R.string.locale_list_summary,
                    localeMap.getValue(AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag())
                )
            setOnPreferenceChangeListener { _, n ->
                settingsManager.setLocale(n as String)
                return@setOnPreferenceChangeListener true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.title_activity_settings)
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // No Menu Provided
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        return true
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun handleAutoBackupService(newValue: Boolean) {
        if (newValue and !AutoBackupService.isRunning)
            requireActivity().startService(
                Intent(
                    requireContext(),
                    AutoBackupService::class.java
                )
            )
        else if (!newValue and AutoBackupService.isRunning)
            requireActivity().stopService(
                Intent(
                    requireContext(),
                    AutoBackupService::class.java
                )
            )
    }

    /**
     * Take Uri Permission for Save Dir
     * @param data return Intent from choose Save Dir
     */
    private fun takeUriPermission(data: Intent) {
        (data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
            settingsManager.saveDir()?.also { oldPath ->
                if (oldPath in requireContext().contentResolver.persistedUriPermissions.map { it.uri })
                    requireContext().contentResolver.releasePersistableUriPermission(oldPath, this)
            }
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                .putString("dir", data.data.toString()).apply()
            requireContext().contentResolver.takePersistableUriPermission(data.data!!, this)
        }
    }
}