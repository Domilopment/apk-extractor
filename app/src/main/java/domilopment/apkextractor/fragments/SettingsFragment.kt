package domilopment.apkextractor.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import domilopment.apkextractor.*

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
        findPreference<Preference>("version")!!.title = getString(R.string.version).format(BuildConfig.VERSION_NAME)
        findPreference<Preference>("github")!!.setOnPreferenceClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(requireContext(), Uri.parse("https://github.com/domilopment/apkextractor"))
            return@setOnPreferenceClickListener true
        }
        findPreference<Preference>("googleplay")?.setOnPreferenceClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = try {
                    setPackage(
                        requireContext().packageManager.getPackageInfo("com.android.vending", 0).packageName
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
        findPreference<Preference>("choose_dir")?.setOnPreferenceClickListener {
            FileHelper(requireActivity()).chooseDir()
            return@setOnPreferenceClickListener true
        }
        findPreference<ListPreference>("list_preference_ui_mode")?.setOnPreferenceChangeListener { _, newValue ->
            SettingsManager(requireContext()).changeUIMode(newValue.toString())
            return@setOnPreferenceChangeListener true
        }
        findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            if (activity?.cacheDir!!.deleteRecursively())
                Toast.makeText(activity, getString(R.string.clear_cache_success), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(activity, getString(R.string.clear_cache_failed), Toast.LENGTH_SHORT).show()
            return@setOnPreferenceClickListener true
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).supportActionBar?.apply {
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