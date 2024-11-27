package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable

class User () : Parcelable {
    private var email: String? = null
    private var username: String? = null
    private var password: String? = null
    private var location: Map<String,String>? = null
    private var imageUrl: String? = null


    constructor(parcel: Parcel) : this() {
        email = parcel.readString()
        username = parcel.readString()
        password = parcel.readString()
        imageUrl = parcel.readString();
    }

    constructor(email: String, username: String, password: String, location: Map<String,String>?, imageUrl: String?) : this() {
        this.email = email
        this.username = username
        this.password = password
        this.location = location
        this.imageUrl = imageUrl;
    }
    fun getEmail(): String? { return email}
    fun getUsername(): String? {return username}
    fun getPassword(): String? {return password}
    fun getLocation(): Map<String, String>? {return location}
    fun setEmail(email: String){this.email = email}
    fun setUsername(username: String){this.username = username}
    fun setPassword(password: String){this.password = password}
    fun setLocation(location: Map<String, String>?){this.location = location}
    fun getImageUrl(): String? { return imageUrl}
    fun setImageUrl(imageUrl: String) {this.imageUrl = imageUrl}

    fun toMap(): Map<String, Any?>{
        return mapOf(
            "email" to email,
            "username" to username,
            "password" to password,
            "location" to location
        )
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}