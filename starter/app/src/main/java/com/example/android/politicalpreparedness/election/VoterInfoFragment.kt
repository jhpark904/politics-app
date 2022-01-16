package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val args = VoterInfoFragmentArgs.fromBundle(requireArguments())
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        val dataSource = ElectionDatabase.getInstance(activity.application).electionDao
        val viewModel = ViewModelProvider(this,
            VoterInfoViewModelFactory(args.election, dataSource))
            .get(VoterInfoViewModel::class.java)

        val binding: FragmentVoterInfoBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_voter_info, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.isSaved.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.followButton.text = getString(R.string.unfollow)
                binding.followButton.setOnClickListener {
                    viewModel.deleteElection(args.election)
                }
            } else {
                binding.followButton.text = getString(R.string.follow)
                binding.followButton.setOnClickListener {
                    viewModel.saveElection(args.election)
                }
            }
        })

        viewModel.electionInfoUrl.observe(viewLifecycleOwner, Observer {
            startWebIntent(it)
        })

        viewModel.ballotInfoUrl.observe(viewLifecycleOwner, Observer {
            startWebIntent(it)
        })

        return binding.root
    }

    private fun startWebIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}