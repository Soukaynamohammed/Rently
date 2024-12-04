package be.ap.student.mobiledev_rently

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.RegisterScreenBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream

class RegisterActivity: AppCompatActivity() {
    private lateinit var binding: RegisterScreenBinding
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference
    private var isUploadInProgress = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageView.setImageURI(it)
                selectedImageUri = it
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        val imageView: ImageView = binding.imageView
        val changeImageButton: Button = binding.addProfilePicButton


        changeImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        supportActionBar?.hide()


        binding.register.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val username = binding.username.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if (isUploadInProgress) {
                    Snackbar.make(
                        binding.root,
                        "Image upload is in progress. Please wait.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (selectedImageUri != null) {
                    uploadImageToFirebase(selectedImageUri!!) { imageUrl ->
                        saveUserToFirebase(email, username, password, imageUrl)
                    }
                } else {
                    saveUserToFirebase(email, username, password, null)
                }
            } else {
                Snackbar.make(binding.root, "Please fill in all the fields", Snackbar.LENGTH_SHORT)
                    .setTextColor(ContextCompat.getColor(this, R.color.error))
                    .show()
            }
        }

    }

    private fun uploadImageToFirebase(uri: Uri, callback: (String?) -> Unit) {
        val fileName = "profile_images/${binding.email.text.toString().trim()}.jpg"
        val fileRef = storageRef.child(fileName)
        isUploadInProgress = true

        val compressedImage = compressImage(uri)

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    isUploadInProgress = false
                    callback(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    isUploadInProgress = false
                    Log.e("FirebaseUpload", "Failed to get download URL: ${exception.message}")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                isUploadInProgress = false
                Log.e("FirebaseUpload", "Image upload failed: ${exception.message}")
                callback(null)
            }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }

    private fun saveUserToFirebase(
        email: String,
        username: String,
        password: String,
        imageUrl: String?
    ) {
        val user = User(email, username, password, null, imageUrl.orEmpty())

        user.encrypt()
        var result: User? = null

        runBlocking {
            launch {
                result = FireBaseCommunication().writeNewUser(user)
            }.join()
        }

        if (result != null) {
            val menuIntent = Intent(this, MenuActivity::class.java)
            menuIntent.putExtra("user", user)
            startActivity(menuIntent)
        } else {
            Snackbar.make(
                binding.root,
                "There is already a user with this email",
                Snackbar.LENGTH_SHORT
            )
                .setTextColor(ContextCompat.getColor(this, R.color.error))
                .show()
        }


    }
    // Function to open the gallery and select an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the result from the image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            // Set the selected image to the ImageView
            binding.imageView.setImageURI(selectedImageUri)
        }
    }
}