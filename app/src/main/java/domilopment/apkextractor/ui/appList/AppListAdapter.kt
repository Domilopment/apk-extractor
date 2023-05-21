package domilopment.apkextractor.ui.appList

import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import domilopment.apkextractor.ui.AppOptionsBottomSheet
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.databinding.AppListItemBinding
import domilopment.apkextractor.ui.fragments.MainFragment
import domilopment.apkextractor.utils.Utils
import java.util.*

class AppListAdapter(
    private val mainFragment: MainFragment
) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>(), Filterable {
    // Static Dataset for Smoother transition
    private var myDataset = listOf<ApplicationModel>()

    // Shown Data in ListView
    var myDatasetFiltered: MutableList<ApplicationModel> = myDataset.toMutableList()
        private set

    private var searchString = ""

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        val binding: AppListItemBinding = AppListItemBinding.bind(myView)
    }

    private val textColorHighlight = MaterialColors.getColor(
        mainFragment.requireContext(), android.R.attr.textColorHighlight, Color.CYAN
    )

    val actionModeCallback = AppListMultiselectCallback(mainFragment, this)

    init {
        setHasStableIds(true)
    }

    /**
     * Creates ViewHolder with Layout
     * @param parent
     * @param viewType
     * @return MyViewHolder
     * A ViewHolder with Layout app_list_item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false)
            .let {
                // set the view's size, margins, padding and layout parameters
                MyViewHolder(it)
            }
    }

    /**
     * Creates View Elements
     * @param holder
     * The Holder Item to be changed
     * @param position
     * Position of the holder Item in View
     * (also position for Item Data in Dataset)
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        val app = myDatasetFiltered[position]
        // Apply data from Dataset item to holder
        holder.binding.apply {
            try {
                firstLine.text = Utils.getSpannable(
                    mainFragment.getString(
                        R.string.holder_app_name, app.appName, app.apkSize
                    ), searchString, textColorHighlight
                )
                secondLine.text =
                    Utils.getSpannable(app.appPackageName, searchString, textColorHighlight)
                icon.setImageDrawable(app.appIcon)
            } catch (_: PackageManager.NameNotFoundException) {
                root.isVisible = false
                mainFragment.removeApplication(app)
            }
            setChecked(this, app.isChecked)
            favoriteStar.isVisible = app.isFavorite
            // ItemView on Click
            root.setOnClickListener {
                if (!actionModeCallback.isActionModeActive()) {
                    mainFragment.selectApplication(app)
                    mainFragment.requireActivity().supportFragmentManager.let {
                        AppOptionsBottomSheet.newInstance(app.appPackageName).apply {
                            show(it, AppOptionsBottomSheet.TAG)
                        }
                    }
                } else {
                    app.isChecked = !app.isChecked
                    actionModeCallback.setModeTitle()
                    setChecked(this, app.isChecked)
                }
            }
            // ItemView on Long Click
            root.setOnLongClickListener {
                if (!actionModeCallback.isActionModeActive()) {
                    setChecked(this, true)
                    app.isChecked = true
                    mainFragment.startSupportActionMode(true)
                    true
                } else false
            }
        }
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = myDatasetFiltered.size

    /**
     * Create unique item ids from file Uri hashcode
     * @param position position in recycler view
     */
    override fun getItemId(position: Int): Long {
        return myDatasetFiltered[position].appSourceDirectory.hashCode().toLong()
    }

    /**
     * Filter Apps with CharSequence (invoked by the SearchView)
     * @return Filter
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            /**
             * The Filter Algorithm
             * @param charSequence
             * charSequence to find
             * @return FilterResults
             * Apps that match charSequence
             */
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                searchString = charSequence.toString().trim()
                val myDatasetFiltered = if (searchString.isBlank()) {
                    myDataset
                } else {
                    myDataset.filter {
                        it.appName.contains(
                            searchString, ignoreCase = true
                        ) || it.appPackageName.contains(
                            searchString, ignoreCase = true
                        )
                    }
                }
                return FilterResults().apply {
                    values = myDatasetFiltered
                    count = myDatasetFiltered.size
                }
            }

            /**
             * Changes RecyclerView Content after Filtering
             * @param charSequence
             * charSequence to find
             * @param filterResults
             * Apps that match charSequence
             */
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                myDatasetFiltered =
                    (filterResults.values as List<ApplicationModel>).toMutableList()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Updates data with delivered Dataset
     * @param apps Updated set of Applications
     */
    fun updateData(apps: List<ApplicationModel>, updateTrigger: Boolean) {
        if (apps == myDataset && !updateTrigger) return
        myDataset = apps
        myDatasetFiltered = myDataset.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Set checked state of list item ViewHolder
     * @param binding ViewHolder layout binding
     * @param checked state to be set for ViewHolder
     */
    private fun setChecked(binding: AppListItemBinding, checked: Boolean) {
        binding.checkCircle.isVisible = checked
        binding.root.isChecked = checked
    }
}
