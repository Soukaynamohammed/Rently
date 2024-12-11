package be.ap.student.mobiledev_rently

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentAllBookingsBinding


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

        user?.let {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MyBookingsFragment.newInstance(it))
                .addToBackStack(null)
                .commit()

            setActiveButton(myBookingsButton, true)
            setActiveButton(rentalsButton, false)
        }

        myBookingsButton.setOnClickListener{
            Log.d("emailallbookingsrentals",user.toString())

            user?.let {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, MyBookingsFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()

                setActiveButton(myBookingsButton, true)
                setActiveButton(rentalsButton, false)
            }
        }

        rentalsButton.setOnClickListener{
            val rentalsFragment = RentalsFragment.newInstance(user)
            user?.let {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, rentalsFragment)
                    .addToBackStack(null)
                    .commit()

                setActiveButton(myBookingsButton, false)
                setActiveButton(rentalsButton, true)
            }
        }

        return view
    }

    // Helper function to change button color
    private fun setActiveButton(button: Button, isActive: Boolean) {
        if (isActive) {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)) // White color
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black)) // Black text
        } else {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)) // Default black color
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // White text
        }
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