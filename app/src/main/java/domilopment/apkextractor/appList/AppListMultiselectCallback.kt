package domilopment.apkextractor.appList

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.view.ActionMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import domilopment.apkextractor.R
import domilopment.apkextractor.fragments.MainFragment

class AppListMultiselectCallback(
    private val mainFragment: MainFragment,
    private val appListAdapter: AppListAdapter
) : ActionMode.Callback {
    private var mode: ActionMode? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate the menu resource providing context menu items
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.menu_multiselect, menu)
        this.mode = mode
        setModeTitle(mode = mode)
        menu.findItem(R.id.action_select_all)?.also {
            (it.actionView as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
                it.isChecked = isChecked
                onActionItemClicked(mode, it)
                buttonView.isChecked = false
            }
        }
        mainFragment.enableRefresh(false)
        mainFragment.attachSwipeHelper(false)
        mainFragment.stateBottomSheetBehaviour(BottomSheetBehavior.STATE_EXPANDED)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> if (item.isChecked) {
                appListAdapter.myDatasetFiltered.forEach {
                    it.isChecked = true
                }
                setModeTitle(mode = mode)
                appListAdapter.notifyDataSetChanged()
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        appListAdapter.myDatasetFiltered.forEach {
            it.isChecked = false
        }
        mainFragment.enableRefresh(true)
        mainFragment.attachSwipeHelper(true)
        mainFragment.stateBottomSheetBehaviour(BottomSheetBehavior.STATE_COLLAPSED)
        mainFragment.showSearchView()
        this.mode = null
        appListAdapter.notifyDataSetChanged()
        mainFragment.startSupportActionMode(false)
    }

    /**
     * Boolean for checking if action mode is active or not
     */
    fun isActionModeActive(): Boolean = mode != null

    fun setModeTitle(
        itemCount: Int = appListAdapter.myDatasetFiltered.filter { it.isChecked }.size,
        mode: ActionMode? = this.mode
    ) {
        mode?.title = mainFragment.getString(R.string.action_mode_title, itemCount)
    }
}