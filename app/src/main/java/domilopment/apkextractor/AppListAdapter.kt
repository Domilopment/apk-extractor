package domilopment.apkextractor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.app_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class AppListAdapter(private val myDataset: List<Application>) : RecyclerView.Adapter<AppListAdapter.MyViewHolder>(), Filterable {
    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView)
    var myDatasetFiltered: List<Application> = myDataset
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val myView = LayoutInflater.from(parent.context).inflate(R.layout.app_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(myView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val app = myDatasetFiltered[position]
        holder.itemView.firstLine.text = app.appName
        holder.itemView.secondLine.text = app.appPackageName
        holder.itemView.icon.setImageDrawable(app.appIcon)
        holder.itemView.checkBox.setOnClickListener {
            app.check(!app.isChecked)
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
}