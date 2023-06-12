package domilopment.apkextractor.ui.fragments

import android.content.ClipData
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import domilopment.apkextractor.*
import domilopment.apkextractor.ui.appList.AppListAdapter
import domilopment.apkextractor.ui.appList.AppListTouchHelperCallback
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.databinding.FragmentAppListBinding
import domilopment.apkextractor.ui.appList.AppListMultiselectCallback
import domilopment.apkextractor.ui.dialogs.AppFilterBottomSheet
import domilopment.apkextractor.ui.dialogs.ProgressDialogFragment
import domilopment.apkextractor.ui.viewModels.MainViewModel
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppListFragment : Fragment() {
    private var _binding: FragmentAppListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewAdapter: AppListAdapter
    private lateinit var swipeHelper: ItemTouchHelper
    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var callback: OnBackPressedCallback
    private val model by activityViewModels<MainViewModel>()
    private val progressDialogViewModel by activityViewModels<ProgressDialogViewModel>()

    private val shareApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireContext().cacheDir.deleteRecursively()
        }

    private var appToUninstall: ApplicationModel? = null
    private val uninstallApp =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            appToUninstall?.also {
                val isAppUninstalled =
                    !Utils.isPackageInstalled(requireContext().packageManager, it.appPackageName)
                if (isAppUninstalled) {
                    model.removeApp(it)
                } else if (Utils.isSystemApp(it) && it.appInstallTime == it.appUpdateTime) {
                    model.moveFromUpdatedToSystemApps(it)
                }
            }
            appToUninstall = null
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
                model.mainFragmentState.collect { uiState ->
                    binding.refresh.isRefreshing = uiState.isRefreshing
                    val recyclerView = binding.listView.list
                    if (!recyclerView.isComputingLayout && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) viewAdapter.updateData(
                        uiState.appList, uiState.updateTrigger.handleTrigger()
                    )
                    else recyclerView.post {
                        viewAdapter.updateData(
                            uiState.appList, uiState.updateTrigger.handleTrigger()
                        )
                    }
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
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
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
                    .setDeleteResult(uninstallApp).build()
            )
            viewAdapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        })
        swipeHelper.attachToRecyclerView(binding.listView.list)

        binding.listView.list.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        progressDialogViewModel.extractionResult.observe(viewLifecycleOwner) { result ->
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

                progressDialogViewModel.resetProgress()
            }
        }

        /**
         * Creates Intent for Apps to Share
         */
        progressDialogViewModel.shareResult.observe(viewLifecycleOwner) { result ->
            result?.getContentIfNotHandled()?.let { files ->
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = FileUtil.MIME_TYPE
                    clipData = ClipData.newRawUri(null, files[0]).apply {
                        files.drop(1).forEach { addItem(ClipData.Item(it)) }
                    }
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }.let {
                    Intent.createChooser(it, getString(R.string.share_intent_title))
                }?.also {
                    shareApp.launch(it)
                }

                progressDialogViewModel.resetProgress()
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
                progressDialogViewModel.saveApps(list)
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
                progressDialogViewModel.createShareUrisForApps(it)
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
                // Associate searchable configuration with the SearchView
                searchView = (menu.findItem(R.id.action_search_app_list).actionView as SearchView).apply {
                    maxWidth = Int.MAX_VALUE
                    imeOptions = EditorInfo.IME_ACTION_SEARCH

                    // listening to search query text change
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        var queryTextChangedJob: Job? = null

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            // filter recycler view when query submitted
                            queryTextChangedJob?.cancel()
                            onFilter(query)
                            return false
                        }

                        override fun onQueryTextChange(query: String?): Boolean {
                            // filter recycler view when text is changed
                            queryTextChangedJob?.cancel()
                            queryTextChangedJob = lifecycleScope.launch(Dispatchers.Main) {
                                delay(300)
                                onFilter(query)
                            }
                            return false
                        }

                        fun onFilter(query: String?) {
                            model.searchQuery(query)
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
                            if (it.isNotBlank() && searchView.query.isBlank()) searchView.setQuery(
                                it, false
                            )
                            if (!AppListMultiselectCallback.isActionModeActive() && it.isNotBlank()) searchView.isIconified =
                                false
                        }
                    }
                }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                when (menuItem.itemId) {
                    R.id.action_settings -> requireActivity().findNavController(R.id.fragment)
                        .navigate(R.id.action_mainFragment_to_settingsFragment)

                    R.id.action_filter -> AppFilterBottomSheet.newInstance().apply {
                        show(this@AppListFragment.parentFragmentManager, AppFilterBottomSheet.TAG)
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
     * Set Enabled State of refresh
     * @param enabled
     * boolean for enabled state
     */
    fun enableRefresh(enabled: Boolean) {
        binding.refresh.isEnabled = enabled
    }

    /**
     * Set State of Multiselect Bottom Sheet
     * @param show
     * State to apply
     */
    fun showBottomSheet(show: Boolean) {
        lifecycleScope.launch {
            if (::searchView.isInitialized && searchView.hasFocus()) {
                searchView.clearFocus()
                delay(100)
            }
            BottomSheetBehavior.from(binding.appMultiselectBottomSheet.root).state = if (show) {
                val paddingBottom =
                    requireContext().obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeight))
                binding.listView.list.setPadding(
                    0, 0, 0, paddingBottom.getDimensionPixelOffset(0, 0)
                )
                paddingBottom.recycle()
                BottomSheetBehavior.STATE_EXPANDED
            } else {
                binding.listView.list.setPadding(0, 0, 0, 0)
                BottomSheetBehavior.STATE_COLLAPSED
            }
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
}