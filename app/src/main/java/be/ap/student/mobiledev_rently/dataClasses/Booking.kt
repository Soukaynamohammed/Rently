package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import be.ap.student.mobiledev_rently.util.StateType
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.LinkedList

@Parcelize
class Booking() : Parcelable {
    private var bookingState: StateType? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private var owner: String? = null
    private var rentee: String? = null
    private var item: String? = null
    private var itemImage: String? = null
    private var itemName: String? = null



    constructor(bookingState: StateType?, startDate: String?, endDate: String?, owner: String?, rentee: String?, item: String?, itemImage: String?,itemName: String? ) : this() {
        this.bookingState = bookingState
        this.startDate = startDate
        this.endDate = endDate
        this.owner = owner
        this.rentee = rentee
        this.item = item
        this.itemImage = itemImage
        this.itemName = itemName
    }

    fun getItemName(): String? {
        return itemName
    }

    fun setItemName(itemName: String?) {
        this.itemName = itemName
    }

    fun getItemImage(): String? {
        return itemImage
    }

    fun setItemImage(itemImage: String?) {
        this.itemImage = itemImage
    }
    fun getBookingState(): StateType? {
        return bookingState
    }
    fun setBookingState(bookingState: StateType){
        this.bookingState = bookingState
    }
    fun getStartDate(): String?{
        return startDate
    }
    fun setStartDate(startDate: String){
        this.startDate = startDate
    }
    fun getEndDate(): String?{
        return endDate
    }
    fun setEndDate(endDate: String){
        this.endDate = endDate
    }
    fun getOwner(): String?{
        return owner
    }
    fun setOwner(owner: String?){
        this.owner = owner
    }
    fun getRentee(): String?{
        return  rentee
    }
    fun setRentee(rentee: String?){
        this.rentee = rentee
    }
    fun getItem(): String?{
        return item
    }
    fun setItem(item: String?){
        this.item = item
    }

    companion object : Parceler<Booking> {
        override fun create(parcel: Parcel): Booking {
            val read = parcel.readString() ?: ""  // Avoid nullability issues
            val bookingState = StateType.valueOf(read)
            Log.d("booking", "create: $read")

            val startDate = parcel.readString() ?: ""
            val endDate = parcel.readString() ?: ""


            val owner = parcel.readString()
            val rentee = parcel.readString()
            val item = parcel.readString()

            // NEW: Read itemImage and itemName from the Parcel
            val itemImage = parcel.readString()
            val itemName = parcel.readString()

            return Booking(bookingState, startDate, endDate, owner, rentee, item, itemImage, itemName)
        }

        override fun Booking.write(parcel: Parcel, flags: Int) {
            // Write in the same order as you read in `create()`
            parcel.writeString(bookingState.toString())
            parcel.writeString(startDate)
            parcel.writeString(endDate)


            parcel.writeString(owner)
            parcel.writeString(rentee)
            parcel.writeString(item)

            // NEW: Write itemImage and itemName to the Parcel
            parcel.writeString(itemImage)
            parcel.writeString(itemName)
        }
    }

//    companion object : Parceler<Booking> {
//        override fun create(parcel: Parcel): Booking {
//            val read = parcel.readString().toString()
//            val bookingState = StateType.valueOf(read)
//            Log.d("booking", "create: $read")
//            val startDate = parcel.readString()?:""
//            val endDate = parcel.readString()?:""
//            val messages = parcel.readArray(
//                ClassLoader.getSystemClassLoader(),
//                Message::class.java
//            )?.toList()
//            val owner = parcel.readString()
//            val rentee = parcel.readString()
//            val item = parcel.readString()
//            return Booking(bookingState, startDate, endDate, messages, owner, rentee, item, null, null)
//        }
//
//
//
//        override fun Booking.write(parcel: Parcel, flags: Int) {
//            parcel.writeString(bookingState.toString())
//            parcel.writeString(startDate)
//            parcel.writeString(endDate)
//            parcel.writeArray(arrayOf(messages))
//            parcel.writeString(owner)
//            parcel.writeString(rentee)
//            parcel.writeString(item)
//            parcel.writeString(itemImage)
//            parcel.writeString(itemName)
//        }
//
//    }

}