package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import java.lang.Exception

class ElectionsViewModel(application: Application): ViewModel() {

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    val savedElections: LiveData<List<Election>>
        get() = database.electionDao.getSavedElections()

    private val _navigateToVoterInfo = MutableLiveData<Election>()
    val navigateToVoterInfo: LiveData<Election>
        get() = _navigateToVoterInfo

    private val database = ElectionDatabase.getInstance(application)

    init {
        populateUpcomingElections()
    }

    private fun populateUpcomingElections() {
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.getElections()
                val elections = result.elections
                _upcomingElections.value = elections
            } catch (e: Exception) {
            }
        }
    }

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfo.value = election
    }

    fun doneNavigating() {
        _navigateToVoterInfo.value = null
    }
}