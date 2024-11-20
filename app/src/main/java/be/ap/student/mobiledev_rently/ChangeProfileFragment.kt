package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import be.ap.student.mobiledev_rently.dataClasses.User

/**
 * A simple [Fragment] subclass.
 * Use the [ChangeProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangeProfileFragment : Fragment() {
    private var user: User? = null
//    private lateinit var binding;


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

        val view = inflater.inflate(R.layout.fragment_change_profile, container, false)
        val nameEditText: EditText = view.findViewById(R.id.usernameValue2)
        val emailEditText: EditText = view.findViewById(R.id.emailValue2)
        val saveButton:  Button = view.findViewById(R.id.saveButton)

        user?.let {
            nameEditText.setText(it.getUsername()) // Replace 'name' with the actual property in your User class
            emailEditText.setText(it.getEmail()) // Replace 'email' with the actual property in your User class
        }

        //TODO : IMAGE UPLOAD MAKEN

//        saveButton.setOnClickListener(
//
//        );

        return view;
    }


    companion object {
        @JvmStatic
        fun newInstance(user: User) =
            ChangeProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }
}