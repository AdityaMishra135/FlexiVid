package com.maurya.flexivid

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.maurya.flexivid.databinding.ActivityMainBinding
import com.maurya.flexivid.fragments.FoldersFragment
import com.maurya.flexivid.fragments.VideosFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        navController.navigate(R.id.videosFragment)

        binding.bottomNavMainActivity.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Any = when (item.itemId) {
                R.id.foldersBottomNav -> navController.navigate(R.id.foldersFragment)
                R.id.videosBottomNav -> navController.navigate(R.id.videosFragment)
                else -> navController.navigate(R.id.videosFragment)
            }

            true
        }


    }
}