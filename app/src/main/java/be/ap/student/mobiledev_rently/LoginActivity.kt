package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.LoginScreenBinding
import be.ap.student.mobiledev_rently.util.FireBaseCommunication
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity: AppCompatActivity(){
    private lateinit var binding: LoginScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        binding.login.setOnClickListener{
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            if (email != "" && password != ""){
                var user : User? = null
                runBlocking {
                    launch{
                        user = FireBaseCommunication().getUser(email)
                    }.join()
                }
                if(user?.getPassword() == User.md5(password)){
                    val menuIntent = android.content.Intent(this, MenuActivity::class.java)
                    menuIntent.putExtra("user", user)
                    startActivity(menuIntent)
                }else{
                    val snackbar = Snackbar.make(view, "the email or password is wrong", Snackbar.LENGTH_SHORT)
                    snackbar.setTextColor(ContextCompat.getColor(this, R.color.error))
                    snackbar.show()
                }
            }else{
                val snackbar = Snackbar.make(view, "please fill in all the fields", Snackbar.LENGTH_SHORT)
                snackbar.setTextColor(ContextCompat.getColor(this, R.color.error))
                snackbar.show()
            }
        }
    }
}