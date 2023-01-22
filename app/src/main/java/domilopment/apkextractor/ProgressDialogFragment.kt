package domilopment.apkextractor

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import domilopment.apkextractor.databinding.ProgressDialogBinding
import domilopment.apkextractor.fragments.MainViewModel
import kotlinx.coroutines.launch

class ProgressDialogFragment : DialogFragment() {
    private var _binding: ProgressDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar
    private lateinit var textPercentages: MaterialTextView
    private lateinit var textValue: MaterialTextView
    private lateinit var currentProcess: MaterialTextView
    private val model by activityViewModels<MainViewModel> {
        MainViewModel(requireActivity().application).defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.progressDialogState.collect { uiState ->
                    currentProcess.text = uiState.process ?: ""
                    progressBar.progress = uiState.progress
                    progressBar.max = uiState.tasks
                    textPercentages.text = getPercentageString()
                    textValue.text = getValueString()
                    if (!uiState.shouldBeShown) dismiss()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title: Int = arguments?.getInt("title") ?: R.string.progress_dialog_title_placeholder

        _binding = ProgressDialogBinding.inflate(layoutInflater)
        progressBar = binding.progressHorizontal
        textPercentages = binding.progressPercentages
        textValue = binding.progressValue
        currentProcess = binding.currentProcess

        return activity?.let {
            MaterialAlertDialogBuilder(it).setTitle(title).setView(binding.root)
                .setCancelable(false)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun getPercentageString() = requireContext().getString(
        R.string.progress_dialog_percentage,
        (progressBar.progress.toFloat() / progressBar.max) * 100
    )

    private fun getValueString() = requireContext().getString(
        R.string.progress_dialog_value,
        progressBar.progress,
        progressBar.max
    )

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