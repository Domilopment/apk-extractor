package domilopment.apkextractor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import domilopment.apkextractor.AppListAdapter
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.ListOfAPKs

class SplashScreenFragment : Fragment() {
    private lateinit var mainActivity: MainActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = (requireActivity() as MainActivity)

        // Set UI Mode
        SettingsManager(requireContext()).changeUIMode()
        // Start List Activity
        mainActivity.viewAdapter = AppListAdapter(mainActivity).also {
            it.updateData()
        }

        findNavController().navigate(R.id.action_splashScreenFragment_to_mainFragment)
    }
}
