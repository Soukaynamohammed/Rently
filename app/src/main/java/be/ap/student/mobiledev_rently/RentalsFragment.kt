package be.ap.student.mobiledev_rently

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import be.ap.student.mobiledev_rently.adapter.RentalsAdapter
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding
import be.ap.student.mobiledev_rently.databinding.FragmentRentalsBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RentalsFragment : Fragment() {
    private lateinit var binding: FragmentRentalsBinding
    var user: User? = null
    private val adapter: RentalsAdapter by lazy {
        RentalsAdapter(parentFragmentManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
            Log.d("RentalsFragment", "User: ${user.toString()}")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRentalsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.root


        binding.recyclerViewRentals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRentals.adapter = adapter

        fetchBookings()

        return view
    }

//    private fun fetchBookings() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val bookings = FireBaseCommunication().getBookingsByOwner(user!!.getEmail().toString())
//            bookings.forEach { booking ->
//                val itemId = booking.getItem()
//                if (!itemId.isNullOrEmpty()) {
//                    val item = FireBaseCommunication().getItemByReference(itemId)
//                }
//            }
//            CoroutineScope(Dispatchers.Main).launch {
//                adapter.submitList(bookings)
//            }
//        }
//    }

    private fun fetchBookings() {
        CoroutineScope(Dispatchers.IO).launch {
            user?.let {
                // Proceed only if user is not null
                val bookings = FireBaseCommunication().getBookingsByOwner(it.getEmail().toString())
                bookings.forEach { booking ->
                    val itemId = booking.getItem()
                    if (!itemId.isNullOrEmpty()) {
                        val item = FireBaseCommunication().getItemByReference(itemId)
                    }
                }
                // Update UI on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.submitList(bookings)
                }
            } ?: run {
                // Handle the case when user is null
                Log.e("MyBookingsFragment", "User object is null")
                // Show an error message or notify the user
            }
        }
    }




    companion object {
        @JvmStatic
        fun newInstance(user: User?) =
            RentalsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }

}