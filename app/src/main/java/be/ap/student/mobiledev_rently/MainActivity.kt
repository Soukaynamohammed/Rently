package be.ap.student.mobiledev_rently

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import be.ap.student.mobiledev_rently.databinding.StartScreenBinding
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MainActivity : AppCompatActivity() {
    private lateinit var binding: StartScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StartScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        Firebase.initialize(this)
        binding.login.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
        binding.register.setOnClickListener{
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }
    }
}