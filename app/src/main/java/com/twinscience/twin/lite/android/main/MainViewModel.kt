package com.twinscience.twin.lite.android.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.dialog.SettingsDialogFragment
import com.twinscience.twin.lite.android.project.def.ProjectDef
import com.twinscience.twin.lite.android.viewmodel.BaseViewModel

import java.util.*
import javax.inject.Inject

/**
 * Created by mertselcukdemir on 21.11.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
class MainViewModel @Inject constructor() : BaseViewModel() {
    var selectedProject: ProjectModel? = null

    /**
     * Checks Bluetooth & Location Services
     */
    fun checkServicesForCoding(projectModel: ProjectModel, activity: MainActivity) {
        val isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled

        if (!isBluetoothEnabled && !activity.gpsHelper.isGPSenabled) {
            showSettingsDialog(activity, "Bluetooth")
        } else if (isBluetoothEnabled && !activity.gpsHelper.isGPSenabled) {
            showSettingsDialog(activity, "Location")
        } else if (!isBluetoothEnabled && activity.gpsHelper.isGPSenabled) {
            showSettingsDialog(activity, "Bluetooth")
        } else {
            selectedProject = projectModel
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1)
        }
    }


    /**
     * This method shows alert dialogs for intent to settings to enable services like bluetooth & location
     */
    fun showSettingsDialog(activity: MainActivity, tag: String) {
        val fm = activity.supportFragmentManager
        val fragment = SettingsDialogFragment.newInstance(tag)
        fragment.show(fm, tag)

        fragment.onResult = { isSettingsSelected ->
            if (isSettingsSelected) {
                when (tag) {
                    "Bluetooth" -> {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        activity.startActivity(intent)
                    }
                    "Location" -> {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        activity.startActivity(intent)
                    }
                }
            }
        }
    }


}