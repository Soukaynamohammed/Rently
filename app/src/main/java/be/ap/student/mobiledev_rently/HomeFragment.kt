package be.ap.student.mobiledev_rently

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.FragmentHomeBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem

class HomeFragment : Fragment() {
    private var user: User? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mapView: org.osmdroid.views.MapView
    private var location: GeoPoint? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity as Activity)
        val view = binding.root
        mapView = binding.map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        val controller = mapView.controller
        if (ActivityCompat.checkSelfPermission(
                this.context as Context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.context as Context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
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
            controller.setCenter(GeoPoint(location?.latitude ?: 0.0, location?.longitude ?: 0.0))
            addMarker(GeoPoint(location?.latitude ?: 0.0, location?.longitude ?: 0.0), "Here", R.drawable.me)
        }
        Configuration.getInstance().setUserAgentValue("github-mbridts-rently")
        controller.setZoom(10.0)
        var items: List<Item>? = null
        runBlocking {
            launch {
                items = FireBaseCommunication().getItemsSearchItems(user?.getEmail() ?: "")
            }
                .join()
        }
        items?.forEach {
            addMarker(GeoPoint(it.getLocation()?.latitude ?: 0.0, it.getLocation()?.longitude ?: 0.0), it.getTitle().toString(), R.drawable.marker)
        }
        Log.d("test", "onCreateView: $location")

        return view
    }

    private fun addMarker(g: GeoPoint, title: String, drawing: Int) {
        val myLocationOverlayItem = OverlayItem(title, "Current Position", g)
        val myCurrentLocationMarker: Drawable? = ResourcesCompat.getDrawable(
            resources, drawing, null
        )
        myLocationOverlayItem.setMarker(myCurrentLocationMarker)
        val items = ArrayList<OverlayItem>()
        items.add(myLocationOverlayItem)
        val mOverlay = ItemizedOverlayWithFocus(this.context, items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    Snackbar.make(binding.root, title, Snackbar.LENGTH_LONG).show()
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
        fun newInstance(user: User?) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }
    }
}