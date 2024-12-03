package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class Item() : Parcelable {
    private var title: String? = null
    private var category: String? = null
    private var description: String? = null
    private var image: String? = null
    private var location: GeoPoint? = null
    private var ownerReference: String? = null
    constructor(title: String?, category: String?, description: String?, image: String?, location: GeoPoint?, ownerReference: String?) : this() {
        this.title = title
        this.category = category
        this.description = description
        this.image = image
        this.location = location
        this.ownerReference = ownerReference
    }
    fun getTitle(): String? {
        return title
    }
    fun setTitle(title: String?){
        this.title = title
    }
    fun getCategory(): String?{
        return category
    }
    fun setCategory(category: String?){
        this.category = category
    }
    fun getDescription(): String?{
        return description
    }
    fun setDescription(description: String?){
        this.description = description
    }
    fun getImage(): String?{
        return image
    }
    fun setImage(image: String?){
        this.image = image
    }
    fun getLocation(): GeoPoint?{
        return location
    }
    fun setLocation(location: GeoPoint?){
        this.location = location
    }
    fun getOwnerReference(): String?{
        return  ownerReference
    }
    fun setOwnerReference(ownerReference: String?){
        this.ownerReference = ownerReference
    }

    companion object : Parceler<Item> {
        override fun create(parcel: Parcel): Item {
            val title = parcel.readString()
            val category = parcel.readString()
            val description = parcel.readString()
            val image = parcel.readString()
            val location = GeoPoint(parcel.readDouble(), parcel.readDouble())
            val ownerReference = parcel.readString()
            return Item(title, category, description, image, location, ownerReference)
        }

        override fun Item.write(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(category)
            parcel.writeString(description)
            parcel.writeString(image)
            if (location != null) {
                parcel.writeDouble(location!!.latitude)
                parcel.writeDouble(location!!.longitude)
            } else {
                parcel.writeDouble(0.0)
                parcel.writeDouble(0.0)
            }
            parcel.writeString(ownerReference)
        }

    }

}