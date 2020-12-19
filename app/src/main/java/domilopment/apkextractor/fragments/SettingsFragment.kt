package domilopment.apkextractor.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import domilopment.apkextractor.*
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ListOfAPKs
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Shows Version Number in Settings
        findPreference<Preference>("version")!!.title =
            getString(R.string.version, BuildConfig.VERSION_NAME)
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
                    setPackage(
                        requireContext().packageManager.getPackageInfo(
                            "com.android.vending",
                            0
                        ).packageName
                    )
                    Uri.parse("market://details?id=${requireContext().packageName}")
                } catch (e: PackageManager.NameNotFoundException) {
                    Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                }
            }.also {
                startActivity(it)
            }
            return@setOnPreferenceClickListener true
        }
        // Select a Dir to Save APKs
        findPreference<Preference>("choose_dir")?.setOnPreferenceClickListener {
            FileHelper(requireActivity()).chooseDir(requireActivity())
            return@setOnPreferenceClickListener true
        }
        // Change between Day and Night Mode
        findPreference<ListPreference>("list_preference_ui_mode")?.setOnPreferenceChangeListener { _, newValue ->
            SettingsManager(requireContext()).changeUIMode(newValue.toString())
            return@setOnPreferenceChangeListener true
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
                Toast.makeText(activity, getString(R.string.clear_cache_failed), Toast.LENGTH_SHORT)
                    .show()
            return@setOnPreferenceClickListener true
        }
        // Fill List of Apps for Aut Backup with Installed or Updated Apps
        findPreference<MultiSelectListPreference>("app_list_auto_backup")?.apply {
            lifecycleScope.launch {
                val load = async(Dispatchers.IO) {
                    val appEntries = mutableListOf<String>()
                    val appValues = mutableListOf<String>()

                    SettingsManager(context).sortData(
                        ListOfAPKs(context.packageManager).userApps
                                + ListOfAPKs(context.packageManager).updatedSystemApps,
                        0
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

                return@setOnPreferenceChangeListener true
            }
        }
        // Check if user has Selected Name Options
        findPreference<MultiSelectListPreference>("app_save_name")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                return@setOnPreferenceChangeListener if (
                    (newValue as Set<*>).contains("name") or (newValue).contains("package")
                ) {
                    true
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.app_save_name_toast),
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}