package domilopment.apkextractor.appList

import android.content.pm.ApplicationInfo
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
import domilopment.apkextractor.R
import domilopment.apkextractor.fragments.MainFragment
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.SettingsManager
import domilopment.apkextractor.utils.Utils

class AppListTouchHelperCallback(
    private val mainFragment: MainFragment,
    private val swipeCallback: (viewHolder: RecyclerView.ViewHolder, apkAction: ApkActionsOptions) -> Unit,
) : ItemTouchHelper.SimpleCallback(
    0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private val swipeRightLayout: View
    private val swipeLeftLayout: View
    private var apkActionsOptions: ApkActionsOptions? = null

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

    /**
     * Only allow all swipe dirs if selected action can be performed on app
     * means don't allow trying uninstall system apps or open app without launch intent
     */
    override fun getSwipeDirs(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int {
        val settingsManager = SettingsManager(mainFragment.requireContext())
        val leftSwipeAction = settingsManager.getLeftSwipeAction()
        val rightSwipeAction = settingsManager.getRightSwipeAction()
        val app =
            (recyclerView.adapter as AppListAdapter).myDatasetFiltered[viewHolder.bindingAdapterPosition]
        var swipeDirs = 0

        if (((leftSwipeAction) != ApkActionsOptions.OPEN || app.launchIntent != null) && (leftSwipeAction != ApkActionsOptions.UNINSTALL || (!Utils.isSystemApp(
                app
            ) || (app.appUpdateTime > app.appInstallTime)))
        ) swipeDirs = swipeDirs or ItemTouchHelper.LEFT

        if (((rightSwipeAction) != ApkActionsOptions.OPEN || app.launchIntent != null) && (rightSwipeAction != ApkActionsOptions.UNINSTALL || ((app.appFlags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM) || (app.appUpdateTime > app.appInstallTime)))) swipeDirs =
            swipeDirs or ItemTouchHelper.RIGHT

        return super.getSwipeDirs(recyclerView, viewHolder) and swipeDirs
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT, ItemTouchHelper.RIGHT -> apkActionsOptions?.let {
                swipeCallback(
                    viewHolder, it
                )
            }
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
            val settingsManager = SettingsManager(mainFragment.requireContext())

            val swipeLayout: View
            val icon: Drawable?
            val text: String
            if (dX > 0) {
                swipeLayout = swipeRightLayout
                apkActionsOptions = settingsManager.getRightSwipeAction() ?: ApkActionsOptions.SAVE
                icon = AppCompatResources.getDrawable(
                    mainFragment.requireContext(), apkActionsOptions!!.icon
                )
                text = mainFragment.resources.getText(apkActionsOptions!!.title).toString()
            } else {
                swipeLayout = swipeLeftLayout
                apkActionsOptions = settingsManager.getLeftSwipeAction() ?: ApkActionsOptions.SHARE
                icon = AppCompatResources.getDrawable(
                    mainFragment.requireContext(), apkActionsOptions!!.icon
                )
                text = mainFragment.resources.getText(apkActionsOptions!!.title).toString()
            }

            val imageView = swipeLayout.findViewById<ShapeableImageView>(R.id.swipeImageView)
            imageView.setImageDrawable(icon)

            val textView = swipeLayout.findViewById<MaterialTextView>(R.id.swipeTextView)
            textView.text = text

            if (swipeLayout.isDirty) {
                val widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(itemView.width, MeasureSpec.EXACTLY)
                val heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(itemView.height, MeasureSpec.EXACTLY)
                swipeLayout.measure(widthMeasureSpec, heightMeasureSpec)
                swipeLayout.layout(
                    itemView.left, itemView.top, itemView.right, itemView.bottom
                )
            }

            canvas.withTranslation(
                itemView.left.toFloat(), itemView.top.toFloat()
            ) { swipeLayout.draw(canvas) }
        }
        super.onChildDraw(
            canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
        )
    }
}