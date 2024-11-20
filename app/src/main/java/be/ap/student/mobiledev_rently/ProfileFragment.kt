package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import be.ap.student.mobiledev_rently.dataClasses.User

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
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

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val usernameTextView: TextView = view.findViewById(R.id.usernameValue)
        val emailTextView: TextView = view.findViewById(R.id.emailValue)

        user?.let {
            usernameTextView.text = it.getUsername()
            emailTextView.text = it.getEmail()
        }

        val editButton: Button = view.findViewById(R.id.register)

        editButton.setOnClickListener {
            user?.let {
                val fragment = ChangeProfileFragment.newInstance(it)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.start_screen, fragment) // Replace 'R.id.container' with the correct container ID
                    .addToBackStack(null) // Add to back stack to allow back navigation
                    .commit()
            }
        }

        return view

    }


}