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
import be.ap.student.mobiledev_rently.databinding.FragmentSearchBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import android.text.Editable
import android.text.TextWatcher
import be.ap.student.mobiledev_rently.dataClasses.Item


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var firebaseCommunication: FireBaseCommunication
    private lateinit var adapter: SearchAdapter
    private var distance: Int = 20
    private var userId: String? = null
    private var itemList = listOf<Item>() // Store the original list of items
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
            runBlocking {
                launch {
                    getLocation()
                }.join()
            }
        }
        firebaseCommunication = FireBaseCommunication()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        user?.let {
            lifecycleScope.launch {
                userId = firebaseCommunication.getUserID(user!!.getEmail().toString())
                if (userId != null) {
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

        adapter = SearchAdapter(parentFragmentManager)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Listen for slider changes
        binding.slider.addOnChangeListener { _, value, _ ->
            binding.sliderValue.text = "${value.toInt()} km"
            distance = value.toInt()
            filterItems(binding.searchInput.text.toString(), binding.searchCategoryInput.text.toString())
        }

        // **Listen for text changes in the search input (product name)**
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString(), binding.searchCategoryInput.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // **Listen for text changes in the search input (category)**
        binding.searchCategoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(binding.searchInput.text.toString(), s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        loadItems()
    }

    private fun loadItems() {
        lifecycleScope.launch {
            try {
                // Get the full list of items (this will be filtered later)
                itemList = firebaseCommunication.getItemsSearchItems(userId.toString())
                filterItems(binding.searchInput.text.toString(), binding.searchCategoryInput.text.toString())
            } catch (e: Exception) {
                Log.e("LoadItems", "Error loading items: ${e.message}")
            }
        }
    }


    private fun filterItems(productQuery: String, categoryQuery: String) {
        val filteredList = itemList.filter { item ->
            val matchesSearchQuery = item.getTitle()!!.contains(productQuery, ignoreCase = true)
            val matchesCategoryQuery = item.getCategory()!!.contains(categoryQuery, ignoreCase = true)
            val isWithinDistance = compare(getDistanceBetweenPoints(item.getLocation()!!, user?.getLocation()!!), distance.toDouble())

            matchesSearchQuery && matchesCategoryQuery && isWithinDistance
        }
        adapter.submitList(filteredList)
    }

    private fun compare(distance: Double?, maxDistance: Double?): Boolean {
        return distance != null && maxDistance != null && distance <= maxDistance
    }

    private fun getDistanceBetweenPoints(point1: GeoPoint, point2: GeoPoint): Double {
        val result = FloatArray(1)
        Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, result)
        return (result[0] / 1000).toDouble()
    }

    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (this.context?.let {
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } != PackageManager.PERMISSION_GRANTED && this.context?.let {
                ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    user?.setLocation(GeoPoint(latitude, longitude))
                }
            }
            .addOnFailureListener { exception: Exception ->
                Log.e("SearchFragment", "Error getting location: ${exception.message}")
            }
    }

    companion object {
        fun newInstance(user: User?) = SearchFragment().apply {
            arguments = Bundle().apply {
                putParcelable("user", user)
            }
        }
    }
}
