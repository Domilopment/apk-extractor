package domilopment.apkextractor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import domilopment.apkextractor.fragments.MainFragment

class AppListTouchHelperCallback(
    private val mainFragment: MainFragment,
    private val rightSwipeCallback: (viewHolder: RecyclerView.ViewHolder) -> Unit,
    private val leftSwipeCallback: (viewHolder: RecyclerView.ViewHolder) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
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
        super.onChildDraw(
            canvas,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
            val itemView = viewHolder.itemView
            val margin = 30

            ColorDrawable().apply {
                color = MaterialColors.getColor(
                    mainFragment.requireContext(),
                    R.attr.colorOnSecondary,
                    null
                )
                bounds =
                    Rect(itemView.left, itemView.top, itemView.right, itemView.bottom)
            }.also {
                it.draw(canvas)
            }

            val colorSecondary = MaterialColors.getColor(
                mainFragment.requireContext(),
                R.attr.colorSecondary,
                null
            )

            val textPaint = Paint().apply {
                color = colorSecondary
                textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20F, mainFragment.resources.displayMetrics)
                textAlign = Paint.Align.CENTER
            }
            val textYPos =
                (itemView.top + itemView.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2)

            if (dX > 0) {
                // Save Icon Drawable
                AppCompatResources.getDrawable(
                    mainFragment.requireContext(),
                    R.drawable.ic_baseline_save_24
                )
                    ?.apply {
                        bounds = Rect(
                            margin,
                            itemView.top + margin,
                            itemView.bottom - itemView.top - margin,
                            itemView.bottom - margin
                        )
                        setTint(colorSecondary)
                    }.also {
                        it?.draw(canvas)
                    }

                // Save Text
                val text =
                    mainFragment.resources.getText(R.string.action_bottom_sheet_save)
                        .toString()
                canvas.drawText(
                    text,
                    itemView.bottom - itemView.top + textPaint.measureText(text) / 2,
                    textYPos,
                    textPaint
                )
            } else {
                // Share Icon Drawable
                AppCompatResources.getDrawable(
                    mainFragment.requireContext(),
                    R.drawable.ic_share_24dp
                )
                    ?.apply {
                        bounds = Rect(
                            itemView.right - (itemView.bottom - itemView.top) + margin,
                            itemView.top + margin,
                            itemView.right - margin,
                            itemView.bottom - margin
                        )
                        setTint(colorSecondary)
                    }.also {
                        it?.draw(canvas)
                    }

                // Share Text
                val text =
                    mainFragment.resources.getText(R.string.action_bottom_sheet_share)
                        .toString()
                canvas.drawText(
                    text,
                    itemView.right - (itemView.bottom - itemView.top) - textPaint.measureText(
                        text
                    ) / 2,
                    textYPos,
                    textPaint
                )
            }
        }
    }
}