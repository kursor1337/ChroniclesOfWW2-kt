package com.kursor.chroniclesofww2.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kursor.chroniclesofww2.data.repositories.battleRepositories.BattleRepository
import com.kursor.chroniclesofww2.model.data.Battle

class BattleListViewModel : ViewModel() {

    private val battleLiveData = MutableLiveData<List<Battle>>()

    fun getBattleLiveData(): LiveData<List<Battle>> = battleLiveData

    fun changeDataSource(battleRepository: BattleRepository) {
        battleLiveData.value = battleRepository.battleList
    }

}