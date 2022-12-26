package domilopment.apkextractor.apkSaveNamePreferenceDialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.ApkNameListBinding

class ApkNameDialogFragment : DialogFragment() {
    private var _binding: ApkNameListBinding? = null
    private val binding get() = _binding!!

    private lateinit var list: RecyclerView

    private lateinit var defaultValues: Array<String>
    private lateinit var prefs: Set<String>

    lateinit var groundTruthMap: Map<String, String>
    lateinit var unselectedList: ArrayList<String>
    var selectedList: ArrayList<String> = arrayListOf()

    companion object {
        private const val selectedListSaveInstanceStateKey = "SELECTED_LIST"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList(selectedListSaveInstanceStateKey, selectedList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ApkNameListBinding.inflate(layoutInflater)

        val savedSelectedList =
            savedInstanceState?.getStringArrayList(selectedListSaveInstanceStateKey)

        defaultValues =
            requireContext().resources.getStringArray(R.array.app_save_name_default_values)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getStringSet("app_save_name", defaultValues.toSet()) as Set<String>

        val names = requireContext().resources.getStringArray(R.array.app_save_name_names)
        val values = requireContext().resources.getStringArray(R.array.app_save_name_values)
        groundTruthMap = values.zip(names).toMap()
        unselectedList = groundTruthMap.keys.toList() as ArrayList<String>
        val processedPrefs = try {
            prefs.toSortedSet(compareBy { it[0].digitToInt() })
                .map { it.removeRange(0, 2) }
        } catch (e: Exception) {
            prefs
        }

        // Restore open Prefs after rotation or load from Preferences
        savedSelectedList?.let {
            it.forEach { item ->
                selectedList.add(item)
                unselectedList.remove(item)
            }
        } ?: kotlin.run {
            processedPrefs.forEach { item ->
                selectedList.add(item)
                unselectedList.remove(item)
            }
        }

        val adapter = ApkNameListAdapter(this)
        list = binding.appNameList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        val helper = ItemTouchHelper(ApkNameListDragAdapter(adapter))
        helper.attachToRecyclerView(list)

        return activity?.let {
            AlertDialog.Builder(it).setTitle(R.string.app_save_name).setView(binding.root)
                .setPositiveButton(requireContext().getString(R.string.app_name_dialog_ok)) { _, _ -> savePrefs() }
                .setNegativeButton(requireContext().getString(R.string.app_name_dialog_cancel)) { _, _ -> cancel() }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        selectedList.clear()
    }

    /**
     * Save selection to SharedPreferences
     */
    private fun savePrefs() {
        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                .putStringSet(
                    "app_save_name",
                    selectedList.map { "${selectedList.indexOf(it)}:$it" }.toSet()
                ).commit()
        ) {
            selectedList.clear()
            dismiss()
        }
    }

    private fun cancel() {
        selectedList.clear()
        this.dismiss()
    }

    /**
     * Deactivate the OK Button if selection does not contain
     * App Name or Package Name and inform the user
     */
    fun isSelectionPositive() {
        val isEnableable = selectedList.contains("name") || selectedList.contains("package")
        (requireDialog() as AlertDialog).getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled =
            isEnableable
        if (!isEnableable) Toast.makeText(
            context,
            requireContext().getString(R.string.app_save_name_toast),
            Toast.LENGTH_LONG
        ).show()
    }
}