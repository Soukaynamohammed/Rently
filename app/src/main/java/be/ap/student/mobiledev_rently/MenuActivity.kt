package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import be.ap.student.mobiledev_rently.databinding.MenuScreenBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: MenuScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MenuScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
    }
}