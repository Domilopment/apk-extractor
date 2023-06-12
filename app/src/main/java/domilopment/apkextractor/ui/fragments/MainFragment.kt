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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import domilopment.apkextractor.R
import domilopment.apkextractor.databinding.FragmentMainBinding
import domilopment.apkextractor.ui.appList.AppListMultiselectCallback

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = binding.mainNavFragmentHost.getFragment<NavHostFragment>().navController
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when {
                destination.id != R.id.appListFragment -> AppListMultiselectCallback.finish()
                else -> return@addOnDestinationChangedListener
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { v, insets ->
            binding.bottomNavigation.isVisible =
                !insets.isVisible(WindowInsetsCompat.Type.ime()) && !AppListMultiselectCallback.isActionModeActive()
            ViewCompat.onApplyWindowInsets(v, insets)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ViewCompat.setOnApplyWindowInsetsListener(
            requireActivity().window.decorView, ViewCompat::onApplyWindowInsets
        )
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = getString(R.string.app_name)
        }
    }
}