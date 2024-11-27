package be.ap.student.mobiledev_rently

import com.google.android.material.snackbar.Snackbar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangePasswordBinding
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding

/**
 * A simple [Fragment] subclass.
 * Use the [ChangePasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
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
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        val password: TextView = binding.password
        val repeatPassword: TextView = binding.repeatPassword
        val saveButton: Button = binding.saveButton

        saveButton.setOnClickListener{
            user?.let {
                if (password.text.toString() == repeatPassword.text.toString()){
                    it.setPassword(password.text.toString())

                    val profileFragment = ProfileFragment.newInstance(user)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, profileFragment)
                        .addToBackStack(null)
                        .commit()
                }
                else{
                    val snackbar = Snackbar.make(view, "The passwords don't match, try again.", Snackbar.LENGTH_SHORT)
                    snackbar.setTextColor(ContextCompat.getColor(view.context, R.color.error))
                    snackbar.show()
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User?) =
            ChangePasswordFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }

}