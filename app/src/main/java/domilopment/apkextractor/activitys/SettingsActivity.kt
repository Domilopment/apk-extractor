package domilopment.apkextractor.activitys

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.FileHelper
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
            findPreference<Preference>("version")!!.title = activity!!.getString(R.string.version).format(BuildConfig.VERSION_NAME)
            findPreference<Preference>("github")!!.setOnPreferenceClickListener {
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(context!!, Uri.parse("https://github.com/domilopment/apkextractor"))
                return@setOnPreferenceClickListener true
            }
            findPreference<Preference>("googleplay")!!.setOnPreferenceClickListener {
                Intent(Intent.ACTION_VIEW).apply {
                    data = try {
                        setPackage(
                            context!!.packageManager.getPackageInfo("com.android.vending", 0).packageName
                        )
                        Uri.parse("market://details?id=${context!!.packageName}")
                    } catch (e: PackageManager.NameNotFoundException) {
                        Uri.parse("https://play.google.com/store/apps/details?id=${context!!.packageName}")
                    }
                }.also {
                    startActivity(it)
                }
                return@setOnPreferenceClickListener true
            }
            findPreference<Preference>("choose_dir")!!.setOnPreferenceClickListener {
                FileHelper(activity!!).chooseDir()
                return@setOnPreferenceClickListener true
            }
        }
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FileHelper.CHOOSE_SAVE_DIR_RESULT -> {
                getDefaultSharedPreferences(this).edit()
                    .putString("dir", data!!.data.toString()).apply()
                (data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
                    contentResolver
                        .takePersistableUriPermission(data.data!!, this)
                }
            }
        }
    }
}
