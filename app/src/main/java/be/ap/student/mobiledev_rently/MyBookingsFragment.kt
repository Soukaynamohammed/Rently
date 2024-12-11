package be.ap.student.mobiledev_rently

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import be.ap.student.mobiledev_rently.adapter.MyBookingsAdapter
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingsBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyBookingsFragment : Fragment() {
    private lateinit var binding: FragmentMyBookingsBinding
    var user: User? = null
    private val adapter: MyBookingsAdapter by lazy {
        MyBookingsAdapter(parentFragmentManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
            Log.d("MyBookingsFragment", "User: ${user.toString()}")

        }
        requireNotNull(user) { "User object must be provided to MyBookingsFragment" }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.recyclerViewBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBookings.adapter = adapter

        fetchBookings()

        return view
    }

    private fun fetchBookings() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("emailuser",user.toString())
            val bookings = FireBaseCommunication().getBookingsByRentee(user!!.getEmail().toString())

            bookings.forEach { booking ->
                val itemId = booking.getItem()
                Log.d("tag", "fetchBookings: $itemId")
                if (!itemId.isNullOrEmpty()) {
                    val item = FireBaseCommunication().getItemByReference(itemId)
                    item?.let {
                        booking.setItemName(it.getTitle())
                        booking.setItemImage(it.getImage())
                    }
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                adapter.submitList(bookings)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User?) =
            MyBookingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }
}