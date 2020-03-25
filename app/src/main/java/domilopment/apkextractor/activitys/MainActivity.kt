package domilopment.apkextractor.activitys

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AppListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var myData: List<Application>
    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        myData = SettingsManager(this).selectedAppTypes()

        viewManager = LinearLayoutManager(this)
        viewAdapter = AppListAdapter(myData)

        recyclerView = findViewById<RecyclerView>(R.id.list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains("dir")) {
            val dlgAlert = AlertDialog.Builder(this)
            dlgAlert.setMessage("Choose a Directory to Save APKs")
            dlgAlert.setTitle("Save Dir")
            dlgAlert.setCancelable(false)
            dlgAlert.setPositiveButton("Ok") { dialog, which ->
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999)
            }
            dlgAlert.create().show()
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
                        Snackbar.make(view, "APK ${d.appName} extracted to $path", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    else
                        Snackbar.make(view, "Extraction of ${d.appName} Failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // filter recycler view when query submitted
                viewAdapter.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(query: String?): Boolean {
                // filter recycler view when text is changed
                viewAdapter.filter.filter(query)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
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
            9999 -> {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("dir", data!!.data.toString()).apply()
                val takeFlags =
                    data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                this.contentResolver
                    .takePersistableUriPermission(data.data!!, takeFlags)
            }
        }
    }
}
