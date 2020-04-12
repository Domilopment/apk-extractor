package domilopment.apkextractor.activitys

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AppListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var path: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (checkNeededPermissions())
            startApplication()

        path = SettingsManager(this).saveDir()

        // Check if Save dir is Selected, Writing permission to dir and whether dir exists
        // if not ask for select dir
        if (!(sharedPreferences.contains("dir"))
            || checkUriPermission(
                Uri.parse(path),
                Binder.getCallingPid(),
                Binder.getCallingUid(),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            ) == PackageManager.PERMISSION_DENIED
            || !DocumentFile.fromTreeUri(
                this,
                Uri.parse(path)
            )!!.exists()
        ) {
            AlertDialog.Builder(this).let {
                it.setMessage(R.string.alert_save_path_message)
                it.setTitle(R.string.alert_save_path_title)
                it.setCancelable(false)
                it.setPositiveButton(R.string.alert_save_path_ok) { _, _ ->
                    FileHelper(this).chooseDir()
                }
            }.create().show()
        }
    }

    private fun startApplication() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(this)

        recyclerView = findViewById<RecyclerView>(R.id.list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    // Function Called on FloatingActionButton Click
    fun saveApps(view: View) {
        path = SettingsManager(this).saveDir()
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.also {
            if (it.isEmpty())
                Toast.makeText(this, R.string.toast_save_apps, Toast.LENGTH_SHORT).show()
            else
                it.forEach { d ->
                    if (FileHelper(this).copy(
                            d.appSourceDirectory,
                            path,
                            "${d.appName}_${d.appVersionName}.apk"
                        )
                    )
                        Snackbar.make(
                            view,
                            if (it.size == 1) {
                                getString(R.string.snackbar_successful_extracted).format(d.appName)
                            } else {
                                getString(R.string.snackbar_successful_extracted_multiple).format(d.appName, it.size)
                            },
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                    else
                        Snackbar.make(
                            view,
                            getString(R.string.snackbar_extraction_failed).format(d.appName),
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).setTextColor(Color.RED).show()
                }
        }
    }

    private fun checkNeededPermissions() : Boolean{
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).filter {
            ActivityCompat.checkSelfPermission(applicationContext, it) != PackageManager.PERMISSION_GRANTED
        }.also {
            if (it.isNotEmpty())
                ActivityCompat.requestPermissions(this, it.toTypedArray(), 0)
        }
        return true
    }

    private fun allPermissionsGranted(grantedPermissions: IntArray): Boolean {
        for (singleGrantedPermission in grantedPermissions)
            if (singleGrantedPermission == PackageManager.PERMISSION_DENIED)
                return false
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted(grantResults)) {
            startApplication()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val byName: MenuItem = menu.findItem(R.id.action_app_name)
        val byPackage: MenuItem = menu.findItem(R.id.action_package_name)
        val byInstall: MenuItem = menu.findItem(R.id.action_install_time)
        val byUpdate: MenuItem = menu.findItem(R.id.action_update_time)
        when (sharedPreferences.getInt("app_sort", 0)){
            1 -> byPackage.isChecked = true
            2 -> byInstall.isChecked = true
            3 -> byUpdate.isChecked = true
            else -> byName.isChecked = true
        }

        // Associate searchable configuration with the SearchView
        searchView = (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            maxWidth = Int.MAX_VALUE
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            // listening to search query text change
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // filter recycler view when query submitted
                    return onFilter(query)
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    // filter recycler view when text is changed
                    return onFilter(query)
                }
                fun onFilter(query: String?): Boolean {
                    viewAdapter.filter.filter(query)
                    return false
                }
            })
        }

        // Retrieve the share menu item
        menu.findItem(R.id.action_share).apply {
            setOnMenuItemClickListener {
                getSelectedApps()?.let {
                    startActivity(Intent.createChooser(it, getString(R.string.action_share)))
                }
                return@setOnMenuItemClickListener true
            }
        }
        return true
    }

    private fun getSelectedApps(): Intent? {
        val files = ArrayList<Uri>()
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.forEach { app ->
                FileProvider.getUriForFile(
                    this,
                    application.packageName+".provider",
                    File(app.appSourceDirectory)
                ).also {
                    files.add(it)
                }
        }
        return if (files.isEmpty()) {
            Toast.makeText(this, R.string.toast_share_app, Toast.LENGTH_SHORT).show()
            null
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = FileHelper.MIME_TYPE
                if (files.size > 1) {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                } else {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, files[0])
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_app_name -> {
                sharedPreferences.edit().putInt("app_sort", 0)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
                viewAdapter.notifyDataSetChanged()
            }
            R.id.action_package_name -> {
                sharedPreferences.edit().putInt("app_sort", 1)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
                viewAdapter.notifyDataSetChanged()
            }
            R.id.action_install_time -> {
                sharedPreferences.edit().putInt("app_sort", 2)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
                viewAdapter.notifyDataSetChanged()
            }
            R.id.action_update_time -> {
                sharedPreferences.edit().putInt("app_sort", 3)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
                viewAdapter.notifyDataSetChanged()
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified) {
            searchView.isIconified = true
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FileHelper.CHOOSE_SAVE_DIR_RESULT -> {
                sharedPreferences.edit()
                    .putString("dir", data!!.data.toString()).apply()
                (data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
                    contentResolver
                        .takePersistableUriPermission(data.data!!, this)
                }
            }
        }
    }
}
