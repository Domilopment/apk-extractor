package domilopment.apkextractor.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.appList.AppListAdapter
import domilopment.apkextractor.appList.AppListTouchHelperCallback
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.databinding.FragmentMainBinding
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import domilopment.apkextractor.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewAdapter: AppListAdapter
    private lateinit var swipeHelper: ItemTouchHelper
    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var callback: OnBackPressedCallback
    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireActivity().application).defaultViewModelProviderFactory
    }

    private val shareApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireContext().cacheDir.deleteRecursively()
        }

    var appToUninstall: ApplicationModel? = null
    private val uninstallApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            appToUninstall?.also {
                if (!Utils.isPackageInstalled(requireContext().packageManager, it.appPackageName)) {
                    model.removeApp(it)
                }
            }
            appToUninstall = null
        }

    private val selectApk =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) it.data?.data?.let { apkUri ->
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(R.string.alert_apk_selected_title))
                    setItems(R.array.selected_apk_options) { _, i: Int ->
                        try {
                            apkFileOptions(i, apkUri)
                        } catch (e: Exception) {
                            Log.e("Apk Extractor: Saved Apps Dialog", e.toString())
                        }
                    }
                }.show()
            } ?: Toast.makeText(
                context, getString(R.string.alert_apk_selected_failed), Toast.LENGTH_LONG
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback {
            // close search view on back button pressed
            if (!searchView.isIconified) searchView.isIconified = true
            isEnabled = !searchView.isIconified
        }.also {
            it.isEnabled = false
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.mainFragmantState.collect { uiState ->
                    binding.refresh.isRefreshing = uiState.isRefreshing
                    viewAdapter.updateData(uiState.appList)
                    if (::searchView.isInitialized) with(searchView.query) {
                        if (isNotBlank()) viewAdapter.filter.filter(this)
                    }
                    if (uiState.actionMode) (requireActivity() as AppCompatActivity).startSupportActionMode(
                        viewAdapter.actionModeCallback
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        viewAdapter = AppListAdapter(this)

        swipeHelper = ItemTouchHelper(AppListTouchHelperCallback(
            this
        ) { viewHolder: RecyclerView.ViewHolder, apkAction: ApkActionsOptions ->
            val app = viewAdapter.myDatasetFiltered[viewHolder.bindingAdapterPosition]
            appToUninstall = app
            apkAction.getAction(
                requireContext(),
                app,
                ApkActionsOptions.ApkActionOptionParams.Builder()
                    .setViews(view, binding.appMultiselectBottomSheet.root).setShareResult(shareApp)
                    .setDeleteResult(uninstallApp)
                    .build()
            )
            viewAdapter.notifyDataSetChanged()
        })
        swipeHelper.attachToRecyclerView(binding.listView.list)

        binding.listView.list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = LinearLayoutManager(requireContext())
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        model.getExtractionResult().observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { (failed, app, size) ->
                if (failed == true) Snackbar.make(
                    view, getString(
                        R.string.snackbar_extraction_failed, app?.appPackageName
                    ), Snackbar.LENGTH_LONG
                ).setAnchorView(binding.appMultiselectBottomSheet.root).setTextColor(Color.RED)
                    .show()
                else Snackbar.make(
                    view, resources.getQuantityString(
                        R.plurals.snackbar_successful_extracted_multiple,
                        size,
                        app?.appName,
                        size - 1
                    ), Snackbar.LENGTH_LONG
                ).setAnchorView(binding.appMultiselectBottomSheet.root).show()

                model.resetProgress()
            }
        }

        /**
         * Creates Intent for Apps to Share
         */
        model.getShareResult().observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { files ->
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = FileHelper.MIME_TYPE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                }.let {
                    Intent.createChooser(it, getString(R.string.share_intent_title))
                }?.also {
                    shareApp.launch(it)
                }

                model.resetProgress()
            }
        }

        // add Refresh Layout action on Swipe
        binding.refresh.setOnRefreshListener {
            model.updateApps()
        }

        binding.appMultiselectBottomSheet.apply {
            actionSaveApk.setOnClickListener {
                saveApps()
            }

            actionShare.setOnClickListener {
                shareApps()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Function Called on BottomSheet Click
     * If backup was successful, print name of last app backup and number of additional
     * else break on first unsuccessful backup and print its name
     */
    private fun saveApps() {
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.also { list ->
            if (list.isEmpty()) Toast.makeText(
                requireContext(), R.string.toast_save_apps, Toast.LENGTH_SHORT
            ).show()
            else {
                val progressDialog =
                    ProgressDialogFragment.newInstance(R.string.progress_dialog_title_save)
                progressDialog.show(parentFragmentManager, "ProgressDialogFragment")
                model.saveApps(list)
            }
        }
    }

    /**
     * Function Called on BottomSheet Click
     * If share Uri generation was successful, show Intent share Dialog
     */
    private fun shareApps() {
        viewAdapter.myDatasetFiltered.filter {
            it.isChecked
        }.also {
            if (it.isEmpty()) Toast.makeText(
                requireContext(), R.string.toast_share_app, Toast.LENGTH_SHORT
            ).show()
            else {
                val progressDialog =
                    ProgressDialogFragment.newInstance(R.string.progress_dialog_title_share)
                progressDialog.show(parentFragmentManager, "ProgressDialogFragment")
                model.createShareUrisForApps(it)
            }
        }
    }

    /**
     * Remove or attach RecyclerView to SwipeHelper
     */
    fun attachSwipeHelper(attach: Boolean) =
        swipeHelper.attachToRecyclerView(if (attach) binding.listView.list else null)

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                val byName: MenuItem = menu.findItem(R.id.action_app_name)
                val byPackage: MenuItem = menu.findItem(R.id.action_package_name)
                val byInstall: MenuItem = menu.findItem(R.id.action_install_time)
                val byUpdate: MenuItem = menu.findItem(R.id.action_update_time)
                val byApkSize: MenuItem = menu.findItem(R.id.action_apk_size)
                when (sharedPreferences.getInt("app_sort", 0)) {
                    0 -> byName.isChecked = true
                    1 -> byPackage.isChecked = true
                    2 -> byInstall.isChecked = true
                    3 -> byUpdate.isChecked = true
                    5 -> byApkSize.isChecked = true
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
                            model.searchQuery(query)
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
                }.also { searchView ->
                    model.searchQuery.observe(viewLifecycleOwner) {
                        it?.also {
                            viewAdapter.filter.filter(it)
                            searchView.setQuery(it, false)
                            if (!viewAdapter.actionModeCallback.isActionModeActive() && it.isNotBlank()) searchView.isIconified =
                                false
                        }
                    }
                }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                when (menuItem.itemId) {
                    R.id.action_settings -> findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
                    R.id.action_app_name -> sortData(menuItem, SettingsManager.SORT_BY_NAME)
                    R.id.action_package_name -> sortData(menuItem, SettingsManager.SORT_BY_PACKAGE)
                    R.id.action_install_time -> sortData(
                        menuItem, SettingsManager.SORT_BY_INSTALL_TIME
                    )
                    R.id.action_update_time -> sortData(
                        menuItem, SettingsManager.SORT_BY_UPDATE_TIME
                    )
                    R.id.action_apk_size -> sortData(menuItem, SettingsManager.SORT_BY_APK_SIZE)
                    R.id.action_show_save_dir -> {
                        val destDir = SettingsManager(requireContext()).saveDir().let {
                            DocumentsContract.buildDocumentUriUsingTree(
                                it, DocumentsContract.getTreeDocumentId(it)
                            )
                        }
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = FileHelper.MIME_TYPE
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, destDir)
                        }
                        selectApk.launch(intent)
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Show Search View Input after ActionMode has ended if a search query is present
     */
    fun showSearchView() {
        if (::searchView.isInitialized) searchView.isIconified = searchView.query.isBlank()
    }

    /**
     * Set Preference for App List Sort Type and Apply
     * @param item Menu item for sortType
     * @param sortType Internal sort type number
     */
    private fun sortData(item: MenuItem, sortType: Int) {
        sharedPreferences.edit().putInt("app_sort", sortType).apply()
        item.isChecked = true
        model.sortApps()
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
        lifecycleScope.launch {
            if (::searchView.isInitialized && searchView.hasFocus()) {
                searchView.clearFocus()
                delay(100)
            }
            BottomSheetBehavior.from(binding.appMultiselectBottomSheet.root).state = state
        }
    }

    /**
     * set if App multiselect action mode should be restored on start
     * @param actionMode Boolean if action mode callback should be enabled
     */
    fun startSupportActionMode(actionMode: Boolean) {
        model.addActionModeCallback(actionMode)
    }

    /**
     * Set ApplicationModel for AppOptionsBottomSheet flow
     * @param app Selected Application from App list
     */
    fun selectApplication(app: ApplicationModel) {
        model.selectApplication(app)
    }

    /**
     * Executes Option for "How to Use Apk" Dialog
     * @param i Selected Option
     * @param data Result Intent, holding Apk files data
     */
    private fun apkFileOptions(i: Int, data: Uri) {
        when (i) {
            // Send Selected Apk File
            0 -> startActivity(
                Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = FileHelper.MIME_TYPE
                    putExtra(Intent.EXTRA_STREAM, data)
                }, getString(R.string.share_intent_title))
            )
            // Install Selected Apk File
            /*
            1 -> startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        data,
                        FileHelper.MIME_TYPE
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            */
            // Delete Selected Apk File
            1 -> DocumentsContract.deleteDocument(
                requireContext().contentResolver, data
            ).let { deleted ->
                Toast.makeText(
                    context,
                    getString(if (deleted) R.string.apk_action_delete_success else R.string.apk_action_delete_failed),
                    Toast.LENGTH_SHORT
                )
            }.show()
        }
    }
}