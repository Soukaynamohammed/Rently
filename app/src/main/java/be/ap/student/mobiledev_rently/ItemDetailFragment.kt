package be.ap.student.mobiledev_rently

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.LocationXml
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentItemDetailBinding
import be.ap.student.mobiledev_rently.util.BookingState
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem
import java.time.LocalDate
import java.util.Calendar


class ItemDetailFragment : Fragment() {
    private lateinit var binding: FragmentItemDetailBinding
    private var item: Item? = null
    private var user: User? = null
    private val calendar = Calendar.getInstance()
    private lateinit var mapView: org.osmdroid.views.MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it.getParcelable("item", Item::class.java)
            user = it.getParcelable("user", User::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val description: TextView = binding.description
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val startDateButton: Button = binding.startDateBookingButton
        val endDateButton: Button = binding.endDateBookingButton
        val startDateBooking: TextView = binding.startDateBooking
        val endDateBooking: TextView = binding.endDateBooking


        Configuration.getInstance().setUserAgentValue("github-mbridts-rently")
        mapView = binding.osmmap
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        val controller = mapView.controller
        controller.setZoom(15.0)
        controller.setCenter(GeoPoint(item?.getLocation()?.latitude ?: 0.0, item?.getLocation()?.longitude ?: 0.0))
        addMarker(GeoPoint(item?.getLocation()?.latitude ?: 0.0, item?.getLocation()?.longitude ?: 0.0))

        item?.let {
            title.text = it.getTitle()
            price.text = "$ ${it.getPrice()} / day"
            category.text = "Category: ${it.getCategory()}"
            description.text = it.getDescription()

            startDate.text = it.getStartDate()
            endDate.text = it.getEndDate()

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
        startDateButton.setOnClickListener{
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

                        startDateBooking.text = item?.getStartDate().toString()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePickerDialog?.show()
        }
        endDateButton.setOnClickListener{
            val datePickerDialog = this.context?.let { it1 ->
                DatePickerDialog(
                    it1, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)
                        var itemId: String? = null
                        runBlocking {
                            launch {
                                itemId = item?.let { it2 -> FireBaseCommunication().getItemId(it2) }
                            }
                                .join()
                        }

                        item?.setStartDate(LocalDate.of(year, monthOfYear, dayOfMonth).toString())
                        itemId?.let { it2 -> FireBaseCommunication().updateItem(item!!, it2) }

                        endDateBooking.text = item?.getStartDate().toString()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            datePickerDialog?.show()
        }
        binding.rentButton.setOnClickListener {
            val bookingState = BookingState.AWAITING
            val startDate = binding.startDateBooking.text.toString()
            val endDate = binding.endDateBooking.text.toString()
            val owner = item?.getOwner()
            var rentee = ""
            var itemId = ""
            runBlocking{
                launch(Dispatchers.IO){
                    rentee = FireBaseCommunication().getUserID(user?.getEmail().toString()).toString()
                    itemId = item?.let { it1 -> FireBaseCommunication().getItemId(it1) }.toString()
                }
            }

            val itemImage = item?.getImage()
            val itemName = item?.getTitle()
            val booking: Booking = Booking(bookingState, startDate, endDate, owner, rentee, itemId, itemImage, itemName)
            if (LocalDate.parse(booking.getStartDate()) >= LocalDate.parse(item?.getStartDate()) && LocalDate.parse(booking.getEndDate()) <= LocalDate.parse(item?.getEndDate())) {
                runBlocking {
                    launch(Dispatchers.IO) {
                        var bookings = FireBaseCommunication().getBookingsByItem(item!!)
                        var available = true
                        val bookingStartDate = LocalDate.parse(booking.getStartDate())
                        val bookingEndDate = LocalDate.parse(booking.getEndDate())
                        bookings = bookings.filter { it.getBookingState() == BookingState.ACCEPTED }
                        bookings.forEach{
                            if (!(bookingEndDate < LocalDate.parse(it.getStartDate())||LocalDate.parse(it.getEndDate()) < bookingStartDate)) {
                                available = false
                            }
                        }
                        if (available){
                            FireBaseCommunication().addBooking(booking)
                        }
                    }
                }
                parentFragmentManager.beginTransaction().replace(R.id.container, MyBookingsFragment.newInstance(user)).commit()
            } else {
                Snackbar.make(binding.root, "Item is not available", Snackbar.LENGTH_LONG).show()
            }

        }
        runBlocking {
            launch(Dispatchers.IO) {
                getLocationReverse(GeoPoint(item?.getLocation()?.latitude ?: 0.0, item?.getLocation()?.longitude ?: 0.0))
            }
        }
        return view
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
            Log.d("test", "getLocationReverse: $response")
            val model = parseAs<LocationXml>(response.body?.string().toString())
            Log.d("test", "getLocationReverse: $model")
            binding.city.text = model.addressparts?.city?:model.addressparts?.road?:""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }).registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private inline fun <reified T : Any> parseAs(resource: String): T {
        return kotlinXmlMapper.readValue(resource, T::class.java)
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

    private fun addMarker(g: GeoPoint) {
        val myLocationOverlayItem = OverlayItem("Here", "Current Position", g)
        val myCurrentLocationMarker: Drawable? = ResourcesCompat.getDrawable(
            resources, R.drawable.marker, null
        )
        myLocationOverlayItem.setMarker(myCurrentLocationMarker)
        val items = ArrayList<OverlayItem>()
        items.add(myLocationOverlayItem)
        val mOverlay = ItemizedOverlayWithFocus(this.context, items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    Snackbar.make(binding.root, "You tapped me!", Snackbar.LENGTH_LONG).show()
                    return true
                }
                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return true
                }
            })
        mapView.overlays.add(mOverlay)
        mapView.invalidate()
    }

    companion object {
        @JvmStatic
        fun newInstance(item: Item, user: User) =
            ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", item)
                    putParcelable("user", user)
                }
            }
    }
}