package com.example.android.politicalpreparedness.election

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class VoterInfoViewModel(val election: Election,
                         private val dataSource: ElectionDao) : ViewModel() {

    private lateinit var voterInfo: VoterInfoResponse

    private val _electionInfoUrl = MutableLiveData<String>()
    val electionInfoUrl: LiveData<String>
        get() = _electionInfoUrl
    private val _ballotInfoUrl = MutableLiveData<String>()
    val ballotInfoUrl: LiveData<String>
        get() = _ballotInfoUrl

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean>
        get() = _isSaved

    init {
        checkIsSaved()
        populateVoterInfo()
    }

    fun onElectionInfoClick() {
        _electionInfoUrl.value = voterInfo.state?.get(0)?.electionAdministrationBody?.electionInfoUrl
    }

    fun onBallotInfoClick() {
        _ballotInfoUrl.value = voterInfo.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
    }

    private fun populateVoterInfo() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val id = election.id.toString()
                val country = election.division.country

                var state: String = election.division.state
                if (state.isNullOrEmpty() || state.isBlank()) {
                    state = "ny"
                }

                val address = "state:${state}/country:${country}"

                try {
                    voterInfo = CivicsApi.retrofitService.getVoterInfo(address, id)
                } catch (e: Exception) {
                    Log.d("VoterInfoViewModel", e.message.toString())
                }
            }
        }
    }

    private fun checkIsSaved() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val savedElection = dataSource.getElectionById(election.id)
                if (savedElection == null) {
                    _isSaved.postValue(false)
                } else {
                    _isSaved.postValue(true)
                }
            }
        }
    }

    fun saveElection(election: Election) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataSource.insertElection(election)
                _isSaved.postValue(true)
            }
        }
    }

    fun deleteElection(election: Election) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dataSource.deleteElectionById(election.id)
                _isSaved.postValue(false)
            }
        }
    }

}