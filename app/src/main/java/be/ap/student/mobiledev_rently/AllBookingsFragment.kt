package be.ap.student.mobiledev_rently

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentAllBookingsBinding
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding


class AllBookingsFragment : Fragment() {
    private lateinit var binding: FragmentAllBookingsBinding
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
        binding = FragmentAllBookingsBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.root

        val myBookingsButton: Button = binding.btnPage1
        val rentalsButton: Button = binding.btnPage2

        myBookingsButton.setOnClickListener{
            user?.let {
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, MyBookingsFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()
            }
        }

        rentalsButton.setOnClickListener{
            user?.let {
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, RentalsFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User?) =
            AllBookingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }

}