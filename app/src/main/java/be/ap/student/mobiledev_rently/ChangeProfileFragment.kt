package be.ap.student.mobiledev_rently

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.LocationXml
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentChangeProfileBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File


class ChangeProfileFragment : Fragment() {
    private var user: User? = null
    private lateinit var binding: FragmentChangeProfileBinding
    private val storageRef = Firebase.storage.reference
    private var isUploadInProgress = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: GeoPoint? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

            if (it.getLocation() != null) {
                location = it.getLocation()
                runBlocking {
                    launch(Dispatchers.IO) {
                        getLocationReverse(location!!)
                    }
                }
            }
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

        binding.locationButton.setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity as Activity)
            if (ActivityCompat.checkSelfPermission(
                    this.context as Context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.context as Context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location = GeoPoint(it.latitude, it.longitude)
                runBlocking {
                    launch(Dispatchers.IO) {
                        getLocationReverse(location!!)
                    }.join()
                }

            }
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
                if (location != null) {
                    user?.setLocation(location!!)
                } else if( binding.location.text.toString() != ""){
                    runBlocking {
                        launch(Dispatchers.IO) {
                            user?.setLocation(getLocation(binding.location.text.toString()))
                        }
                            .join()
                    }
                }
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

    private fun getLocationReverse(location: GeoPoint){
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://nominatim.openstreetmap.org/reverse?lat=${location.latitude}&lon=${location.longitude}&zoom=18")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Rently/1.0")
            .build()
        try{
            val response: Response = client.newCall(request).execute()
            val model = parseAs<LocationXml>(response.body?.string().toString())
            binding.location.setText("${model.addressparts?.city?:""}, ${model.addressparts?.road?:""} ${model.addressparts?.number?:""}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inline fun <reified T : Any> parseAs(resource: String): T {
        return kotlinXmlMapper.readValue(resource)
    }

    private val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }).registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


    private fun getLocation(address: String): GeoPoint {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://nominatim.openstreetmap.org/search?format=json&q=$address")
            .method("GET", null)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Rently/1.0")
            .build()
        try{
            val response: Response = client.newCall(request).execute()

            val mapper = jacksonObjectMapper()
            val model: List<Map<String, Any>> = mapper.readValue(response.body?.string().toString())
            Log.d("tag", "getLocation: lat = ${model[0]["lat"]}, lon = ${model[0]["lon"]}")
            return GeoPoint(model[0]["lat"].toString().toDouble(), model[0]["lon"].toString().toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
            return GeoPoint(0.0, 0.0)
        }
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