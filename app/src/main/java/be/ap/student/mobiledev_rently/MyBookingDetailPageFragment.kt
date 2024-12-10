package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingDetailPageBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingsBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailBinding

class MyBookingDetailPageFragment : Fragment() {
    private lateinit var binding: FragmentMyBookingDetailPageBinding
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
        binding = FragmentMyBookingDetailPageBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val status: TextView = binding.bookingState
        val description: TextView = binding.description
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val ownerImage: ImageView = binding.ownerImage
        val ownerName: TextView = binding.ownerName


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(booking: Booking?) =
            MyBookingDetailPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("booking", booking)
                }
            }
    }

}