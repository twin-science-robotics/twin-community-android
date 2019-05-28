package com.twinscience.twin.lite.android.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.TwinLiteApplication
import com.twinscience.twin.lite.android.utils.GPSHelper
import com.twinscience.twin.lite.android.viewmodel.ViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var factory: ViewModelFactory
    lateinit var gpsHelper: GPSHelper
    val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TwinLiteApplication.getAppComponent(this)?.inject(this)
    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()
}