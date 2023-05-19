package domilopment.apkextractor.ui.apkList

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.databinding.ApkListItemBinding
import domilopment.apkextractor.ui.ApkOptionsBottomSheet
import domilopment.apkextractor.ui.fragments.ApkListFragment
import java.util.*

class ApkListAdapter(
    private val apkListFragment: ApkListFragment
) : RecyclerView.Adapter<ApkListAdapter.MyViewHolder>(), Filterable {
    // Static Dataset for Smoother transition
    private var myDataset = listOf<PackageArchiveModel>()

    // Shown Data in ListView
    var myDatasetFiltered: MutableList<PackageArchiveModel> = myDataset.toMutableList()
        private set

    private var searchString = ""

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        val binding: ApkListItemBinding = ApkListItemBinding.bind(myView)
    }

    private val textColorHighlight = MaterialColors.getColor(
        apkListFragment.requireContext(), android.R.attr.textColorHighlight, Color.CYAN
    )

    /**
     * Creates ViewHolder with Layout
     * @param parent
     * @param viewType
     * @return MyViewHolder
     * A ViewHolder with Layout app_list_item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.apk_list_item, parent, false)
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
        val apk = myDatasetFiltered[position]
        // Apply data from Dataset item to holder
        holder.binding.apply {
            apkFileName.text = getSpannable(apk.fileName)
            apkAppName.text = getSpannable(apk.appName.toString())
            apkPackageName.text = getSpannable(apk.appPackageName)
            apkIcon.setImageDrawable(apk.appIcon)
            apkVersionName.text = getSpannable(
                apkListFragment.getString(
                    R.string.apk_holder_version, apk.appVersionName, apk.appVersionCode
                )
            )
            // ItemView on Click
            root.setOnClickListener {
                if (apkListFragment.isRefreshing()) return@setOnClickListener

                apkListFragment.selectApplication(apk)
                apkListFragment.requireActivity().supportFragmentManager.let {
                    ApkOptionsBottomSheet.newInstance().apply {
                        show(it, ApkOptionsBottomSheet.TAG)
                    }
                }

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
                searchString = charSequence.toString().trim()
                val myDatasetFiltered = if (searchString.isBlank()) {
                    myDataset
                } else {
                    myDataset.filter {
                        it.fileName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appPackageName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appVersionName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appVersionCode?.toString()?.contains(
                            searchString, ignoreCase = true
                        ) ?: false
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
                    (filterResults.values as List<PackageArchiveModel>).toMutableList()
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Updates data with delivered Dataset
     * @param apps Updated set of Applications
     */
    fun updateData(apps: List<PackageArchiveModel>, updateTrigger: Boolean) {
        if (apps == myDataset && !updateTrigger) return
        myDataset = apps
        myDatasetFiltered = myDataset.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Creates a String Spannable from text, that shows the position of the search word inside the String
     * @param text text that is displayed to the user
     * @return String spannable with color marked search word
     */
    private fun getSpannable(text: String?): Spannable {
        if (text == null) return "".toSpannable()

        val spannable = text.toSpannable()
        if (searchString.isNotBlank() && text.contains(searchString, ignoreCase = true)) {
            val startIndex = text.lowercase().indexOf(searchString.lowercase())
            spannable.setSpan(
                ForegroundColorSpan(textColorHighlight),
                startIndex,
                startIndex + searchString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}
