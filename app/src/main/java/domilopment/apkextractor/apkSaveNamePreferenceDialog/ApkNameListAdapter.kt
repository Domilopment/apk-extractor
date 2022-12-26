package domilopment.apkextractor.apkSaveNamePreferenceDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.ApkNameListItemBinding
import java.util.Collections

class ApkNameListAdapter(
    private val apkSaveNamePreferenceDialog: ApkNameDialogFragment
) : RecyclerView.Adapter<ApkNameListAdapter.MyViewHolder>() {
    private var itemList =
        apkSaveNamePreferenceDialog.selectedList + apkSaveNamePreferenceDialog.unselectedList

    class MyViewHolder(myView: View) : RecyclerView.ViewHolder(myView) {
        val binding: ApkNameListItemBinding = ApkNameListItemBinding.bind(myView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.apk_name_list_item, parent, false)
            .let {
                MyViewHolder(it)
            }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getSelectetSize(): Int {
        return apkSaveNamePreferenceDialog.selectedList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val key = itemList[position]
        val name = apkSaveNamePreferenceDialog.groundTruthMap[key]
        holder.binding.appNameListItemText.text = name
        val isItemSelected = apkSaveNamePreferenceDialog.selectedList.contains(key)
        holder.binding.appNameListItemCheckbox.isChecked = isItemSelected
        holder.binding.appNameListItemDragHandle.isVisible = isItemSelected
        holder.binding.appNameListItemCheckbox.setOnClickListener { view ->
            apkSaveNamePreferenceDialog.selectedList.remove(key)
            apkSaveNamePreferenceDialog.unselectedList.remove(key)
            if ((view as CheckBox).isChecked) {
                holder.binding.appNameListItemDragHandle.isVisible = true
                apkSaveNamePreferenceDialog.selectedList.add(
                    if (position > getSelectetSize()) getSelectetSize() else position,
                    key
                )
            } else {
                holder.binding.appNameListItemDragHandle.isVisible = false
                apkSaveNamePreferenceDialog.unselectedList.add(0, key)
            }
            apkSaveNamePreferenceDialog.isSelectionPositive()
            itemList =
                apkSaveNamePreferenceDialog.selectedList + apkSaveNamePreferenceDialog.unselectedList
            notifyDataSetChanged()
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
        swapItems(fromPosition, toPosition, apkSaveNamePreferenceDialog.selectedList)
        itemList =
            apkSaveNamePreferenceDialog.selectedList + apkSaveNamePreferenceDialog.unselectedList
        notifyItemMoved(fromPosition, toPosition)
    }
}