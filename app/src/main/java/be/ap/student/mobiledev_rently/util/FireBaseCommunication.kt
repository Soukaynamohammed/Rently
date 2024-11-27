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
            db.collection("users").document(user.getEmail()!!)
                .set(user)
            user
        } else null
    }
    suspend fun getUser(email: String?): User? {
        if (email == null) return email
        try {
            val documentQuery = users.document(email).get()
            val task: Task<DocumentSnapshot> = documentQuery
            task.await()
            val result = documentQuery.result
            if (result.get("username") == null) return null
            return User(email, result.get("username").toString(),
                result.get("password").toString(), null ,null)
        } catch (e: Exception) {
            throw e
        }
    }
}