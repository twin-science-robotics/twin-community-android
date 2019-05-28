package com.twinscience.twin.lite.android.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.TwinLiteApplication
import com.twinscience.twin.lite.android.blockly.BlocklyActivity
import com.twinscience.twin.lite.android.utils.GPSHelper
import com.twinscience.twin.lite.android.utils.ScreenUtils
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
        gpsHelper = GPSHelper(this)
    }

    override fun onResume() {
        super.onResume()
        ScreenUtils.setFullScreen(this)
    }
    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_host_fragment).navigateUp()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // permission was granted, yay!
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, BlocklyActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable("selectedProject", mainViewModel.selectedProject)
                    intent.putExtras(bundle)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    this.startActivity(intent)
                    //Move this to view model


                } else {
                    // permission denied, boo!
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request

    }

}