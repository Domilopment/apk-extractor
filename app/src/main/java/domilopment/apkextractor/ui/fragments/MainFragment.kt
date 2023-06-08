package domilopment.apkextractor.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.FragmentMainBinding
import domilopment.apkextractor.ui.viewModels.MainViewModel
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val model by activityViewModels<MainViewModel>()

    private var isActioModeActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.mainFragmentState.collect { uiState ->
                    isActioModeActive = uiState.actionMode
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomNavigation.setupWithNavController(binding.mainNavFragmentHost.getFragment<NavHostFragment>().navController)
        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().findViewById<View>(android.R.id.content).rootView) { _, insets ->
            if (!isActioModeActive) binding.bottomNavigation.isVisible = !insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
    }

    /**
     * Show or hide bottom navigation menu
     * @param visible boolean for menu should be shown
     */
    fun enableNavigation(visible: Boolean) {
        binding.bottomNavigation.isVisible = visible
    }
}