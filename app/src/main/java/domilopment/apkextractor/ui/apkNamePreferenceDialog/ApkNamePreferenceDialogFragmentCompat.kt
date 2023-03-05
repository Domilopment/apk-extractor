package domilopment.apkextractor.ui.apkNamePreferenceDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.ApkNameListBinding

class ApkNamePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var _binding: ApkNameListBinding? = null
    private val binding get() = _binding!!

    private lateinit var entries: Array<CharSequence>
    private lateinit var entryValues: Array<CharSequence>
    private lateinit var defaultValues: Array<String>
    private lateinit var persistedStrings: Array<CharSequence>

    val unselectedList: ArrayList<String> = arrayListOf()
    val selectedList: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (preference as MultiSelectListPreference).also {
            entries = it.entries
            entryValues = it.entryValues
        }

        check(entries.size == entryValues.size) {
            "ListPreference requires an entries array and an entryValues array which are both the same length"
        }

        defaultValues = resources.getStringArray(R.array.app_save_name_default_values)
        persistedStrings = try {
            preference.getPersistedStringSet(
                defaultValues.map { "${defaultValues.indexOf(it)}:$it" }.toSet()
            ).toSortedSet(compareBy { it[0].digitToInt() })
                .map { it.removeRange(0, 2) }.toTypedArray()
        } catch (e: Exception) {
            preference.getPersistedStringSet(defaultValues.toSet()).toTypedArray()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult and preference.callChangeListener(selectedList)) {
            preference.persistStringSet(
                selectedList.map { "${selectedList.indexOf(it)}:$it" }.toSet()
            )
        }
    }

    override fun onCreateDialogView(context: Context): View {
        _binding = ApkNameListBinding.inflate(layoutInflater)

        val adapter = ApkNameListAdapter(this)
        val list = binding.appNameList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        val helper = ItemTouchHelper(ApkNameListDragAdapter(adapter))
        helper.attachToRecyclerView(list)

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val savedSelectedList =
            savedInstanceState?.getStringArrayList(selectedListSaveInstanceStateKey)

        unselectedList.addAll(entryValues.toList() as ArrayList<String>)
        // Restore open Prefs after rotation or load from Preferences
        savedSelectedList?.let {
            it.forEach { item ->
                selectedList.add(item)
                unselectedList.remove(item)
            }
        } ?: kotlin.run {
            persistedStrings.forEach { item ->
                selectedList.add(item.toString())
                unselectedList.remove(item)
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList(selectedListSaveInstanceStateKey, selectedList)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        isSelectionPositive()
    }

    /**
     * Deactivate the OK Button if selection does not contain
     * App Name or Package Name and inform the user
     */
    fun isSelectionPositive() {
        val isEnableable = selectedList.contains("name") || selectedList.contains("package")
        (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            isEnableable
        if (!isEnableable) Toast.makeText(
            context,
            requireContext().getString(R.string.app_save_name_toast),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Get entry (Name) for entryValue
     * @param value on pos x of input values
     * @return String of entry (name) on pos x of input entries
     */
    fun getItemName(value: String) = entries[entryValues.indexOf(value)]

    companion object {
        private const val selectedListSaveInstanceStateKey = "SELECTED_LIST"

        fun newInstance(key: String): ApkNamePreferenceDialogFragmentCompat {
            val fragment = ApkNamePreferenceDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}