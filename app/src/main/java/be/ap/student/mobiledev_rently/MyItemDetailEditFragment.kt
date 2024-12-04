package be.ap.student.mobiledev_rently

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailEditBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream


class MyItemDetailEditFragment : Fragment() {
    private lateinit var binding: FragmentMyItemDetailEditBinding
    private var item: Item? = null
    private val storageRef = Firebase.storage.reference
    private var isUploadInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item", Item::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyItemDetailEditBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val description: TextView = binding.description
        val saveButton: Button = binding.save
        var itemId: String? = null

        if (item?.getImage()!!.isNotEmpty())
        {
            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(item?.getImage()!!)
            imageRef.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                imageView.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // todo Handle any errors
            }
        }


        item?.let {
            title.text = it.getTitle()
            price.text = it.getPrice().toString()
            category.text = it.getCategory()
            description.text = it.getDescription()
            runBlocking {
                launch {
                    itemId = FireBaseCommunication().getItemId(item!!)
                }
                    .join()
            }
            if (!it.getImage().isNullOrEmpty()) {
                loadImageFromFirebase(it.getImage()!!, imageView)
                Glide.with(this)
                    .load(it.getImage())
                    .placeholder(R.drawable.default_character)
                    .into(imageView)
            }
            else{
                imageView.setImageResource(R.drawable.default_character)
            }
        }
        imageView.setOnClickListener {
            val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    imageView.setImageURI(it)
                    uploadImage(it)
                }
            }
            pickImageLauncher.launch("image/*")
        }
        saveButton.setOnClickListener {
            itemId?.let { it1 -> FireBaseCommunication().updateItem(item!!, it1) }
        }

        return view
    }

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
    private fun uploadImage(uri: Uri) {
        val fileName = "profile_images/${FireBaseCommunication()}.jpg"
        val fileRef = storageRef.child(fileName)
        isUploadInProgress = true // Mark upload as in progress

        val compressedImage = compressImage(uri);

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                Log.d("FirebaseUpload", "Image upload successful: $fileName")
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("FirebaseUpload", "Download URL: $downloadUri")
                    item?.setImage(downloadUri.toString()) // Save image URL to user
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

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(item: Item) =
            MyItemDetailEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", item)
                }
            }
    }
}