package domilopment.apkextractor

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import domilopment.apkextractor.fragments.MainFragment

class AppListTouchHelperCallback(
    private val mainFragment: MainFragment,
    private val rightSwipeCallback: (viewHolder: RecyclerView.ViewHolder) -> Unit,
    private val leftSwipeCallback: (viewHolder: RecyclerView.ViewHolder) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private val swipeRightLayout: View
    private val swipeLeftLayout: View

    init {
        val layoutInflater = LayoutInflater.from(mainFragment.requireContext())

        swipeRightLayout = layoutInflater.inflate(R.layout.app_list_item_swipe_right, null, false)
        swipeLeftLayout = layoutInflater.inflate(R.layout.app_list_item_swipe_left, null, false)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> leftSwipeCallback(viewHolder)
            ItemTouchHelper.RIGHT -> rightSwipeCallback(viewHolder)
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
            val itemView = viewHolder.itemView

            val swipeLayout: View
            val icon: Drawable?
            val text: String
            if (dX > 0) {
                swipeLayout = swipeRightLayout
                icon = AppCompatResources.getDrawable(
                    mainFragment.requireContext(),
                    R.drawable.ic_baseline_save_48
                )
                text =
                    mainFragment.resources.getText(R.string.action_bottom_sheet_save)
                        .toString()
            } else {
                swipeLayout = swipeLeftLayout
                icon = AppCompatResources.getDrawable(
                    mainFragment.requireContext(),
                    R.drawable.ic_baseline_share_48
                )
                text =
                    mainFragment.resources.getText(R.string.action_bottom_sheet_share)
                        .toString()
            }

            val imageView =
                swipeLayout.findViewById<ShapeableImageView>(R.id.swipeImageView)
            imageView.setImageDrawable(icon)

            val textView =
                swipeLayout.findViewById<MaterialTextView>(R.id.swipeTextView)
            textView.text = text

            if (swipeLayout.isDirty) {
                val widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(itemView.width, MeasureSpec.EXACTLY)
                val heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(itemView.height, MeasureSpec.EXACTLY)
                swipeLayout.measure(widthMeasureSpec, heightMeasureSpec)
                swipeLayout.layout(
                    itemView.left,
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
            }

            canvas.withTranslation(
                itemView.left.toFloat(),
                itemView.top.toFloat()
            ) { swipeLayout.draw(canvas) }
        }
        super.onChildDraw(
            canvas,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }
}