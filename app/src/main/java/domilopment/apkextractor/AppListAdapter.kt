package domilopment.apkextractor

import android.view.*
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.databinding.AppListItemBinding
import domilopment.apkextractor.fragments.MainFragment
import java.util.*

class AppListAdapter(
    private val mainFragment: MainFragment
) :
    RecyclerView.Adapter<AppListAdapter.MyViewHolder>(),
    Filterable,
    ActionMode.Callback {
    private val settingsManager = SettingsManager(mainFragment.requireContext())

    // Static Dataset for Smoother transition
    private var myDataset = listOf<Application>()

    // Shown Data in ListView
    var myDatasetFiltered: MutableList<Application> = myDataset.toMutableList()
        private set

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        private val _binding: AppListItemBinding = AppListItemBinding.bind(myView)
        val binding: AppListItemBinding get() = _binding
    }

    private var multiselect = false
    private var myTitle = 0
    private var mode: ActionMode? = null

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
                // set the view's size, margins, paddings and layout parameters
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
        // say holder should not be re used for other Dataset menbers
        holder.setIsRecyclable(false)
        // Apply data from Dataset item to holder
        holder.binding.apply {
            firstLine.text =
                mainFragment.getString(R.string.holder_app_name).format(app.appName, app.apkSize)
            secondLine.text = app.appPackageName
            icon.setImageDrawable(app.appIcon)
            checkBox.isVisible = app.isChecked
            // ItemView on Click
            root.setOnClickListener {
                if (multiselect) {
                    app.isChecked = !app.isChecked

                    if (app.isChecked)
                        myTitle++
                    else {
                        myTitle--
                        (mode?.menu?.findItem(R.id.action_select_all)?.actionView as CheckBox).isChecked =
                            false
                    }

                    mode?.title = myTitle.toString()

                    checkBox.isVisible = app.isChecked

                } else {
                    val optionsBottomSheet = AppOptionsBottomSheet(app)
                    optionsBottomSheet.show(
                        mainFragment.requireActivity().supportFragmentManager,
                        AppOptionsBottomSheet.TAG
                    )
                }
            }
            // ItemView on Long Click
            root.setOnLongClickListener {
                return@setOnLongClickListener if (!multiselect) {
                    multiselect = true
                    checkBox.isVisible = true
                    app.isChecked = true
                    myTitle++
                    (mainFragment.requireActivity() as AppCompatActivity).startSupportActionMode(
                        this@AppListAdapter
                    )
                    true
                } else
                    false
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
             * The Filter Algorythm
             * @param charSequence
             * charSequence to find
             * @return FilterResultes
             * Apps that match charSequence
             */
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase(Locale.getDefault())
                val myDatasetFiltered = if (charString.isEmpty()) {
                    myDataset
                } else {
                    myDataset.filter {
                        it.appName.toLowerCase(Locale.getDefault()).contains(charString)
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
                myDatasetFiltered = filterResults.values as MutableList<Application>
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Sorts data on Call after Selected Sort type
     */
    fun sortData() {
        myDataset = settingsManager.sortData(myDataset)
        myDatasetFiltered = myDataset.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Updates data with delivered Dataset
     * @param apps Updated set of Applications
     */
    fun updateData(apps: List<Application>) {
        myDataset = apps
        myDatasetFiltered = myDataset.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate the menu resource providing context menu items
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.menu_multiselect, menu)
        this.mode = mode
        mode.title = myTitle.toString()
        menu.findItem(R.id.action_select_all)?.also {
            (it.actionView as CheckBox).setOnCheckedChangeListener { _, isChecked ->
                it.isChecked = isChecked
                onActionItemClicked(mode, it)
            }
        }
        mainFragment.enableRefresh(false)
        mainFragment.stateBottomSheetBehaviour(BottomSheetBehavior.STATE_EXPANDED)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> if (item.isChecked) {
                myDatasetFiltered.forEach {
                    it.isChecked = true
                }
                myTitle = itemCount
                mode.title = itemCount.toString()
                notifyDataSetChanged()
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        myDatasetFiltered.forEach {
            it.isChecked = false
        }
        myTitle = 0
        multiselect = false
        mainFragment.enableRefresh(true)
        mainFragment.stateBottomSheetBehaviour(BottomSheetBehavior.STATE_COLLAPSED)
        this.mode = null
        notifyDataSetChanged()
    }

    /**
     * Destroy action Mode if active
     */
    fun finish() {
        mode?.finish()
    }
}
