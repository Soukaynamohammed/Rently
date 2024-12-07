package be.ap.student.mobiledev_rently

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.FragmentItemDetailBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar


class ItemDetailFragment : Fragment() {
    private lateinit var binding: FragmentItemDetailBinding
    private var item: Item? = null
    private val calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item", Item::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val description: TextView = binding.description
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val pickupDateButton: Button = binding.pickupDateButton
        val pickupDate: TextView = binding.pickupDate

        item?.let {
            title.text = it.getTitle()
            price.text = "$ ${it.getPrice()} / day"
            category.text = "Category: ${it.getCategory()}"
            description.text = it.getDescription()

            startDate.text = it.getStartDate().toString()
            endDate.text = it.getEndDate().toString()

            if (!it.getImage().isNullOrEmpty()) {
                loadImageFromFirebase(it.getImage()!!, imageView)
                Glide.with(this)
                    .load(it.getImage())
                    .placeholder(R.drawable.default_item)
                    .into(imageView)
            }
            else{
                imageView.setImageResource(R.drawable.default_item)
            }
        }


        //todo omzetten naar boekingdatum opslaan ipv availibilitydatums
        pickupDateButton.setOnClickListener{
            val datePickerDialog = this.context?.let { it1 ->
                DatePickerDialog(
                    it1, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)
                        // Format the selected date into a string
                        var itemId: String? = null
                        runBlocking {
                            launch {
                                itemId = item?.let { it2 -> FireBaseCommunication().getItemId(it2) }
                            }
                                .join()
                        }

                        item?.setStartDate(LocalDate.of(year, monthOfYear, dayOfMonth).toString())
                        itemId?.let { it2 -> FireBaseCommunication().updateItem(item!!, it2) }

                        pickupDate.text = item?.getStartDate().toString()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePickerDialog?.show()
        }


        return view
    }

    private fun loadImageFromFirebase(imageUrl: String?, imageView: ImageView) {
        if (imageUrl.isNullOrEmpty()) {
            Log.e("LoadImage", "Image URL is null or empty.")
            imageView.setImageResource(R.drawable.default_item)
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

    companion object {
        @JvmStatic
        fun newInstance(item: Item) =
            ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", item)
                }
            }
    }
}