package be.ap.student.mobiledev_rently.util
import be.ap.student.mobiledev_rently.dataClasses.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


class FireBaseCommunication {
    private val users = FirebaseFirestore.getInstance().collection("/users")
    private val db = Firebase.firestore
    suspend fun writeNewUser(user: User) : User?{
        return if (getUser(user.getEmail()) == null){
            db.collection("users").document()
                .set(user)
            user
        } else null
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
//    suspend fun getItem(id: String?): Item? {
//        if (id == null) return null
//        try {
//            val task = items.document(id).get()
//            task.await()
//            if (task.result.exists()) return null
//            val result = task.result
//            return Item(email, result.get("username").toString(),
//                result.get("password").toString(), null )
//        } catch (e: Exception) {
//            throw e
//        }
//    }

    suspend fun changePassword(email: String?){


    }
}