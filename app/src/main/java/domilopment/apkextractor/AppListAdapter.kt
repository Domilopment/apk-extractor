package domilopment.apkextractor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.activitys.MainActivity
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.app_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class AppListAdapter(private val myDataset: List<Application>, private val mainActivity: MainActivity) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>(), Filterable {
    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView)
    var myDatasetFiltered: List<Application> = myDataset
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false).let {
            // set the view's size, margins, paddings and layout parameters
            MyViewHolder(it)
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        val app = myDatasetFiltered[position]
        holder.setIsRecyclable(false)
        holder.itemView.apply {
            firstLine.text = app.appName
            secondLine.text = app.appPackageName
            icon.setImageDrawable(app.appIcon)
            checkBox.isChecked = app.isChecked
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                app.isChecked = isChecked
                mainActivity.updateIntent()
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDatasetFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase(Locale.getDefault())
                myDatasetFiltered = if (charString.isEmpty()) {
                    myDataset
                } else {
                    val filteredList: MutableList<Application> = ArrayList()
                    for (app in myDataset) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (app.appName.toLowerCase(Locale.getDefault()).contains(charString)
                            || app.appPackageName.contains(charSequence)
                        ) {
                            filteredList.add(app)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = myDatasetFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                myDatasetFiltered = filterResults.values as List<Application>
                notifyDataSetChanged()
            }
        }
    }

    fun sortData() {
            SettingsManager(mainActivity).sortData(myDatasetFiltered)
    }
}