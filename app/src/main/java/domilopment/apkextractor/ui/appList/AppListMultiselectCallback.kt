package domilopment.apkextractor.ui.appList

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.view.ActionMode
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.fragments.AppListFragment

class AppListMultiselectCallback(
    private val appListFragment: AppListFragment
) : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate the menu resource providing context menu items
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.menu_multiselect, menu)
        AppListMultiselectCallback.mode = mode
        setModeTitle(mode = mode)
        menu.findItem(R.id.action_select_all)?.also {
            (it.actionView as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
                it.isChecked = isChecked
                onActionItemClicked(mode, it)
                buttonView.isChecked = false
            }
        }
        appListFragment.startSupportActionMode(true)
        appListFragment.showBottomSheet(true)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> if (item.isChecked) {
                appListFragment.selectAllApps(true)
                setModeTitle(mode = mode)
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        appListFragment.selectAllApps(false)
        appListFragment.showBottomSheet(false)
        appListFragment.showSearchView()
        AppListMultiselectCallback.mode = null
        appListFragment.startSupportActionMode(false)
    }

    fun setModeTitle(
        itemCount: Int = appListFragment.getSelectedAppsCount(),
        mode: ActionMode? = AppListMultiselectCallback.mode
    ) {
        mode?.title = appListFragment.getString(R.string.action_mode_title, itemCount)
    }

    fun pause() {
        mode = null
    }

    companion object {
        private var mode: ActionMode? = null

        /**
         * Boolean for checking if action mode is active or not
         */
        fun isActionModeActive(): Boolean = mode != null

        fun finish() = mode?.finish()
    }
}