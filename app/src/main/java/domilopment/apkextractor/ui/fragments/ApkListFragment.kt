package domilopment.apkextractor.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.databinding.FragmentApkListBinding
import domilopment.apkextractor.ui.dialogs.ApkOptionsBottomSheet
import domilopment.apkextractor.ui.composables.apkList.ApkListContent
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.ApkSortOptions
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ApkListFragment : Fragment() {
    private var _binding: FragmentApkListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var searchView: SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var callback: OnBackPressedCallback
    private val model by activityViewModels<ApkListViewModel>()

    private val selectApk =
        registerForActivityResult(object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                val pickerInitialUri = SettingsManager(requireContext()).saveDir().let {
                    DocumentsContract.buildDocumentUriUsingTree(
                        it, DocumentsContract.getTreeDocumentId(it)
                    )
                }
                return super.createIntent(context, input).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
            }
        }) {
            it?.let { apkUri ->
                FileUtil(requireContext()).getDocumentInfo(
                    apkUri,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE
                )
            }?.let { documentFile ->
                PackageArchiveModel(
                    documentFile.uri,
                    documentFile.displayName!!,
                    documentFile.lastModified!!,
                    documentFile.size!!
                )
            }?.also { apk ->
                model.selectPackageArchive(apk)
                ApkOptionsBottomSheet.newInstance()
                    .show(requireActivity().supportFragmentManager, ApkOptionsBottomSheet.TAG)
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApkListBinding.inflate(inflater, container, false)
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val apkListUiState by model.apkListFragmentState.collectAsState()
                val searchString by model.searchQuery.collectAsState()

                APKExtractorTheme(
                    dynamicColor = sharedPreferences.getBoolean(
                        Constants.PREFERENCE_USE_MATERIAL_YOU, false
                    )
                ) {
                    ApkListContent(apkList = apkListUiState.appList,
                        searchString = searchString,
                        refreshing = apkListUiState.isRefreshing,
                        isPullToRefresh = true,
                        onRefresh = { model.updatePackageArchives() },
                        onClick = { app ->
                            model.selectPackageArchive(app)
                            requireActivity().supportFragmentManager.let {
                                ApkOptionsBottomSheet.newInstance().apply {
                                    show(it, ApkOptionsBottomSheet.TAG)
                                }
                            }
                        })
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Associate searchable configuration with the SearchView
                searchView =
                    (menu.findItem(R.id.action_search_apk_list).actionView as SearchView).apply {
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
                        lifecycleScope.launch {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                model.searchQuery.collect {
                                    it?.also {
                                        if (it.isNotBlank()) {
                                            if (searchView.query.isBlank()) searchView.setQuery(
                                                it, false
                                            )
                                            searchView.isIconified = false
                                        }
                                    }
                                }
                            }
                        }
                    }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_apk_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                when (menuItem.itemId) {
                    R.id.action_show_save_dir -> selectApk.launch(arrayOf(FileUtil.MIME_TYPE))
                    android.R.id.home -> requireActivity().onBackPressedDispatcher.onBackPressed()
                    R.id.action_sort_apk_file_name_asc -> sortItemSelected(ApkSortOptions.SORT_BY_FILE_NAME_ASC)
                    R.id.action_sort_apk_file_size_asc -> sortItemSelected(ApkSortOptions.SORT_BY_FILE_SIZE_ASC)
                    R.id.action_sort_apk_file_mod_date_asc -> sortItemSelected(ApkSortOptions.SORT_BY_LAST_MODIFIED_ASC)
                    R.id.action_sort_apk_file_name_desc -> sortItemSelected(ApkSortOptions.SORT_BY_FILE_NAME_DESC)
                    R.id.action_sort_apk_file_size_desc -> sortItemSelected(ApkSortOptions.SORT_BY_FILE_SIZE_DESC)
                    R.id.action_sort_apk_file_mod_date_desc -> sortItemSelected(ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC)
                    R.id.action_settings -> requireActivity().findNavController(R.id.fragment)
                        .navigate(R.id.action_mainFragment_to_settingsFragment)
                }
                return true
            }

            private fun sortItemSelected(sortMode: ApkSortOptions) {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                    .putString("apk_sort", sortMode.name).commit()
                model.sort(sortMode)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}