package com.teckatecka.netadmin.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:       ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        val topLevelDestinations = setOf(
            R.id.scannerFragment,
            R.id.diagnosticsFragment,
            R.id.wifiFragment,
            R.id.calculatorFragment,
            R.id.moreFragment
        )
        val appBarConfig = AppBarConfiguration(topLevelDestinations)

        binding.toolbar.setupWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)
    }
}
