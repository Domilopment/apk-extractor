package domilopment.apkextractor.ui.dialogs

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.AppFilterBottomSheetBinding
import domilopment.apkextractor.ui.viewModels.MainViewModel
import domilopment.apkextractor.utils.*
import domilopment.apkextractor.utils.appFilterOptions.AppFilter
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import domilopment.apkextractor.utils.appFilterOptions.AppFilterInstaller
import domilopment.apkextractor.utils.appFilterOptions.AppFilterOthers
import domilopment.apkextractor.utils.settings.AppSortOptions

class AppFilterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: AppFilterBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private val model by activityViewModels<MainViewModel>()

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

        binding.updatedSystemApps.apply {
            maxLines = 2
            isChecked =
                sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_UPDATED_SYSTEM_APPS, false)
        }

        binding.systemApps.apply {
            maxLines = 2
            isEnabled =
                sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_UPDATED_SYSTEM_APPS, false)
            isChecked = sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_SYSTEM_APPS, false)
        }

        binding.userApps.apply {
            maxLines = 2
            isChecked = sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_USER_APPS, true)
        }

        binding.appType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // Respond to button selection
            val preferenceKey: String = when (checkedId) {
                R.id.updated_system_apps -> {
                    binding.systemApps.isEnabled = isChecked
                    Constants.PREFERENCE_KEY_UPDATED_SYSTEM_APPS
                }

                R.id.system_apps -> Constants.PREFERENCE_KEY_SYSTEM_APPS
                R.id.user_apps -> Constants.PREFERENCE_KEY_USER_APPS
                else -> return@addOnButtonCheckedListener
            }
            sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply()
            model.changeSelection(preferenceKey, isChecked)
        }

        binding.sortOrder.apply {
            isChecked = sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_APP_SORT_ASC, true)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit()
                    .putBoolean(Constants.PREFERENCE_KEY_APP_SORT_ASC, isChecked).apply()
                model.sortApps()
            }
        }

        (binding.sort.getChildAt(
            sharedPreferences.getInt(
                Constants.PREFERENCE_KEY_APP_SORT, 0
            )
        ) as MaterialButton).isChecked = true

        binding.sort.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.sort_app_name -> sharedPreferences.edit()
                    .putInt(Constants.PREFERENCE_KEY_APP_SORT, AppSortOptions.SORT_BY_NAME.ordinal)
                    .apply()

                R.id.sort_app_package -> sharedPreferences.edit().putInt(
                        Constants.PREFERENCE_KEY_APP_SORT, AppSortOptions.SORT_BY_PACKAGE.ordinal
                    ).apply()

                R.id.sort_app_install -> sharedPreferences.edit().putInt(
                        Constants.PREFERENCE_KEY_APP_SORT,
                        AppSortOptions.SORT_BY_INSTALL_TIME.ordinal
                    ).apply()

                R.id.sort_app_update -> sharedPreferences.edit().putInt(
                        Constants.PREFERENCE_KEY_APP_SORT,
                        AppSortOptions.SORT_BY_UPDATE_TIME.ordinal
                    ).apply()

                R.id.sort_app_apk_size -> sharedPreferences.edit().putInt(
                        Constants.PREFERENCE_KEY_APP_SORT, AppSortOptions.SORT_BY_APK_SIZE.ordinal
                    ).apply()
            }
            model.sortApps()
        }

        binding.sortFavorites.apply {
            isChecked = sharedPreferences.getBoolean(Constants.PREFERENCE_KEY_SORT_FAVORITES, true)
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit()
                    .putBoolean(Constants.PREFERENCE_KEY_SORT_FAVORITES, isChecked).apply()
                model.sortFavorites()
            }
        }

        setupFilterMenuChip(
            binding.filterAppCategories,
            AppFilterCategories.values(),
            Constants.PREFERENCE_KEY_FILTER_CATEGORY,
            R.string.app_categories,
            R.string.filter_category_all
        )

        setupFilterMenuChip(
            binding.filterAppInstallationSources,
            AppFilterInstaller.values(),
            Constants.PREFERENCE_KEY_FILTER_INSTALLER,
            R.string.installation_sources,
            R.string.all_sources
        )

        setupFilterChip(
            binding.filterFavorites,
            AppFilterOthers.FAVORITES,
            Constants.PREFERENCE_KEY_FILTER_OTHERS
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Setup filter Chip
     * @param chip chip to setup
     * @param filterOptions filter option Enum for Chip
     * @param preferenceKey key of preference, selection is saved in
     */
    private fun setupFilterChip(chip: Chip, filterOptions: AppFilter, preferenceKey: String) {
        chip.text = filterOptions.getTitleString(requireContext())
        chip.isChecked =
            sharedPreferences.getStringSet(preferenceKey, setOf())?.contains(filterOptions.name)
                ?: false
        chip.setOnCheckedChangeListener { _, isChecked ->
            val filter = sharedPreferences.getStringSet(preferenceKey, setOf())?.toMutableSet()
            if (isChecked) filter?.add(filterOptions.name) else filter?.remove(filterOptions.name)
            sharedPreferences.edit().putStringSet(preferenceKey, filter).apply()
            model.filterApps()
        }
    }

    /**
     * Setup Menu filter Chip
     * @param chip chip to setup
     * @param filterOptions filter option enums for Chip
     * @param preferenceKey key of preference, selection is saved in
     * @param menuTitleRes title of menu if no (neutral) Item is selected
     * @param neutralMenuOptionTitleRes title of option, when no filter should be applied
     */
    private fun <T : AppFilter> setupFilterMenuChip(
        chip: Chip,
        filterOptions: Array<T>,
        preferenceKey: String,
        menuTitleRes: Int,
        neutralMenuOptionTitleRes: Int
    ) {
        val menu = PopupMenu(requireContext(), chip)
        menu.menu.add(Menu.NONE, -1, Menu.NONE, neutralMenuOptionTitleRes)
        filterOptions.forEach {
            val title = it.getTitleString(requireContext())
            if (title != null) {
                menu.menu.add(Menu.NONE, it.ordinal, Menu.NONE, title)
                if (sharedPreferences.getString(preferenceKey, null) == it.name) {
                    chip.isChecked = true
                    chip.text = title
                }
            }
        }
        menu.setOnMenuItemClickListener {
            if (it.itemId == -1) {
                chip.isChecked = false
                chip.text = getString(menuTitleRes)
                sharedPreferences.edit().remove(preferenceKey).apply()
            } else {
                val filter = filterOptions[it.itemId]
                chip.isChecked = true
                chip.text = filter.getTitleString(requireContext())
                sharedPreferences.edit().putString(preferenceKey, filter.name).apply()
            }
            model.filterApps()
            return@setOnMenuItemClickListener true
        }
        menu.setOnDismissListener {
            chip.closeIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.baseline_arrow_drop_down_24
            )
        }

        chip.setOnClickListener {
            chip.isChecked = !chip.isChecked
            chip.closeIcon = ContextCompat.getDrawable(
                requireContext(), R.drawable.baseline_arrow_drop_up_24
            )
            menu.show()
        }
    }
}