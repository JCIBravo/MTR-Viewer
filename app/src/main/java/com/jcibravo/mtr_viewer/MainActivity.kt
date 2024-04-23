package com.jcibravo.mtr_viewer

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcibravo.mtr_viewer.classes.MTRAddress
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

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

        //Load host files
        val filePath = File(applicationContext.filesDir, "hosts.json")
        if (filePath.exists()) {
            val json = FileInputStream(filePath).use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }

            if (json.isNotEmpty()) {
                val addresses: MutableList<MTRAddress> = gson.fromJson(json, object : TypeToken<MutableList<MTRAddress>>() {}.type)
                savedAddresses.addAll(addresses)
            }
        } else filePath.createNewFile()
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