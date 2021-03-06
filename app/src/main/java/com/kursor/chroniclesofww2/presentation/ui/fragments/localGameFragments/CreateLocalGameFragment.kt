package com.kursor.chroniclesofww2.presentation.ui.fragments.localGameFragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.kursor.chroniclesofww2.R
import com.kursor.chroniclesofww2.connection.local.LocalServer
import com.kursor.chroniclesofww2.Settings
import com.kursor.chroniclesofww2.presentation.ui.fragments.abstractGameFragment.CreateAbstractGameFragment
import com.kursor.chroniclesofww2.viewModels.BattleViewModel
import com.kursor.chroniclesofww2.viewModels.GameDataViewModel
import org.koin.android.ext.android.inject

class CreateLocalGameFragment : CreateAbstractGameFragment() {


    override val actionToBattleChooseFragmentId =
        R.id.action_createLocalGameFragment_to_battleChooseFragment
    override val battleViewModel by navGraphViewModels<BattleViewModel>(R.id.navigation_local_game)
    override val gameDataViewModel by viewModels<GameDataViewModel>()

    override fun initServer() {
        if (gameDataJson.isBlank()) return
        server = LocalServer(
            requireActivity(),
            settings.username,
            binding.hostPasswordEditText.text.toString(),
            serverListener
        )
        server.startListening()
        isHostReady = true
    }

    override fun checkConditionsForServerInit(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }


}