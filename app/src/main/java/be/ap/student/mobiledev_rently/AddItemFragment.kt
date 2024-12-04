package be.ap.student.mobiledev_rently

import android.app.DatePickerDialog
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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentAddItemBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemsBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class AddItemFragment : Fragment() {
    private lateinit var binding: FragmentAddItemBinding
    private var user: User? = null
    private var userId: String? = null
    private var item: Item? = null
    private val storageRef = Firebase.storage.reference
    private var isUploadInProgress = false
    private lateinit var firebaseCommunication: FireBaseCommunication
    private val calendar = Calendar.getInstance()
    private var itemId: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("user", User::class.java)
        }
        firebaseCommunication = FireBaseCommunication()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddItemBinding.inflate(inflater, container, false)

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val description: TextView = binding.description
        val saveButton: Button = binding.save
        val addPictureButton: Button = binding.addPicturePicButton
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val startDateEdit: Button = binding.startDateButton
        val endDateEdit: Button = binding.endDateButton

        item = Item()

        user?.let {
            lifecycleScope.launch {
                userId = firebaseCommunication.getUserID(user!!.getEmail().toString())
            }

            runBlocking {
                launch {
                    itemId = FireBaseCommunication().getItemId(item!!)
                }
                    .join()
            }

        }




//        val userEmail = user?.getEmail()?.trim()
//        val currentItemId = itemId

//        if (userEmail.isNullOrEmpty()) {
//            Log.e("AddItemFragment", "User email is null or empty!")
//        } else {
//            Log.d("AddItemFragment", "User email: $userEmail")
//        }
//
//        if (currentItemId.isNullOrEmpty()) {
//            Log.e("AddItemFragment", "Item ID is null or empty!")
//        } else {
//            Log.d("AddItemFragment", "Item ID: $currentItemId")
//        }


        startDateEdit.setOnClickListener {
            // Create a DatePickerDialog for the end date
            val datePickerDialog = this.context?.let { context ->
                DatePickerDialog(
                    context, { _, year, monthOfYear, dayOfMonth ->
                        // Create a new Calendar instance and set the selected date
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)

                        // Convert selected date to a LocalDate string format
                        val formattedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toString() // Ensure correct month format

                        // Update the Item object with the selected end date
                        item?.setStartDate(formattedDate)

                        // Ensure the UI reflects the selected date in endDate TextView
                        startDate.text = formattedDate

                        // Optionally, update the item in Firebase if required
                        itemId?.let { id ->
                            FireBaseCommunication().updateItem(item!!, id)
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePickerDialog?.show()
        }


        endDateEdit.setOnClickListener {
            // Create a DatePickerDialog for the end date
            val datePickerDialog = this.context?.let { context ->
                DatePickerDialog(
                    context, { _, year, monthOfYear, dayOfMonth ->
                        // Create a new Calendar instance and set the selected date
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)

                        // Convert selected date to a LocalDate string format
                        val formattedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toString() // Ensure correct month format

                        // Update the Item object with the selected end date
                        item?.setEndDate(formattedDate)

                        // Ensure the UI reflects the selected date in endDate TextView
                        endDate.text = formattedDate

                        // Optionally, update the item in Firebase if required
                        itemId?.let { id ->
                            FireBaseCommunication().updateItem(item!!, id)
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePickerDialog?.show()
        }




        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageView.setImageURI(it)
                uploadImage(it)
            }
        }

        addPictureButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }



        saveButton.setOnClickListener {
            val updatedTitle = title.text.toString()
            val updatedPrice = price.text.toString().toDoubleOrNull() ?: 0.0
            val updatedCategory = category.text.toString()
            val updatedDescription = description.text.toString()

            // Update the Item instance
            item?.apply {
                setTitle(updatedTitle)
                setPrice(updatedPrice)
                setCategory(updatedCategory)
                setDescription(updatedDescription)
                setOwner(userId!!)
            }

            lifecycleScope.launch {
                firebaseCommunication.writeNewItem(item!!)

                val myItemsFragment = MyItemsFragment.newInstance(user)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, myItemsFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        saveButton.setOnClickListener {
            val updatedTitle = title.text.toString()
            val updatedPrice = price.text.toString().toDoubleOrNull() ?: 0.0 // Handle invalid input gracefully
            val updatedCategory = category.text.toString()
            val updatedDescription = description.text.toString()

            item?.apply {
                setTitle(updatedTitle)
                setPrice(updatedPrice)
                setCategory(updatedCategory)
                setDescription(updatedDescription)
            }

            itemId?.let { it1 ->
                FireBaseCommunication().updateItem(item!!, it1)

                val myItemsFragment = MyItemsFragment.newInstance(user)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, myItemsFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        //todo : add location permissions or someting
        val view = binding.root

        return view
    }


    private fun uploadImageToFirebase(uri: Uri, callback: (String?) -> Unit) {
        val fileName = "item_images/${FireBaseCommunication()}.jpg"
//        val fileName = "profile_images/${FireBaseCommunication()}.jpg"

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
        val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Compress to 50% quality
        return outputStream.toByteArray()
    }

    private fun uploadImage(uri: Uri) {
        val fileName = "item_images/${FireBaseCommunication()}.jpg"
//        val fileName = "profile_images/${FireBaseCommunication()}.jpg"

        val fileRef = storageRef.child(fileName)
        isUploadInProgress = true

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

    companion object {
        @JvmStatic
        fun newInstance(user: User?): AddItemFragment {
            val fragment = AddItemFragment()
            val args = Bundle()
            args.putParcelable("user", user)
            fragment.arguments = args
            return fragment
        }
    }
}