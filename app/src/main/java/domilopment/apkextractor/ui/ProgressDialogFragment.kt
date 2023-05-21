package domilopment.apkextractor.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.ProgressDialogBinding
import kotlinx.coroutines.launch

class ProgressDialogFragment : DialogFragment() {
    private var _binding: ProgressDialogBinding? = null
    private val binding get() = _binding!!

    private val model by activityViewModels<ProgressDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.progressDialogState.collect { uiState ->
                    if (!uiState.shouldBeShown) dismiss()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title: Int = arguments?.getInt("title") ?: R.string.progress_dialog_title_placeholder

        _binding = DataBindingUtil.inflate(layoutInflater, R.layout.progress_dialog, null, false)
        binding.lifecycleOwner = this
        binding.model = model

        return activity?.let {
            MaterialAlertDialogBuilder(it).setTitle(title).setView(binding.root)
                .setCancelable(false).create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        fun newInstance(title: Int): ProgressDialogFragment {
            val fragment = ProgressDialogFragment()
            val bundle = Bundle(1)
            bundle.putInt("title", title)
            fragment.arguments = bundle
            return fragment
        }
    }
}