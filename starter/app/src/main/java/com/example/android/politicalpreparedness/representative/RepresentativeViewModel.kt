package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel: ViewModel() {

    val address: LiveData<Address>
        get() = _address
    private val _address = MutableLiveData<Address>()

    val representatives: LiveData<List<Representative>>
        get() = _representatives
    private val _representatives = MutableLiveData<List<Representative>>()

    fun getAddress(addrs: Address) {
        _address.value = addrs
    }

    fun fetchRepresentatives(addrs: Address) {
        viewModelScope.launch {
            val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(addrs.toFormattedString())
            _representatives.value = offices.flatMap { office ->
                office.getRepresentatives(officials)
            }
        }
    }
}
