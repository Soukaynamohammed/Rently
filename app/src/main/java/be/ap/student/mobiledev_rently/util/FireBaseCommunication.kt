package be.ap.student.mobiledev_rently.util
import be.ap.student.mobiledev_rently.dataClasses.Booking
import be.ap.student.mobiledev_rently.dataClasses.Item
import be.ap.student.mobiledev_rently.dataClasses.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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
    suspend fun getUser(email: String?): User? {
        if (email == null) return null
        try {
            val task = users.whereEqualTo("email", email).get()
            task.await()
            if (task.result.size() == 0) return null
            val result = task.result.documents[0]
            return User(email, result.getString("username"), result.getString("password"),
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
                result.getGeoPoint("location"), result.getString("ownerReference") )
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
                    item.getGeoPoint("location"), item.getString("ownerReference") ))
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
                    item.getGeoPoint("location"), item.getString("ownerReference") ))
            }
            return items
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun getItemsByUser(userId: String): List<Item> {
        try {
            val task = items.whereEqualTo("ownerReference", "users/${userId}").get()
            task.await()
            val items = LinkedList<Item>()
            if (task.result.size() == 0) return items
            for(item in task.result) {
                items.add(Item(item.getString("title"), item.getString("category"),
                    item.getString("description"), item.getString("image"),
                    item.getGeoPoint("location"), item.getString("ownerReference") ))
            }
            return items
        } catch (e: Exception) {
            throw e
        }
    }

    fun writeNewItem(item: Item): Item{
        db.collection("items").document()
            .set(item)
        return item
    }
    suspend fun getBookingsYourItems(userId: String): List<Booking>{
        try {
            val task = bookings.whereEqualTo("owner", "users/${userId}").get()
            task.await()
            val bookings = LinkedList<Booking>()
            if (task.result.size() == 0) return bookings
            for(booking in task.result) {
                bookings.add(Booking(
                    StateType.valueOf(booking.getString("title").toString()), LocalDate.parse(booking.getString("startDate")),
                    LocalDate.parse(booking.getString("endDate")), booking.getField("messages"),
                    booking.getString("owner"), booking.getString("rentee"),
                    booking.getString("item")))
            }
            return bookings
        } catch (e: Exception) {
            throw e
        }
    }
}