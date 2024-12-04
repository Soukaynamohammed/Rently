package be.ap.student.mobiledev_rently

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate


class MyItemDetailFragment : Fragment() {
    private lateinit var binding: FragmentMyItemDetailBinding
    private var item: Item? = null

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
        binding = FragmentMyItemDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val description: TextView = binding.description
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val startDateEdit: Button = binding.startDateButton
        val endDateEdit: Button = binding.endDateButton
        val editButton: Button = binding.edit


        item?.let {
            title.text = it.getTitle()
            price.text = "$ ${it.getPrice()}/day"
            category.text = it.getCategory()
            description.text = it.getDescription()
            startDate.text = it.getStartDate().toString()
            endDate.text = it.getEndDate().toString()

            if (!it.getImage().isNullOrEmpty()) {
                loadImageFromFirebase(it.getImage()!!, imageView)
                Glide.with(this)
                    .load(it.getImage())
                    .placeholder(R.drawable.default_character)
                    .into(imageView)
            }
            else{
                imageView.setImageResource(R.drawable.default_character) // Fallback image
            }
        }
        editButton.setOnClickListener {
            item?.let {

                parentFragmentManager.beginTransaction()
                    .add(R.id.container, MyItemDetailEditFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()
            }
        }

        startDateEdit.setOnClickListener{
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.show(parentFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                // formatting date in dd-mm-yyyy format.
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
                item?.setStartDate(LocalDate.parse(dateFormatter.format(it)))
                var id: String? = null
                runBlocking {
                    launch {
                        id = item?.let { it1 -> FireBaseCommunication().getItemId(it1) }
                    }
                }

                item?.let { it1 -> id?.let { it2 -> FireBaseCommunication().updateItem(it1, it2) } }
            }
        }
        endDateEdit.setOnClickListener{
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.show(parentFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                // formatting date in dd-mm-yyyy format.
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
                item?.setEndDate(LocalDate.parse(dateFormatter.format(it)))
            }
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
}