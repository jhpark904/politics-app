package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.setNewValue
import com.google.android.gms.location.LocationServices
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this).get(RepresentativeViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding: FragmentRepresentativeBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_representative, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val representativeAdapter = RepresentativeListAdapter()
        binding.representativeList.adapter = representativeAdapter

        binding.buttonLocation.setOnClickListener {
            getLocation()
        }

        binding.buttonSearch.setOnClickListener {
            val line1 = binding.addressLine1.text?.toString()
            var line2 = binding.addressLine2.text.toString()
            val city = binding.city.text?.toString()
            val zip = binding.zip.text?.toString()
            val state = binding.state.selectedItem.toString()

            if (line1.isNullOrEmpty() || city.isNullOrEmpty()
                || zip.isNullOrEmpty()) {
                Toast.makeText(context, "Please fill in all the required fields",
                Toast.LENGTH_SHORT).show()
            } else {
                viewModel.getAddress(Address(line1, line2, city, state, zip))
            }
        }

        viewModel.address.observe(viewLifecycleOwner, Observer {
            binding.addressLine1.setText(it.line1)
            binding.addressLine2.setText(it.line2)
            binding.city.setText(it.city)
            binding.state.setNewValue(it.state)
            binding.zip.setText(it.zip)

            viewModel.fetchRepresentatives(it)
        })

        viewModel.representatives.observe(viewLifecycleOwner, Observer {
            representativeAdapter.submitList(it)
        })

        return binding.root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
            false
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkLocationPermissions()) {
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext())
            val locationResult = fusedLocationClient.lastLocation

            locationResult.addOnCompleteListener {
                if (it.result != null && it.isSuccessful) {
                    viewModel.getAddress(geoCodeLocation(it.result!!))
                } else {
                    Toast.makeText(
                        context,
                        "Could not retrieve address from location...", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}