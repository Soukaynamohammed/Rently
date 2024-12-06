package be.ap.student.mobiledev_rently

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import be.ap.student.mobiledev_rently.adapter.SearchAdapter
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding
import be.ap.student.mobiledev_rently.databinding.FragmentSearchBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var firebaseCommunication: FireBaseCommunication
    private val adapter = SearchAdapter()
    private var userId: String? = null

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
            runBlocking {
                launch{
                    getLocation()
                }
                    .join()
            }
        }
        firebaseCommunication = FireBaseCommunication()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.root

        user?.let {
            // Fetch user ID and load items after it's fetched
            lifecycleScope.launch {
                userId = firebaseCommunication.getUserID(user!!.getEmail().toString())
                if (userId != null) {
                    // Only load items after the userId is properly fetched
                    loadItems()
                } else {
                    Log.e("SearchFragment", "Failed to fetch userId")
                }
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadItems()
    }

    private fun loadItems() {
        lifecycleScope.launch {
            try {
                val items = FireBaseCommunication().getItemsSearchItems(userId.toString())
                adapter.submitList(items)
            } catch (e: Exception) {
                Log.e("LoadItems", "Error loading items: ${e.message}")
            }
        }
    }




    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (this.context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && this.context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In this place, we can only use the location object if it is non-null.
                if (location != null) {
                    // Use the location object (latitude, longitude, etc.)
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // ... do something with the location ...
                    user?.setLocation(GeoPoint(latitude, longitude))
                } else {
                    // Location is null, handle accordingly (e.g., request location updates)
                }
            }
            .addOnFailureListener { exception: Exception ->
                // Handle any errors that occurred while getting the location
                Log.e("HomeFragment", "Error getting location: ${exception.message}")
            }
    }

    companion object {
        fun newInstance(user: User?) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }
}
