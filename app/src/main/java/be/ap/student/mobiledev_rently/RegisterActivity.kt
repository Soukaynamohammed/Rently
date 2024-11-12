package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.RegisterScreenBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RegisterActivity: AppCompatActivity(){
    private lateinit var binding: RegisterScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        binding.register.setOnClickListener{
            if (binding.email.text != null && binding.password.text != null && binding.username.text != null){
                val email = binding.email.text.toString().trim()
                val password = binding.password.text.toString().trim()
                val username = binding.username.text.toString().trim()
                val user = User(email, username, password, null)
                var result: User? = null
                runBlocking {
                    launch{
                        result = FireBaseCommunication().writeNewUser(user)
                    }.join()
                }
                if (result != null){
                    val menuIntent = android.content.Intent(this, MenuActivity::class.java)
                    startActivity(menuIntent)
                }
            }
            val snackbar = Snackbar.make(view, "please fill in all the fields", Snackbar.LENGTH_SHORT)
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.error))
            snackbar.show()
        }
    }
}