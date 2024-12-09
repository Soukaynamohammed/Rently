package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding
import be.ap.student.mobiledev_rently.databinding.FragmentRentalsBinding

class RentalsFragment : Fragment() {
    private lateinit var binding: FragmentRentalsBinding
    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRentalsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.root

        return view
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