package com.kursor.chroniclesofww2.presentation.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kursor.chroniclesofww2.objects.Const.game.BATTLE
import com.kursor.chroniclesofww2.objects.Moshi
import com.kursor.chroniclesofww2.R
import com.kursor.chroniclesofww2.data.repositories.battleRepositories.LocalCustomBattleRepository
import com.kursor.chroniclesofww2.data.repositories.battleRepositories.StandardBattleRepository
import com.kursor.chroniclesofww2.databinding.FragmentBattleChooseBinding
import com.kursor.chroniclesofww2.model.data.Battle
import com.kursor.chroniclesofww2.presentation.adapters.BattleAdapter
import com.kursor.chroniclesofww2.presentation.ui.fragments.abstractGameFragment.CreateAbstractGameFragment.Companion.BATTLE_REQUEST_CODE
import com.kursor.chroniclesofww2.viewModels.BattleListViewModel
import com.kursor.chroniclesofww2.viewModels.BattleViewModel
import com.phelat.navigationresult.BundleFragment
import com.phelat.navigationresult.navigateUp
import org.koin.android.ext.android.inject

class BattleChooseFragment : BundleFragment() {

    lateinit var binding: FragmentBattleChooseBinding

    private val standardBattleRepository by inject<StandardBattleRepository>()
    private val localCustomBattleRepository by inject<LocalCustomBattleRepository>()
    //private val remoteCustomBattleRepository by inject<RemoteCustomBattleRepository>()

    var navigationGraphId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationGraphId = requireArguments().getInt(NAVIGATION_GRAPH_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBattleChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val battleViewModel by navGraphViewModels<BattleViewModel>(navigationGraphId!!)

        binding.battleRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val battleListViewModel by viewModels<BattleListViewModel>()
        battleListViewModel.getBattleLiveData().observe(viewLifecycleOwner) { newBattleList ->
            binding.battleRecyclerView.adapter =
                BattleAdapter(requireActivity(), newBattleList).apply {
                    setOnItemClickListener { view, position, battle ->
                        navigateUpWithBattle(battle, battleViewModel)
                    }
                }
        }

        battleListViewModel.changeDataSource(standardBattleRepository)
        binding.defaultMissionButton.setOnClickListener {
            navigateUpWithBattle(standardBattleRepository.defaultBattle(), battleViewModel)
        }
        binding.customMissionButton.setOnClickListener {
            navigate(
                R.id.action_battleChooseFragment2_to_createNewBattleDialogFragment2,
                BATTLE_REQUEST_CODE
            )
        }

        binding.standardBattlesButton.setOnClickListener {
            battleListViewModel.changeDataSource(standardBattleRepository)
        }
        binding.localCustomBattlesButton.setOnClickListener {
            battleListViewModel.changeDataSource(localCustomBattleRepository)
        }
        binding.remoteCustomBattlesButton.setOnClickListener {
            //battleListViewModel.changeDataSource(remoteCustomBattleRepository)
        }
    }

    private fun navigateUpWithBattle(battle: Battle, battleViewModel: BattleViewModel) {
        battleViewModel.battleLiveData.value = battle
        requireActivity().onBackPressed()
    }

    companion object {
        const val MISSION_FRAGMENT = "MISSION_FRAGMENT"

        const val CUSTOM_MISSION_REQUEST_CODE = 505

        const val NAVIGATION_GRAPH_ID = "navigation graph id"
    }

}