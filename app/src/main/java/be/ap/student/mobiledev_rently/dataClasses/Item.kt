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
    private var owner: String? = null
    private var price: Double? = null
    private var startDate: String = ""
    private var endDate: String = ""
    constructor(title: String?, category: String?, description: String?, image: String?, location: GeoPoint?, owner: String?, price: Double?, startDate: String, endDate: String) : this() {
        this.title = title
        this.category = category
        this.description = description
        this.image = image
        this.location = location
        this.owner = owner
        this.price = price
        this.startDate = startDate
        this.endDate = endDate
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
    fun getOwner(): String?{
        return owner
    }
    fun setOwner(owner: String){
        this.owner = owner
    }
    fun getPrice(): Double?{
        return price
    }
    fun setPrice(price: Double) {
        this.price = price
    }
    fun getStartDate(): String? {
        return startDate
    }
    fun setStartDate(startDate: String){
        this.startDate = startDate
    }
    fun getEndDate(): String? {
        return endDate
    }
    fun setEndDate(endDate: String){
        this.endDate = endDate
    }

    companion object : Parceler<Item> {
        override fun create(parcel: Parcel): Item {
            val title = parcel.readString()
            val category = parcel.readString()
            val description = parcel.readString()
            val image = parcel.readString()
            val location = GeoPoint(parcel.readDouble(), parcel.readDouble())
            val owner = parcel.readString()
            val price = parcel.readDouble()
            val startDate = parcel.readString()?:""
            val endDate = parcel.readString()?:""
            return Item(title, category, description, image, location, owner, price, startDate, endDate)
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
            parcel.writeString(owner)
            price?.let { parcel.writeDouble(it) }
            parcel.writeString(startDate)
            parcel.writeString(endDate)
        }

    }

}