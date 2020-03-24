package domilopment.apkextractor.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            findPreference<Preference>("version")!!.title = "Version: ${BuildConfig.VERSION_NAME}"
            findPreference<Preference>("github")!!.setOnPreferenceClickListener {
                val browse = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/domilopment/apkextractor"))
                startActivity(browse)
                return@setOnPreferenceClickListener true
            }
        }
    }
}
