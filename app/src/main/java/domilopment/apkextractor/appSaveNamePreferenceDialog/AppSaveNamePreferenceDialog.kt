package domilopment.apkextractor.appSaveNamePreferenceDialog

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.AppNameListBinding

class AppSaveNamePreferenceDialog(context: Context) : AlertDialog(context) {
    private val binding = AppNameListBinding.inflate(this.layoutInflater)
    private val list: RecyclerView

    private val defaultValues =
        context.resources.getStringArray(R.array.app_save_name_default_values)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        .getStringSet("app_save_name", defaultValues.toSet())

    val groundTruthMap: Map<String, String>
    val unselectedList: ArrayList<String>
    val selectedList: ArrayList<String> = ArrayList()

    init {
        val names = context.resources.getStringArray(R.array.app_save_name_names)
        val values = context.resources.getStringArray(R.array.app_save_name_values)
        groundTruthMap = values.zip(names).toMap()
        unselectedList = groundTruthMap.keys.toList() as ArrayList<String>
        val processedPrefs = try {
            prefs?.toSortedSet(compareBy<String> { it[0].digitToInt() })
                ?.map { it.removeRange(0, 2) }
        } catch (e: Exception) {
            prefs
        }
        processedPrefs?.forEach { item ->
            selectedList.add(item)
            unselectedList.remove(item)
        }

        setView(binding.root)
        setTitle(R.string.app_save_name)
        setCancelable(false)

        val adapter = AppNameListAdapter(this@AppSaveNamePreferenceDialog)
        list = binding.appNameList.apply {
            layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically() = false
            }
            this.adapter = adapter
        }
        val helper = ItemTouchHelper(AppNameListDragAdapter(adapter))
        helper.attachToRecyclerView(list)

        setButton(
            BUTTON_POSITIVE,
            context.getString(R.string.app_name_dialog_ok)
        ) { _, _ -> savePrefs() }
        setButton(
            BUTTON_NEGATIVE,
            context.getString(R.string.app_name_dialog_cancel)
        ) { _, _ -> dismiss() }
    }

    /**
     * Save selection to SharedPreferences
     */
    private fun savePrefs() {
        if (PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(
                    "app_save_name",
                    selectedList.map { "${selectedList.indexOf(it)}:$it" }.toSet()
                ).commit()
        ) dismiss()
    }

    /**
     * Deactivate the OK Button if selection does not contain
     * App Name or Package Name and inform the user
     */
    fun isSelectionPositive() {
        val isEnableable = selectedList.contains("name") || selectedList.contains("package")
        getButton(BUTTON_POSITIVE).isEnabled = isEnableable
        if (!isEnableable) Toast.makeText(
            context,
            context.getString(R.string.app_save_name_toast),
            Toast.LENGTH_LONG
        ).show()
    }

}