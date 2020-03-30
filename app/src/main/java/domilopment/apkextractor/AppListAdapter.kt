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
import domilopment.apkextractor.activitys.MainActivity
import domilopment.apkextractor.data.Application
import kotlinx.android.synthetic.main.app_list_item.view.*
import java.util.*

class AppListAdapter(
    private val myDataset: List<Application>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>(), Filterable {
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

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDatasetFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().toLowerCase(Locale.getDefault())
                myDatasetFiltered = if (charString.isEmpty()) {
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