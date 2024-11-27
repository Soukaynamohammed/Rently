package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class User () : Parcelable{
    private var email: String? = null
    private var username: String? = null
    private var password: String? = null
    private var location: GeoPoint? = null
    private var imageUrl: String? = null

    constructor(email: String?, username: String?, password: String?, location: GeoPoint?, imageUrl: String?) : this() {
        this.email = email
        this.username = username
        this.password = password
        this.location = location
        this.imageUrl = imageUrl
    }
    fun getEmail(): String? { return email}
    fun getUsername(): String? {return username}
    fun getPassword(): String? {return password}
    fun getLocation(): GeoPoint? {return location}
    fun setEmail(email: String){this.email = email}
    fun setUsername(username: String){this.username = username}
    fun setPassword(password: String){this.password = password}
    fun setLocation(location: GeoPoint?){this.location = location}
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "email" to email,
            "username" to username,
            "password" to password,
            "location" to location,
            "imageUrl" to imageUrl
        )
    }

    private companion object : Parceler<User> {
        override fun create(parcel: Parcel): User {
            val email = parcel.readString()
            val username = parcel.readString()
            val password = parcel.readString()
            val location = GeoPoint(parcel.readDouble(), parcel.readDouble())
            val imageUrl = parcel.readString()
            return User(email, username, password, location, imageUrl)
        }

        override fun User.write(parcel: Parcel, flags: Int) {
            parcel.writeString(email)
            parcel.writeString(username)
            parcel.writeString(password)
            if(location != null){
                parcel.writeDouble(location!!.latitude)
                parcel.writeDouble(location!!.longitude)
            } else {
                parcel.writeDouble(0.0)
                parcel.writeDouble(0.0)
            }
            parcel.writeString(imageUrl)
        }
    }
}