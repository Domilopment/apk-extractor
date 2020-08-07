package domilopment.apkextractor.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.R
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.app_list.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File

class MainFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var viewAdapter: AppListAdapter
    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var callback: OnBackPressedCallback
    private val model by activityViewModels<MainViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(mainActivity) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback {
            // close search view on back button pressed
            if (!searchView.isIconified) {
                searchView.isIconified = true
            }
            isEnabled = !searchView.isIconified
        }.also {
            it.isEnabled = false
        }

        mainActivity = (requireActivity() as MainActivity)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // add Refresh Layout action on Swipe
        refresh.setOnRefreshListener {
            searchView.isIconified = true
            updateData()
        }

        fab.setOnClickListener { saveApps(it) }

        model.getApps().observe(viewLifecycleOwner, Observer<List<Application>>{ apps ->
            viewAdapter.updateData(apps)
            refresh.isRefreshing = false
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        viewAdapter = AppListAdapter(mainActivity)

        list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(mainActivity)
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        mainActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
        updateData()
    }

    /**
     * Function Called on FloatingActionButton Click
     * @param view
     * The FloatingActionButton
     */
    private fun saveApps(view: View) {
        val path = SettingsManager(requireContext()).saveDir()
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.also {
            if (it.isEmpty())
                Toast.makeText(requireContext(), R.string.toast_save_apps, Toast.LENGTH_SHORT).show()
            else
                it.forEach { d ->
                    if (FileHelper(requireActivity()).copy(
                            d.appSourceDirectory,
                            path,
                            SettingsManager(requireContext()).appName(d)
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

    /**
     * Update Dataset
     */
    private fun updateData() {
        refresh.isRefreshing = true
        model.updateApps()
    }

    /**
     * Creates Options Menu
     * @param menu
     * @return Boolean
     * True after Menu is Created
     */
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_main, menu)

        val byName: MenuItem = menu.findItem(R.id.action_app_name)
        val byPackage: MenuItem = menu.findItem(R.id.action_package_name)
        val byInstall: MenuItem = menu.findItem(R.id.action_install_time)
        val byUpdate: MenuItem = menu.findItem(R.id.action_update_time)
        when (sharedPreferences.getInt("app_sort", 0)){
            0 -> byName.isChecked = true
            1 -> byPackage.isChecked = true
            2 -> byInstall.isChecked = true
            3 -> byUpdate.isChecked = true
            else -> throw Exception("No such sort type")
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
            setOnSearchClickListener {
                callback.isEnabled = true
            }
        }

        // Retrieve the share menu item
        menu.findItem(R.id.action_share).apply {
            setOnMenuItemClickListener {
                getSelectedApps()?.let {
                    startActivityForResult(
                        Intent.createChooser(it, getString(R.string.action_share)),
                        MainActivity.SHARE_APP_RESULT
                    )
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    /**
     * Creates Intent for Apps to Share
     * @return Intent?
     * Returns Intent with selected app APKs
     * if no Apps Selected null
     */
    private fun getSelectedApps(): Intent? {
        val files = ArrayList<Uri>()
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.forEach { app ->
            FileProvider.getUriForFile(
                requireContext(),
                requireActivity().application.packageName+".provider",
                File(app.appSourceDirectory).copyTo(File(requireActivity().cacheDir, SettingsManager(requireContext()).appName(app)), true)
            ).also {
                files.add(it)
            }
        }
        return if (files.isEmpty()) {
            Toast.makeText(requireContext(), R.string.toast_share_app, Toast.LENGTH_SHORT).show()
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

    /**
     * Perform actions for menu Items
     * @param item selected menu Item
     * return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings ->
                findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            R.id.action_app_name -> {
                sharedPreferences.edit().putInt("app_sort", 0)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
            }
            R.id.action_package_name -> {
                sharedPreferences.edit().putInt("app_sort", 1)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
            }
            R.id.action_install_time -> {
                sharedPreferences.edit().putInt("app_sort", 2)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
            }
            R.id.action_update_time -> {
                sharedPreferences.edit().putInt("app_sort", 3)
                    .apply()
                item.isChecked = true
                viewAdapter.sortData()
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }
}