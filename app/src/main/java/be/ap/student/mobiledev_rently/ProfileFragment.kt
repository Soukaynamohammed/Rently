package be.ap.student.mobiledev_rently

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangeProfileBinding
import be.ap.student.mobiledev_rently.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.File


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var user: User? = null
    private val storageRef = Firebase.storage.reference
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
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.root

        val usernameTextView: TextView = binding.usernameValue
        val emailTextView: TextView = binding.emailValue
        val editButton: Button = binding.edit
        val changePassButton : Button = binding.changePassword
        val imageView: ImageView = binding.imageView




        if (user?.getImageUrl()!!.isNotEmpty())
        {
            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(user?.getImageUrl()!!)
            imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                imageView.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // todo Handle any errors
            }
        }


        user?.let {
            usernameTextView.setText(it.getUsername())
            emailTextView.setText(it.getEmail())
            if (!it.getImageUrl().isNullOrEmpty()) {
                loadImageFromFirebase(it.getImageUrl()!!, imageView)
                Glide.with(this)
                    .load(it.getImageUrl())
                    .placeholder(R.drawable.default_character)
                    .into(imageView)
            }
            else{
                imageView.setImageResource(R.drawable.default_character) // Fallback image
            }
        }

        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ){ uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imageView.setImageURI(selectedImageUri)
            }
        }


        editButton.setOnClickListener {
            user?.let {

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, ChangeProfileFragment.newInstance(it))
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

//    private fun loadImageFromFirebase(imageUrl: String, imageView: ImageView) {
//        val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
//        imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
//            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//            imageView.setImageBitmap(bitmap)
//        }.addOnFailureListener { exception ->
//            Log.e("FirebaseDownload", "Failed to load image: ${exception.message}")
//        }
//    }

    private fun loadImageFromFirebase(imageUrl: String?, imageView: ImageView) {
        if (imageUrl.isNullOrEmpty()) {
            Log.e("LoadImage", "Image URL is null or empty.")
            imageView.setImageResource(R.drawable.default_character) // Set a default image
            return
        }

        val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bitmap)
        }.addOnFailureListener { exception ->
            Log.e("FirebaseDownload", "Failed to load image: ${exception.message}")
        }
    }


    private fun saveImageLocally(fileName: String, callback: (Uri?) -> Unit) {
        val localFile = File(requireContext().filesDir, "profile_${user?.getEmail()}.jpg")

        val fileName = "profile_images/${user?.getEmail()}.jpg"
        Log.d("FirebaseDebug", "Fetching file: $fileName")
        val fileRef = storageRef.child(fileName)


        fileRef.getFile(localFile)
            .addOnSuccessListener {
                Log.d("FirebaseDownload", "File downloaded to: ${localFile.absolutePath}")
                callback(Uri.fromFile(localFile)) // Return the local file's URI
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseDownload", "Failed to download file: ${exception.message}")
                callback(null) // Return null on failure
            }
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