package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentItemDetailBinding
import be.ap.student.mobiledev_rently.databinding.FragmentRentalsBinding


class RentalDetailFragment : Fragment() {
    private lateinit var binding: FragmentRentalsBinding
    var booking: Booking? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking = it.getParcelable("booking", Booking::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRentalsBinding.inflate(inflater, container, false)
        val view = binding.root


        return view
    }


    companion object {
        @JvmStatic
        fun newInstance(booking: Booking?) =
            MyBookingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("booking", booking)
                }
            }
    }


}