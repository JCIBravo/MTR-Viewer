package com.jcibravo.mtr_viewer

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private val connectionFragment = ConnectionFragment()
    private val stationFragment = StationFragment()
    private val routeListFragment = RouteListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadFragment(connectionFragment)

        val navigation = findViewById<BottomNavigationView>(R.id.nav_view)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            return@OnNavigationItemSelectedListener when (item.itemId) {
                R.id.navigation_timetable -> {
                    loadFragment(stationFragment)
                    true
                }

                R.id.navigation_routes -> {
                    loadFragment(routeListFragment)
                    true
                }

                R.id.navigation_connect -> {
                    loadFragment(connectionFragment)
                    true
                }

                else -> false
            }
        }

    private fun loadFragment(fragment: Fragment?) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment!!)
        transaction.commit()
    }
}