package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize

@Parcelize
class Item() : Parcelable {
    private var title: String? = null
    private var category: String? = null
    private var description: String? = null
    private var image: String? = null
    private var location: GeoPoint? = null
    private var ownerReference: String? = null
    constructor(title: String, category: String, description: String, image: String, location: GeoPoint, ownerReference: String) : this() {
        this.title = title

    }


}