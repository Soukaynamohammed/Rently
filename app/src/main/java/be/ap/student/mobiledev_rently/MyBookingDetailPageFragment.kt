package be.ap.student.mobiledev_rently

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.LocationXml
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingDetailPageBinding
import be.ap.student.mobiledev_rently.util.BookingState
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.material.snackbar.Snackbar
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


class MyBookingDetailPageFragment : Fragment() {
    private lateinit var binding: FragmentMyBookingDetailPageBinding
    var booking: Booking? = null
    var item: Item? = null
    var user: User? = null
    private lateinit var mapView: org.osmdroid.views.MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking = it.getParcelable("booking", Booking::class.java)
        }
        requireNotNull(booking) { "Booking object must be provided to MyItemDetailPageFragment" }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyBookingDetailPageBinding.inflate(inflater, container, false)
        val view = binding.root
        var bookingId: String? = null
        mapView = binding.osmmap
        Glide.with(binding.imageView.context)
            .load(booking?.getItemImage() ?: R.drawable.default_item)
            .into(binding.imageView)
        binding.title.text = booking?.getItemName()
        runBlocking {
            launch(Dispatchers.IO) {
                item = FireBaseCommunication().getItemByReference(booking?.getItem()!!)
            }
            launch(Dispatchers.IO) {
                user = FireBaseCommunication().getUserByReference(booking?.getOwner()!!)
            }
            launch(Dispatchers.IO) {
                bookingId = FireBaseCommunication().getBookingId(booking!!)
            }
                .join()
        }
        binding.description.text = item?.getDescription()
        binding.price.text = "Price: $${item?.getPrice()}"
        binding.category.text = "Category: ${item?.getCategory()}"
        binding.bookingState.text = "State: ${booking?.getBookingState()}"
        binding.startDate.text = item?.getStartDate()
        binding.endDate.text = item?.getEndDate()
        binding.startDateBooking.text = booking?.getStartDate()
        binding.endDateBooking.text = booking?.getEndDate()
        Glide.with(binding.ownerImage.context)
            .load(user?.getImageUrl() ?: R.drawable.default_character)
            .into(binding.ownerImage)
        binding.ownerName.text = user?.getUsername()
        binding.cancelButton.setOnClickListener{
            booking!!.setBookingState(BookingState.CANCELLED)
            FireBaseCommunication().updateBooking(bookingId.toString(), booking!!)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, RentalsFragment.newInstance(user))
                .addToBackStack(null)
                .commit()
        }
        Configuration.getInstance().setUserAgentValue("github-mbridts-rently")
        binding.osmmap.setZoomLevel(15.0)
        binding.osmmap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmmap.controller.setCenter(GeoPoint(item?.getLocation()?.latitude?:0.0, item?.getLocation()?.longitude?:0.0))
        addMarker(GeoPoint(item?.getLocation()?.latitude?:0.0, item?.getLocation()?.longitude?:0.0))
        getLocationReverse(GeoPoint(item?.getLocation()?.latitude?:0.0, item?.getLocation()?.longitude?:0.0))
        return view
    }
    private fun addMarker(g: GeoPoint ) {
        val myLocationOverlayItem = OverlayItem("here", "Current Position", g)
        val myCurrentLocationMarker: Drawable? = ResourcesCompat.getDrawable(
            resources, R.drawable.marker, null
        )
        myLocationOverlayItem.setMarker(myCurrentLocationMarker)
        val items = ArrayList<OverlayItem>()
        items.add(myLocationOverlayItem)
        val mOverlay = ItemizedOverlayWithFocus(this.context, items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    Snackbar.make(binding.root, "here", Snackbar.LENGTH_LONG).show()
                    return true
                }
                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return true
                }
            })
        mapView.overlays.add(mOverlay)
        mapView.invalidate()
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


    companion object {
        @JvmStatic
        fun newInstance(booking: Booking?) =
            MyBookingDetailPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("booking", booking)
                }
            }
    }


}