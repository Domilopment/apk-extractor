package domilopment.apkextractor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.data.Application
import domilopment.apkextractor.fragments.MainFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.app_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class AppListAdapter(
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>(),
    Filterable,
    LoaderManager.LoaderCallbacks<List<Application>> {
    // Static Dataset for Smoother transition
    private val myDataset = ArrayList(SettingsManager(mainActivity).selectedAppTypes())
    // Shown Data in ListView
    var myDatasetFiltered: List<Application> = myDataset
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
                myDatasetFiltered = filterResults.values as List<Application>
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Sorts date on Call after Selected Sort type
     */
    fun sortData() {
        SettingsManager(mainActivity).sortData(myDatasetFiltered)
        notifyDataSetChanged()
    }

    /**
     * Update Dataset
     */
    fun updateData(fragment: MainFragment? = null) {
        mainActivity.refresh?.isRefreshing = true
        mainActivity.run {
            LoaderManager.getInstance(this)
                .initLoader(SettingsManager.DATA_LOADER_ID, null, this@AppListAdapter)
                .forceLoad()
        }
    }

    /**
     * Creates Data Loader Class on Request
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Application>> {
        return when (id) {
            SettingsManager.DATA_LOADER_ID -> SettingsManager(mainActivity)
            else -> throw Exception("No such Loader")
        }
    }

    /**
     * Updates Dataset when Loader delivers result
     */
    override fun onLoadFinished(loader: Loader<List<Application>>, data: List<Application>) {
        myDataset.clear()
        myDataset.addAll(data)
        myDatasetFiltered = myDataset
        notifyDataSetChanged()
        mainActivity.refresh?.isRefreshing = false
    }

    /**
     * Clear Data on Loader Reset
     */
    override fun onLoaderReset(loader: Loader<List<Application>>) {
        myDataset.clear()
        myDatasetFiltered = myDataset
    }
}