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
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuItemCompat
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
    private lateinit var myData: List<Application>
    private lateinit var path: String
    private lateinit var mShareActionProvider: ShareActionProvider
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (checkNeededPermissions())
            startApplication()

        path = SettingsManager(this).saveDir()

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
                it.setMessage("Choose a Directory to Save APKs")
                it.setTitle("Save Dir")
                it.setCancelable(false)
                it.setPositiveButton("Ok") { _, _ ->
                    FileHelper(this).chooseDir()
                }
            }.create().show()
        }
    }

    private fun startApplication() {
        myData = SettingsManager(this).selectedAppTypes()

        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(myData, this)

        recyclerView = findViewById<RecyclerView>(R.id.list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        fab.setOnClickListener { view ->
            path = SettingsManager(this).saveDir()
            for (d in viewAdapter.myDatasetFiltered)
                if(d.isChecked)
                    if (FileHelper(this).copy(
                            d.appSourceDirectory,
                            path,
                            "${d.appName}_${d.appVersionName}.apk"
                        )
                    )
                        Snackbar.make(view, "APK ${d.appName} extracted", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    else
                        Snackbar.make(view, "Extraction of ${d.appName} FAILED", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setTextColor(Color.RED).show()
        }
    }

    private fun checkNeededPermissions() : Boolean{
        val neededPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val missingPermissions = ArrayList<String>()
        for (neededPermission in neededPermissions) {
            if (ActivityCompat.checkSelfPermission(applicationContext, neededPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(neededPermission)
            }
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 0)
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
        val shareItem = menu.findItem(R.id.action_share)

        // Now get the ShareActionProvider from the item
        mShareActionProvider =
            MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

        return true
    }

    fun updateIntent() {
        mShareActionProvider.setShareIntent(getSelectedApps())
    }

    private fun getSelectedApps(): Intent? {
        val files = ArrayList<Uri>()
        for (app in viewAdapter.myDatasetFiltered) {
            if (app.isChecked) {
                val uri = FileProvider.getUriForFile(
                    this,
                    application.packageName+".provider",
                    File(app.appSourceDirectory))
                files.add(uri)
            }
        }
        return if (files.isEmpty())
            null
        else {
            Intent(Intent.ACTION_SEND).apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = FileHelper.MIME_TYPE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
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
