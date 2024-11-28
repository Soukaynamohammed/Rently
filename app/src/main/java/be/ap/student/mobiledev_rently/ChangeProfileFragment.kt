package be.ap.student.mobiledev_rently

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContract
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangeProfileBinding
import androidx.activity.result.contract.ActivityResultContracts
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * A simple [Fragment] subclass.
 * Use the [ChangeProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChangeProfileFragment : Fragment() {
    private var user: User? = null
    private lateinit var binding: FragmentChangeProfileBinding
    private var selectedImageUri: Uri? = null


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
        binding = FragmentChangeProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val nameEditText: EditText = binding.usernameValue2
        val emailEditText: EditText = binding.emailValue2
        val saveButton:  Button = binding.saveButton
        val imageUrl: ImageView = binding.imageView;
        val imageView: ImageView = binding.imageView

        user?.let {
            nameEditText.setText(it.getUsername())
            emailEditText.setText(it.getEmail())
        }

        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ){ uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imageView.setImageURI(uri)
            }
        }

        imageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            user?.let {

                val oldEmail: String? = it.getEmail();

                it.setUsername(nameEditText.text.toString())
                it.setEmail(emailEditText.text.toString())
                it.setImageUrl(imageUrl.sourceLayoutResId.toString())


                runBlocking {
                    launch {
                        FireBaseCommunication().updateUser(it, oldEmail)
                    }.join()
                }
            }

            val profileFragment = ProfileFragment.newInstance(user)
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, profileFragment)
                .addToBackStack(null)
                .commit()

        }

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