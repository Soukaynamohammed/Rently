package be.ap.student.mobiledev_rently.util
import android.util.Log
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.getField
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.LinkedList


class FireBaseCommunication {
    private val users = FirebaseFirestore.getInstance().collection("/users")
    private val items = FirebaseFirestore.getInstance().collection("/items")
    private val bookings = FirebaseFirestore.getInstance().collection("/boekingen")
    private val db = Firebase.firestore
    fun writeNewUser(user: User): User{
        db.collection("users").document()
            .set(user)
        return user
    }

    suspend fun getUserID(email: String): String? {
        try {
            val task = users.whereEqualTo("email", email).get()
            task.await()
            if (task.result.size() == 0) return null
            val result = task.result.documents[0]
            return result.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUser(email: String?): User? {
        if (email == null) return null
        try {
            val task = users.whereEqualTo("email", email).get()
            task.await()
            if (task.result.size() == 0) return null
            val result = task.result.documents[0]
            val location = result.getGeoPoint("location")
            return User(email, result.get("username").toString(), result.get("password").toString(),
                result.getGeoPoint("location"), result.getString("imageUrl") )

        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun updateUser(user: User, oldEmail: String?){
        try {
            val task = users.whereEqualTo("email", oldEmail).get()
            task.await()
            if (task.result.size() == 0) return
            val result = task.result.documents[0]
            val id = result.id
            users.document(id).set(user)
            return
        } catch (e: Exception) {
            throw e
        }

    }
    suspend fun getItemById(id: String): Item? {
        try {
            val task = items.document(id).get()
            task.await()
            if (task.result.exists()) return null
            val result = task.result
            return Item(result.getString("title"), result.getString("category"),
                result.getString("description"), result.getString("image"),
                result.getGeoPoint("location"), result.getString("owner") ,
                result.getDouble("price"), result.getString("startDate")?:"" ,
                result.getString("endDate")?:"")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getItems(): List<Item> {
        try {
            val task = items.get()
            task.await()
            val items = LinkedList<Item>()
            if (task.result.size() == 0) return items
            for(item in task.result) {
                items.add(Item(item.getString("title"), item.getString("category"),
                    item.getString("description"), item.getString("image"),
                    item.getGeoPoint("location"), item.getString("owner"),
                    item.getDouble("price"), item.getString("startDate")?:"",
                    item.getString("endDate")?:""))
            }
            return items
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun getItemsByCategory(category: String): List<Item> {
        try {
            val task = items.whereEqualTo("category", category).get()
            task.await()
            val items = LinkedList<Item>()
            if (task.result.size() == 0) return items
            for(item in task.result) {
                items.add(Item(item.getString("title"), item.getString("category"),
                    item.getString("description"), item.getString("image"),
                    item.getGeoPoint("location"), item.getString("owner") ,
                    item.getDouble("price"), item.getString("startDate")?:"",
                    item.getString("endDate")?:""))
            }
            return items
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun getItemsByUser(userId: String): List<Item> {
        try {
            val task = items.whereEqualTo("owner", "/users/${userId}").get()

            task.await()
            val items = LinkedList<Item>()
            if (task.result.size() == 0) return items
            for(item in task.result) {
                val startDate= item.getString("startDate")?:""
                val endDate= item.getString("endDate")?:""
                items.add(Item(item.getString("title"), item.getString("category"),
                    item.getString("description"), item.getString("image"),
                    item.getGeoPoint("location"), item.getString("owner"),
                    item.getDouble("price"), startDate, endDate))
            }
            return items
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun getItemId(item: Item): String?{
        val task = items.whereEqualTo("title", item.getTitle()).whereEqualTo("category", item.getCategory()).whereEqualTo("description", item.getDescription()).whereEqualTo("price", item.getPrice()).get()
        task.await()
        if (task.result.size() == 0) return null
        val result = task.result.documents[0]
        return result.id
    }

    fun updateItem(item: Item, id: String): Item{
        db.collection("items").document(id)
            .set(item)
        return item
    }
//
//    fun writeNewItem(item: Item): Item{
//        db.collection("items").document()
//            .set(item)
//        return item
//    }

    fun writeNewItem(item: Item, callback: (Boolean, String?) -> Unit) {
        db.collection("items").add(item) // Use `add` to automatically generate a document ID
            .addOnSuccessListener { documentReference ->
                Log.d("FirebaseWrite", "Item successfully added with ID: ${documentReference.id}")
                callback(true, documentReference.id) // Return success and document ID
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseWrite", "Failed to add item: ${exception.message}")
                callback(false, exception.message) // Return failure and error message
            }
    }



    suspend fun getBookingsYourItems(userId: String): List<Booking>{
        try {
            val task = bookings.whereEqualTo("owner", "users/${userId}").get()
            task.await()
            val bookings = LinkedList<Booking>()
            if (task.result.size() == 0) return bookings
            for(booking in task.result) {
                bookings.add(Booking(
                    StateType.valueOf(booking.getString("title").toString()), booking.getString("startDate"),
                    booking.getString("endDate"), booking.getField("messages"),
                    booking.getString("owner"), booking.getString("rentee"),
                    booking.getString("item")))
            }
            return bookings
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getImage(downloadPath: String): String? {
        return try {
            // Get a reference to the file in Firebase Storage
            val fileRef = FirebaseStorage.getInstance().reference.child(downloadPath)

            // Fetch the download URL (this suspends until the URL is retrieved)
            val url = fileRef.downloadUrl.await()
            url.toString() // Return the download URL as a string
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if an error occurs
        }
    }
    suspend fun getBookingsByItem(item: Item): List<Booking>{
        try {
            val itemId = getItemId(item) ?: return emptyList()
            val task = bookings.whereEqualTo("item", "/items/${itemId}").get()
            task.await()
            val bookings = LinkedList<Booking>()
            if (task.result.size() == 0) return bookings
            task.result.forEach {
                bookings.add(it.toObject(Booking::class.java))
            }
            return bookings
        } catch (e: Exception) {
            throw e
        }
    }
}