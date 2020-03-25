package domilopment.apkextractor.activitys

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
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
            findPreference<Preference>("choose_dir")!!.setOnPreferenceClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999)
                return@setOnPreferenceClickListener true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                9999 -> {
                    getDefaultSharedPreferences(context).edit()
                        .putString("dir", data!!.data.toString()).apply()
                    val takeFlags =
                        data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity!!.contentResolver
                        .takePersistableUriPermission(data.data!!, takeFlags)
                }
            }
        }
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
    }
}
