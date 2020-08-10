package domilopment.apkextractor

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.app_list_item.view.*
import java.util.*

class AppListAdapter(
    private val mainActivity: MainActivity
) :
    RecyclerView.Adapter<AppListAdapter.MyViewHolder>(),
    Filterable
{
    private val settingsManager = mainActivity.settingsManager
    // Static Dataset for Smoother transition
    private var myDataset = listOf<Application>()
    // Shown Data in ListView
    var myDatasetFiltered: MutableList<Application> = myDataset.toMutableList()
        private set
    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView)

    /**
     * Creates ViewHolder with Layout
     * @param parent
     * @param viewType
     * @return MyViewHolder
     * A ViewHolder with Layout app_list_item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false).let {
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
        holder.itemView.apply {
            firstLine.text = app.appName
            secondLine.text = app.appPackageName
            icon.setImageDrawable(app.appIcon)
            checkBox.isChecked = app.isChecked
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                app.isChecked = isChecked
            }
            setOnLongClickListener {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", app.appPackageName, null)
                }.also {
                    mainActivity.startActivity(it)
                }
                return@setOnLongClickListener true
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
        settingsManager.sortData(myDatasetFiltered)
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
}