package be.ap.student.mobiledev_rently

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import be.ap.student.mobiledev_rently.dataClasses.User
import be.ap.student.mobiledev_rently.databinding.MenuScreenBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: MenuScreenBinding
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = intent.extras?.getParcelable("user", User::class.java)
        binding = MenuScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadFragment(HomeFragment())
        supportActionBar?.hide()
        binding.bottomNavBar.selectedItemId = R.id.navigation_home
        binding.bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_search_item->{
                    //TODO: replace fragment
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_message->{
                    //TODO: replace fragment
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_home->{
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_my_items->{
                    //TODO: replace fragment
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_profile->{
                    loadFragment(ProfileFragment())
                    true
                }

                else -> {
                    loadFragment(HomeFragment())
                    true
                }
            }
        }
    }
    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
}