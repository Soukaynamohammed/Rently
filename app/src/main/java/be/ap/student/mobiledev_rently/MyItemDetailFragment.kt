package be.ap.student.mobiledev_rently

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class MyItemDetailFragment : Fragment() {
    private lateinit var binding: FragmentMyItemDetailBinding
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
                imageView.setImageResource(R.drawable.default_item) // Fallback image
            }
        }


        editButton.setOnClickListener {
            item?.let {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, MyItemDetailEditFragment.newInstance(it))
                    .addToBackStack(null)
                    .commit()
            }
        }

        startDateEdit.setOnClickListener{
            // Create a DatePickerDialog
            val datePickerDialog = this.context?.let { it1 ->
                DatePickerDialog(
                    it1, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        // Create a new Calendar instance to hold the selected date
                        val selectedDate = Calendar.getInstance()
                        // Set the selected date using the values received from the DatePicker dialog
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
                        // Update the TextView to display the selected date with the "Selected Date: " prefix
                        startDate.text = item?.getStartDate().toString()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            // Show the DatePicker dialog
            datePickerDialog?.show()
        }

        endDateEdit.setOnClickListener{
            // Create a DatePickerDialog
            val datePickerDialog = this.context?.let { it1 ->
                DatePickerDialog(
                    it1, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        // Create a new Calendar instance to hold the selected date
                        val selectedDate = Calendar.getInstance()
                        // Set the selected date using the values received from the DatePicker dialog
                        selectedDate.set(year, monthOfYear, dayOfMonth)
                        // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        // Format the selected date into a string
                        var itemId: String? = null
                        runBlocking {
                            launch {
                                itemId = item?.let { it2 -> FireBaseCommunication().getItemId(it2) }
                            }
                                .join()
                        }

                        item?.setEndDate(LocalDate.of(year, monthOfYear, dayOfMonth).toString())
                        itemId?.let { it2 -> FireBaseCommunication().updateItem(item!!, it2) }
                        // Update the TextView to display the selected date with the "Selected Date: " prefix
                        endDate.text = item?.getEndDate().toString()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            // Show the DatePicker dialog
            datePickerDialog?.show()
        }

        binding.delete.setOnClickListener {
            basicAlert()
        }

        return view

    }
    private fun basicAlert(){

        val builder = AlertDialog.Builder(this.context)

        with(builder)
        {
            setTitle("RENTLY")
            setMessage("Are you sure you want to delete this item?")
            setPositiveButton("YES", DialogInterface.OnClickListener(positiveButtonClick))
            setNegativeButton("NO", DialogInterface.OnClickListener(negativeButtonClick))
            show()
        }
    }
    private val positiveButtonClick = { _: DialogInterface, _: Int ->
        runBlocking {
            launch {
                item?.let { it1 -> FireBaseCommunication().deleteItem(FireBaseCommunication().getItemId(it1)) }
            }
        }
        parentFragmentManager.popBackStack()
    }
    private val negativeButtonClick = { _: DialogInterface, _: Int ->
        Toast.makeText(requireContext(), "You cancelled deleting the item", Toast.LENGTH_LONG).show()
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
            MyItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", item)
                }
            }
    }
}