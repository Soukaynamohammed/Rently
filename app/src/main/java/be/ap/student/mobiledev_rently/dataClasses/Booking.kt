package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable
import be.ap.student.mobiledev_rently.util.StateType
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.util.LinkedList

@Parcelize
class Booking() : Parcelable {
    private var bookingState: StateType? = null
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var messages: List<Message>? = LinkedList<Message>()
    private var owner: String? = null
    private var rentee: String? = null
    private var item: String? = null
    constructor(bookingState: StateType?, startDate: LocalDate?, endDate: LocalDate?, messages: List<Message>?, owner: String?, rentee: String?, item: String?) : this() {
        this.bookingState = bookingState
        this.startDate = startDate
        this.endDate = endDate
        this.messages = messages
        this.owner = owner
        this.rentee = rentee
        this.item = item
    }
    fun getBookingState(): StateType? {
        return bookingState
    }
    fun setBookingState(bookingState: StateType){
        this.bookingState = bookingState
    }
    fun getStartDate(): LocalDate?{
        return startDate
    }
    fun setStartDate(startDate: LocalDate?){
        this.startDate = startDate
    }
    fun getEndDate(): LocalDate?{
        return endDate
    }
    fun setEndDate(endDate: LocalDate?){
        this.endDate = endDate
    }
    fun getMessages(): List<Message>?{
        return messages
    }
    fun setMessages(messages: List<Message>){
        this.messages = messages
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
            val bookingState = StateType.valueOf(parcel.readString().toString())
            val startDate = LocalDate.parse(parcel.readString())
            val endDate = LocalDate.parse(parcel.readString())
            val messages = parcel.readArray(
                ClassLoader.getSystemClassLoader(),
                Message::class.java
            )?.toList()
            val owner = parcel.readString()
            val rentee = parcel.readString()
            val item = parcel.readString()
            return Booking(bookingState, startDate, endDate, messages, owner, rentee, item)
        }

        override fun Booking.write(parcel: Parcel, flags: Int) {
            parcel.writeString(bookingState.toString())
            parcel.writeString(startDate.toString())
            parcel.writeString(endDate.toString())
            parcel.writeArray(arrayOf(messages))
            parcel.writeString(owner)
            parcel.writeString(rentee)
            parcel.writeString(item)
        }

    }

}