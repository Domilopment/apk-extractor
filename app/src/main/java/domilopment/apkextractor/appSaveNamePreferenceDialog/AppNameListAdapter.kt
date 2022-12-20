package domilopment.apkextractor.appSaveNamePreferenceDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.AppNameListItemBinding
import java.util.Collections

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
        val isItemSelected = appSaveNamePreferenceDialog.selectedList.contains(key)
        holder.binding.appNameListItemCheckbox.isChecked = isItemSelected
        holder.binding.appNameListItemDragHandle.isVisible = isItemSelected
        holder.binding.appNameListItemCheckbox.setOnClickListener { view ->
            run {
                appSaveNamePreferenceDialog.selectedList.remove(key)
                appSaveNamePreferenceDialog.unselectedList.remove(key)
                if ((view as CheckBox).isChecked) {
                    holder.binding.appNameListItemDragHandle.isVisible = true
                    appSaveNamePreferenceDialog.selectedList.add(
                        if (position > getSelectetSize()) getSelectetSize() else position,
                        key
                    )
                } else {
                    holder.binding.appNameListItemDragHandle.isVisible = false
                    appSaveNamePreferenceDialog.unselectedList.add(0, key)
                }
                appSaveNamePreferenceDialog.isSelectionPositive()
                itemList =
                    appSaveNamePreferenceDialog.selectedList + appSaveNamePreferenceDialog.unselectedList
                notifyDataSetChanged()
            }
        }
    }

    /**
     * swap items in different Lists
     * @param fromPosition The place the item was at the beginning
     * @param toPosition The place the item is Dragged to
     */
    private fun swapItems(fromPosition: Int, toPosition: Int, list: List<String>) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition)
                Collections.swap(list, i, i + 1)
        } else if (fromPosition > toPosition) {
            for (i in fromPosition downTo toPosition + 1)
                Collections.swap(list, i, i - 1)
        }
    }

    /**
     * Function called to swap dragged items in Selected Items
     * @param fromPosition The place the item was at the beginning
     * @param toPosition The place the item is Dragged to
     */
    fun swapItems(fromPosition: Int, toPosition: Int) {
        swapItems(fromPosition, toPosition, appSaveNamePreferenceDialog.selectedList)
        itemList =
            appSaveNamePreferenceDialog.selectedList + appSaveNamePreferenceDialog.unselectedList
        notifyItemMoved(fromPosition, toPosition)
    }
}