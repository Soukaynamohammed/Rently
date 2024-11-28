package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangeProfileBinding
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var user: User? = null

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
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.root

        val usernameTextView: TextView = binding.usernameValue
        val emailTextView: TextView = binding.emailValue
        val editButton: Button = binding.edit
        val changePassButton : Button = binding.changePassword


        user?.let {
            usernameTextView.text = it.getUsername()
            emailTextView.text = it.getEmail()
        }


        editButton.setOnClickListener {
            user?.let {

                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ChangeProfileFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()


            }
        }

        changePassButton.setOnClickListener{
            user?.let {
//                Toast.makeText(requireContext(), "Change password button clicked", Toast.LENGTH_SHORT).show()

                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ChangePasswordFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()


            }
        }





        return view

    }

    companion object {
        @JvmStatic
        fun newInstance(user: User?) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }


}