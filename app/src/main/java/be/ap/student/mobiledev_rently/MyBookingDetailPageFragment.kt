package be.ap.student.mobiledev_rently

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.LocationXml
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingDetailPageBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyBookingsBinding
import be.ap.student.mobiledev_rently.databinding.FragmentMyItemDetailBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.bumptech.glide.Glide
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
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
import java.util.Calendar

class MyBookingDetailPageFragment : Fragment() {
    private lateinit var binding: FragmentMyBookingDetailPageBinding
    var booking: Booking? = null
    var item: Item? = null
    private val calendar = Calendar.getInstance()
    private lateinit var mapView: org.osmdroid.views.MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            booking = it.getParcelable("booking", Booking::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBookingDetailPageBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView: ImageView = binding.imageView
        val title: TextView = binding.title
        val price: TextView = binding.price
        val category: TextView = binding.category
        val status: TextView = binding.bookingState
        val description: TextView = binding.description
        val startDate: TextView = binding.startDate
        val endDate: TextView = binding.endDate
        val ownerImage: ImageView = binding.ownerImage
        val ownerName: TextView = binding.ownerName


        runBlocking {
            launch {
                val documentId = booking!!.getItem().toString().substringAfterLast("/")
                Log.d("ItemID", documentId)
                item = FireBaseCommunication().getItemById(documentId)

            }.join()
        }


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


        return view
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


