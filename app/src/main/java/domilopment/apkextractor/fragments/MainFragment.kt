package domilopment.apkextractor.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.databinding.FragmentMainBinding
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewAdapter: AppListAdapter
    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var callback: OnBackPressedCallback
    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireContext()).defaultViewModelProviderFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback {
            // close search view on back button pressed
            if (!searchView.isIconified)
                searchView.isIconified = true

            isEnabled = !searchView.isIconified
        }.also {
            it.isEnabled = false
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // add Refresh Layout action on Swipe
        binding.refresh.setOnRefreshListener {
            searchView.isIconified = true
            updateData()
        }

        model.getApps().observe(viewLifecycleOwner, { apps ->
            viewAdapter.updateData(apps)
            binding.refresh.isRefreshing = false
        })

        binding.appMultiselectBottomSheet.apply {
            actionSaveApk.setOnClickListener {
                saveApps(it)
            }

            actionShare.setOnClickListener {
                getSelectedApps()?.let {
                    Intent.createChooser(it, getString(R.string.action_share))
                }?.also {
                    startActivityForResult(it, MainActivity.SHARE_APP_RESULT)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        viewAdapter = AppListAdapter(this)

        binding.listView.list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(requireContext())
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        viewAdapter.finish()
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
        updateData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * Function Called on FloatingActionButton Click
     * If backup was successful, print name of last app backup and number of additional
     * else break on first unsuccessful backup and print its name
     * @param view
     * The FloatingActionButton
     */
    private fun saveApps(view: View) {
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.also { list ->
            if (list.isEmpty()) Toast.makeText(
                requireContext(),
                R.string.toast_save_apps,
                Toast.LENGTH_SHORT
            ).show()
            else if (
                kotlin.run success@{
                    val settingsManager = SettingsManager(requireContext())
                    val fileHelper = FileHelper(requireContext())
                    list.forEach { app ->
                        if (
                            !fileHelper.copy(
                                app.appSourceDirectory,
                                settingsManager.saveDir()!!,
                                settingsManager.appName(app)
                            )
                        ) {
                            Snackbar.make(
                                view,
                                getString(R.string.snackbar_extraction_failed)
                                    .format(app.appPackageName),
                                Snackbar.LENGTH_LONG
                            ).setAnchorView(binding.appMultiselectBottomSheet.root)
                                .setTextColor(Color.RED)
                                .show()
                            return@success false
                        }
                    }
                    return@success true
                }
            ) {
                Snackbar.make(
                    view,
                    if (list.size == 1) getString(R.string.snackbar_successful_extracted)
                        .format(list.last().appName)
                    else resources.getQuantityString(R.plurals.snackbar_successful_extracted_multiple, list.size, list.last().appName, list.size)
                    .format(list.last().appName, list.size - 1),
                    Snackbar.LENGTH_LONG
                ).setAnchorView(binding.appMultiselectBottomSheet.root)
                    .show()
            }
        }
    }

    /**
     * Update Dataset
     */
    private fun updateData() {
        binding.refresh.isRefreshing = true
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
        when (sharedPreferences.getInt("app_sort", 0)) {
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

            // Enable on return Callback if user Opens SearchView
            setOnSearchClickListener {
                callback.isEnabled = true
            }

            // Disable on return Callback if user closes SearchView
            setOnCloseListener {
                callback.isEnabled = false
                return@setOnCloseListener false
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
        val fileHelper = FileHelper(requireContext())
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.forEach { app ->
            fileHelper.shareURI(app).also {
                files.add(it)
            }
        }
        return if (files.isEmpty()) {
            Toast.makeText(requireContext(), R.string.toast_share_app, Toast.LENGTH_SHORT).show()
            null
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = FileHelper.MIME_TYPE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            }
        }
    }

    /**
     * Set Preferencies for App List Sort Type and Apply
     * @param item Menu item for sortType
     * @param sortType Internal sort type number
     */
    private fun sortData(item: MenuItem, sortType: Int) {
        sharedPreferences.edit().putInt("app_sort", sortType).apply()
        item.isChecked = true
        viewAdapter.sortData()
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
            R.id.action_app_name -> sortData(item, 0)
            R.id.action_package_name -> sortData(item, 1)
            R.id.action_install_time -> sortData(item, 2)
            R.id.action_update_time -> sortData(item, 3)
            R.id.action_show_save_dir -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    val destDir = SettingsManager(requireContext()).saveDir()
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, destDir)
                    setDataAndType(destDir, FileHelper.MIME_TYPE)
                }
                activity?.startActivityForResult(intent, MainActivity.SELECTED_APK_RESULT)
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Set Enabled State of refresh
     * @param enabled
     * boolean for enabled state
     */
    fun enableRefresh(enabled: Boolean) {
        binding.refresh.isEnabled = enabled
    }

    /**
     * Set State of Multiselect Bottom Sheet
     * @param state
     * State to apply
     */
    fun stateBottomSheetBehaviour(state: Int) {
        BottomSheetBehavior.from(binding.appMultiselectBottomSheet.root).state = state
    }
}