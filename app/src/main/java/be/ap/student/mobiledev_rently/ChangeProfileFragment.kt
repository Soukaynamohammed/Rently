package be.ap.student.mobiledev_rently

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangeProfileBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.System.out


class ChangeProfileFragment : Fragment() {
    private var user: User? = null
    private lateinit var binding: FragmentChangeProfileBinding
    private var selectedImageUri: Uri? = null
    private val storageRef = Firebase.storage.reference
    private var isUploadInProgress = false


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
        val imageView: ImageView = binding.imageView
        val changeImageButton: Button = binding.changeProfilePicButton

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
            nameEditText.setText(it.getUsername())
            emailEditText.setText(it.getEmail())

            if (it.getImageUrl() != "") {
                loadImageFromFirebase(it.getImageUrl()!!, imageView)
                Glide.with(this)
                    .load(it.getImageUrl())
                    .placeholder(R.drawable.default_character)
                    .into(imageView)
            }
        }


        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageView.setImageURI(it)
                uploadImage(it)
            }
        }


        changeImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }



        saveButton.setOnClickListener {
            if (isUploadInProgress) {
                Log.e("SaveProfile", "Image upload is still in progress. Please wait.")
                Toast.makeText(requireContext(), "Image upload is in progress. Please wait.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            user?.let {
                val oldEmail = it.getEmail()
                it.setUsername(nameEditText.text.toString())
                it.setEmail(emailEditText.text.toString())

                updateUserInFirebase(it, oldEmail)
            }

            val profileFragment = ProfileFragment.newInstance(user)
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, profileFragment)
                .addToBackStack(null)
                .commit()
        }

        return view;
    }



    private fun uploadImageToFirebase(uri: Uri, callback: (String) -> Unit) {
        val fileName = "profile_images/${user?.getEmail()}.jpg"
        val fileRef = storageRef.child(fileName)
        isUploadInProgress = true

        fileRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("FirebaseUpload", "Image upload successful: $fileName")
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("FirebaseUpload", "Download URL: $downloadUri")
                    user?.setImageUrl(downloadUri.toString())
                    isUploadInProgress = false
                    // Save image locally after successful upload
                    saveImageLocally(fileName) { localUri ->
                        if (localUri != null) {
                            Log.d("LocalSave", "Image saved locally at: $localUri")
                            isUploadInProgress = false
                        }
                    }

                    callback(downloadUri.toString()) // Pass the download URL to the callback
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Failed to get download URL: ${exception.message}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUpload", "Image upload failed: ${exception.message}")
            }


    }


    private fun uploadImage(uri: Uri) {
        val fileName = "profile_images/${user?.getEmail()}.jpg"
        val fileRef = storageRef.child(fileName)
        isUploadInProgress = true // Mark upload as in progress

        val compressedImage = compressImage(uri);

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                Log.d("FirebaseUpload", "Image upload successful: $fileName")
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("FirebaseUpload", "Download URL: $downloadUri")
                    user?.setImageUrl(downloadUri.toString()) // Save image URL to user
                    isUploadInProgress = false // Mark upload as complete
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Failed to get download URL: ${exception.message}")
                    isUploadInProgress = false // Mark upload as complete (failed)

                    Glide.with(this)
                        .load(compressedImage.toString())
                        .placeholder(R.drawable.default_character)
                        .into(binding.imageView)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUpload", "Image upload failed: ${exception.message}")
                isUploadInProgress = false // Mark upload as complete (failed)
            }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Compress to 50% quality
        return outputStream.toByteArray()
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

    private fun loadImageFromFirebase(imageUrl: String, imageView: ImageView) {
        val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bitmap)
        }.addOnFailureListener { exception ->
            Log.e("FirebaseDownload", "Failed to load image: ${exception.message}")
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