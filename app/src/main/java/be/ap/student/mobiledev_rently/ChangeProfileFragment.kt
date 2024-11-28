package be.ap.student.mobiledev_rently

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
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
    private val storageRef = Firebase.storage.reference


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
        val changeImageButton: Button = binding.changeProfilePicButton

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

        changeImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            user?.let {

                val oldEmail: String? = it.getEmail();

                it.setUsername(nameEditText.text.toString())
                it.setEmail(emailEditText.text.toString())
                it.setImageUrl(imageUrl.sourceLayoutResId.toString())

                selectedImageUri?.let { uri ->
                    uploadImageToFirebase(uri) { imageUrl ->
                        it.setImageUrl(imageUrl)
                        updateUserInFirebase(it, oldEmail)
                    }
                } ?: run {
                    updateUserInFirebase(it, oldEmail)
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

    // Upload Image to Firebase
    private fun uploadImageToFirebase(uri: Uri, callback: (String) -> Unit) {
        val fileName = "profile_images/${System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("FirebaseUpload", "Image upload successful: $fileName")
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("FirebaseUpload", "Download URL: $downloadUri")
                    callback(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Failed to get download URL: ${exception.message}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUpload", "Image upload failed: ${exception.message}")
            }
    }


    private fun updateUserInFirebase(user: User, oldEmail: String?) {
        runBlocking {
            launch {
                FireBaseCommunication().updateUser(user, oldEmail)
            }.join()
        }

        val profileFragment = ProfileFragment.newInstance(user)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, profileFragment)
            .addToBackStack(null)
            .commit()
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