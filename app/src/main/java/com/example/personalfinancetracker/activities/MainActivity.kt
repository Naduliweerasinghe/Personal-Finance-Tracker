package com.example.personalfinancetracker.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.fragments.AnalyticsFragment
import com.example.personalfinancetracker.fragments.DashboardFragment
import com.example.personalfinancetracker.fragments.SettingsFragment
import com.example.personalfinancetracker.fragments.TransactionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        setupBottomNavigation()

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment(), false)
        } else {
            // Restore the current fragment
            currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment(), true)
                    true
                }
                R.id.navigation_transactions -> {
                    loadFragment(TransactionFragment(), true)
                    true
                }
                R.id.navigation_analytics -> {
                    loadFragment(AnalyticsFragment(), true)
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment(), true)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        currentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}