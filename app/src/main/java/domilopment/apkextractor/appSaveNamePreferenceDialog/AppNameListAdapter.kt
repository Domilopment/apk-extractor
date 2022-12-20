package domilopment.apkextractor.appSaveNamePreferenceDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.AppNameListItemBinding

class AppNameListAdapter(
    private val appSaveNamePreferenceDialog: AppSaveNamePreferenceDialog
) : RecyclerView.Adapter<AppNameListAdapter.MyViewHolder>() {
    private var itemList =
        appSaveNamePreferenceDialog.selectedList + appSaveNamePreferenceDialog.unselectedList

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        val binding: AppNameListItemBinding = AppNameListItemBinding.bind(myView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.app_name_list_item, parent, false)
            .let {
                MyViewHolder(it)
            }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getSelectetSize(): Int {
        return appSaveNamePreferenceDialog.selectedList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val key = itemList[position]
        val name = appSaveNamePreferenceDialog.groundTruthMap[key]
        holder.binding.appNameListItemText.text = name
        holder.binding.appNameListItemCheckbox.isChecked =
            appSaveNamePreferenceDialog.selectedList.contains(key)
        holder.binding.appNameListItemCheckbox.setOnClickListener { view ->
            run {
                appSaveNamePreferenceDialog.selectedList.remove(key)
                appSaveNamePreferenceDialog.unselectedList.remove(key)
                if ((view as CheckBox).isChecked) {
                    appSaveNamePreferenceDialog.selectedList.add(
                        if (position > getSelectetSize()) getSelectetSize() else position,
                        key
                    )
                } else {
                    appSaveNamePreferenceDialog.unselectedList.add(0, key)
                }
                appSaveNamePreferenceDialog.isSelectionPositive()
                itemList =
                    appSaveNamePreferenceDialog.selectedList + appSaveNamePreferenceDialog.unselectedList
                notifyDataSetChanged()
            }
        }
    }
}