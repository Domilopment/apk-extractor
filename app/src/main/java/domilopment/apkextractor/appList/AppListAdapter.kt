package domilopment.apkextractor.appList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.AppOptionsBottomSheet
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.databinding.AppListItemBinding
import domilopment.apkextractor.fragments.MainFragment
import java.util.*

class AppListAdapter(
    private val mainFragment: MainFragment
) :
    RecyclerView.Adapter<AppListAdapter.MyViewHolder>(),
    Filterable {
    // Static Dataset for Smoother transition
    private var myDataset = listOf<ApplicationModel>()

    // Shown Data in ListView
    var myDatasetFiltered: MutableList<ApplicationModel> = myDataset.toMutableList()
        private set

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        val binding: AppListItemBinding = AppListItemBinding.bind(myView)
    }

    val actionModeCallback = AppListMultiselectCallback(mainFragment, this)

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
            firstLine.text =
                mainFragment.getString(R.string.holder_app_name, app.appName, app.apkSize)
            secondLine.text = app.appPackageName
            icon.setImageDrawable(app.appIcon)
            checkBox.isVisible = app.isChecked
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
                    checkBox.isVisible = app.isChecked
                }
            }
            // ItemView on Long Click
            root.setOnLongClickListener {
                if (!actionModeCallback.isActionModeActive()) {
                    checkBox.isVisible = true
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
                val charString = charSequence.toString().lowercase()
                val myDatasetFiltered = if (charString.isBlank()) {
                    myDataset
                } else {
                    myDataset.filter {
                        it.appName.lowercase().contains(charString)
                                || it.appPackageName.contains(charSequence)
                    }
                }
                return FilterResults().apply {
                    values = myDatasetFiltered
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
                myDatasetFiltered = (filterResults.values as List<ApplicationModel>).toMutableList()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Updates data with delivered Dataset
     * @param apps Updated set of Applications
     */
    fun updateData(apps: List<ApplicationModel>) {
        if (apps == myDataset) return
        myDataset = apps
        myDatasetFiltered = myDataset.toMutableList()
        notifyDataSetChanged()
    }
}
