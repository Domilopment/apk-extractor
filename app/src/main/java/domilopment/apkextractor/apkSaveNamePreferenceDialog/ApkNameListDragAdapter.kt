package domilopment.apkextractor.apkSaveNamePreferenceDialog

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ApkNameListDragAdapter(private val apkNameListAdapter: ApkNameListAdapter) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if ((viewHolder as ApkNameListAdapter.MyViewHolder).binding.appNameListItemCheckbox.isChecked) super.getMovementFlags(
            recyclerView,
            viewHolder
        ) else 0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        viewHolder as ApkNameListAdapter.MyViewHolder
        target as ApkNameListAdapter.MyViewHolder
        return if (viewHolder.binding.appNameListItemCheckbox.isChecked and target.binding.appNameListItemCheckbox.isChecked) {
            apkNameListAdapter.swapItems(
                viewHolder.bindingAdapterPosition,
                target.bindingAdapterPosition
            )
            true
        } else false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // No Swipe Actions Supported
    }
}