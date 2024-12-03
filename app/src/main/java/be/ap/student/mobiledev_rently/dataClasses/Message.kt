package be.ap.student.mobiledev_rently.dataClasses

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class Message() : Parcelable {
    var message :String? = null
    var sender :String? = null
    constructor(message: String?, sender: String?): this(){
        this.message = message
        this.sender = sender
    }
    @JvmName("getMessageMethod")
    fun getMessage(): String?{
        return message
    }
    @JvmName("setMessageMethod")
    fun setMessage(message: String){
        this.message = message
    }
    @JvmName("getSenderMethod")
    fun getSender(): String?{
        return sender
    }
    @JvmName("setSenderMethod")
    fun setSender(sender: String){
        this.sender = sender
    }
    companion object : Parceler<Message> {
        override fun create(parcel: Parcel): Message {
            val message = parcel.readString()
            val sender = parcel.readString()
            return Message(message, sender)
        }

        override fun Message.write(parcel: Parcel, flags: Int) {
            parcel.writeString(message)
            parcel.writeString(sender)
        }

    }
}