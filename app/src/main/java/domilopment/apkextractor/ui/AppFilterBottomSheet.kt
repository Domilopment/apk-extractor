package domilopment.apkextractor.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.AppFilterBottomSheetBinding
import domilopment.apkextractor.ui.fragments.MainViewModel
import domilopment.apkextractor.utils.AppFilterOptions
import domilopment.apkextractor.utils.Utils

class AppFilterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: AppFilterBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireActivity().application).defaultViewModelProviderFactory
    }

    companion object {
        const val TAG = "app_filter_bottom_sheet"

        @JvmStatic
        fun newInstance(): AppFilterBottomSheet {
            return AppFilterBottomSheet()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = AppFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BottomSheetBehavior.from(view.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            peekHeight = 0
        }

        binding.updatedSystemApps.isChecked =
            sharedPreferences.getBoolean("updated_system_apps", false)

        binding.systemApps.apply {
            isEnabled = sharedPreferences.getBoolean("updated_system_apps", false)
            isChecked = sharedPreferences.getBoolean("system_apps", false)
        }

        binding.userApps.isChecked = sharedPreferences.getBoolean("user_apps", true)

        binding.appType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // Respond to button selection
            val preferenceKey: String = when (checkedId) {
                R.id.updated_system_apps -> {
                    binding.systemApps.isEnabled = isChecked
                    "updated_system_apps"
                }
                R.id.system_apps -> "system_apps"
                R.id.user_apps -> "user_apps"
                else -> return@addOnButtonCheckedListener
            }
            sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply()
            model.changeSelection(preferenceKey, isChecked)
        }

        binding.sortOrder.apply {
            isChecked = sharedPreferences.getBoolean("app_sort_asc", true)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean("app_sort_asc", isChecked).apply()
                model.sortApps()
            }
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.sort_apps, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.sort.adapter = adapter
        }
        binding.sort.setSelection(sharedPreferences.getInt("app_sort", 0))
        binding.sort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                sharedPreferences.edit().putInt("app_sort", position).apply()
                model.sortApps()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                parent?.setSelection(sharedPreferences.getInt("app_sort", 0))
            }

        }

        binding.sortFavorites.apply {
            isChecked = sharedPreferences.getBoolean("sort_favorites", true)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean("sort_favorites", isChecked).apply()
                model.sortFavorites()
            }
        }

        binding.filterFavorites.apply {
            setupFilterChip(this, AppFilterOptions.FAVORITES)
        }

        binding.filterPlayStore.apply {
            setupStoreFilterChip(this, "com.android.vending", AppFilterOptions.GOOGLE)
        }

        binding.filterGalaxyStore.apply {
            setupStoreFilterChip(this, "com.sec.android.app.samsungapps", AppFilterOptions.SAMSUNG)
        }

        binding.filterAmazonStore.apply {
            setupStoreFilterChip(this, "com.amazon.venezia", AppFilterOptions.AMAZON)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Setup filter Chip
     * @param chip chip to setup
     * @param filterOptions filter option integer for Chip
     */
    private fun setupFilterChip(chip: Chip, filterOptions: AppFilterOptions) {
        chip.isChecked = sharedPreferences.getInt(
            "filter", 0
        ) and filterOptions.getByte() == filterOptions.getByte()
        chip.setOnCheckedChangeListener { _, isChecked ->
            val filter = sharedPreferences.getInt("filter", 0)
            val value =
                if (isChecked) filter or filterOptions.getByte() else filter and filterOptions.getByte()
                    .inv()
            sharedPreferences.edit().putInt("filter", value).apply()
            model.filterApps()
        }
    }

    /**
     * Setup filter Chip, for Store filter options
     * @param chip chip to setup
     * @param packageName Package name of store
     * @param filterOptions filter option integer for Store
     */
    private fun setupStoreFilterChip(
        chip: Chip, packageName: String, filterOptions: AppFilterOptions
    ) {
        packageName.runCatching {
            Utils.getPackageInfo(requireContext().packageManager, this)
        }.onSuccess {
            chip.isVisible = true
            chip.text =
                requireContext().packageManager.getApplicationLabel(it.applicationInfo).toString()
            setupFilterChip(chip, filterOptions)
        }
    }
}