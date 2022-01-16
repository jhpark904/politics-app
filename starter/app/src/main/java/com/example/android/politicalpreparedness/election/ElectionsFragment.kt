package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.election.adapter.ElectionViewHolder

class ElectionsFragment: Fragment() {

    private val viewModel: ElectionsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, ElectionsViewModelFactory(activity.application))
            .get(ElectionsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding: FragmentElectionBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_election, container, false
        )
        binding.lifecycleOwner = this

        viewModel.navigateToVoterInfo.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        it
                    )
                )
                viewModel.doneNavigating()
            }
        })

        val upcomingAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })

        val savedAdapter = ElectionListAdapter(ElectionListener { election->
            viewModel.onElectionClicked(election)
        })

        binding.upcomingList.adapter = upcomingAdapter
        binding.savedList.adapter = savedAdapter

        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer {
            it.let {
                upcomingAdapter.submitList(it)
            }
        })
        
        viewModel.savedElections.observe(viewLifecycleOwner, Observer { 
            it.let {
                savedAdapter.submitList(it)
            }
        })

        return binding.root
    }
}