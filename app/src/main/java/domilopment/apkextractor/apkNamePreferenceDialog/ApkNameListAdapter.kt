package domilopment.apkextractor.apkNamePreferenceDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.ApkNameListItemBinding
import java.util.Collections

class ApkNameListAdapter(
    private val apkNamePreferenceDialog: ApkNamePreferenceDialogFragmentCompat
) : RecyclerView.Adapter<ApkNameListAdapter.MyViewHolder>() {
    private val itemList get() = apkNamePreferenceDialog.selectedList + apkNamePreferenceDialog.unselectedList

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val key = itemList[position]
        val name = apkNamePreferenceDialog.getItemName(key)
        holder.binding.appNameListItemText.text = name
        val isItemSelected = apkNamePreferenceDialog.selectedList.contains(key)
        holder.binding.appNameListItemCheckbox.isChecked = isItemSelected
        holder.binding.appNameListItemDragHandle.isVisible = isItemSelected
        holder.binding.root.setOnClickListener {
            val checkBox = holder.binding.appNameListItemCheckbox
            checkBox.isChecked = !checkBox.isChecked
            apkNamePreferenceDialog.selectedList.remove(key)
            apkNamePreferenceDialog.unselectedList.remove(key)
            holder.binding.appNameListItemDragHandle.isVisible = checkBox.isChecked
            if (checkBox.isChecked) {
                apkNamePreferenceDialog.selectedList.add(key)
            } else {
                apkNamePreferenceDialog.unselectedList.add(0, key)
            }
            apkNamePreferenceDialog.isSelectionPositive()
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
        swapItems(fromPosition, toPosition, apkNamePreferenceDialog.selectedList)
        notifyItemMoved(fromPosition, toPosition)
    }
}