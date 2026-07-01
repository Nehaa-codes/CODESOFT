package com.example.quoteapp

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.quoteapp.fragment.CategoriesFragment
import com.example.quoteapp.fragment.FavoritesFragment
import com.example.quoteapp.fragment.HomeFragment
import com.example.quoteapp.fragment.SharedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_favorites -> loadFragment(FavoritesFragment())
                R.id.nav_shared -> loadFragment(SharedFragment())
                R.id.nav_categories -> loadFragment(CategoriesFragment())
            }
            true
        }

        onBackPressedDispatcher.addCallback(this) {
            if (bottomNav.selectedItemId != R.id.nav_home) {
                bottomNav.selectedItemId = R.id.nav_home
            } else {
                finish()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}