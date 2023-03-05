package domilopment.apkextractor.ui.apkNamePreferenceDialog

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ApkNameListDragAdapter(private val apkNameListAdapter: ApkNameListAdapter) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        viewHolder as ApkNameListAdapter.MyViewHolder
        target as ApkNameListAdapter.MyViewHolder
        val fromCheckValue = viewHolder.binding.appNameListItemCheckbox.isChecked
        val toCheckValue = target.binding.appNameListItemCheckbox.isChecked
        return if (fromCheckValue and toCheckValue) {
            apkNameListAdapter.swapSelected(
                viewHolder.bindingAdapterPosition,
                target.bindingAdapterPosition
            )
            true
        } else if (fromCheckValue and !toCheckValue) {
            apkNameListAdapter.moveFromSelectedToUnselected(
                viewHolder,
                target.bindingAdapterPosition
            )
            true
        } else if (!fromCheckValue and toCheckValue) {
            apkNameListAdapter.moveFromUnselectedToSelected(
                viewHolder,
                target.bindingAdapterPosition
            )
            true
        } else if (!fromCheckValue and !toCheckValue) {
            apkNameListAdapter.swapUnselected(
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