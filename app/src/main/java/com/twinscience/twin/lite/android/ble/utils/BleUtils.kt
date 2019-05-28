package com.twinscience.twin.lite.android.ble.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.dialog.utils.DialogUtils
import com.twinscience.twin.lite.android.utils.GPSHelper


/**
 * Created by mertselcukdemir on 14.04.2019
 * Copyright (c) 2019 YGA to present
 * All rights reserved.
 */
object BleUtils {

    /**
     * Check bluetooth & gps services
     */
    fun isDefaultServicesEnabled(activity: Activity): Boolean {
        val gpsHelper = GPSHelper(activity)
        val dialogUtils = DialogUtils()
        val isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled
        return if (!isBluetoothEnabled && !gpsHelper.isGPSenabled) {
            dialogUtils.showAlertWithButton(activity, activity.getString(R.string.title_bluetooth_settings), activity.getString(R.string.lbl_ok),null, false)
            false
        } else if (isBluetoothEnabled && !gpsHelper.isGPSenabled) {
            dialogUtils.showAlertWithButton(activity, activity.getString(R.string.title_location_settings), activity.getString(R.string.lbl_ok),null, false)
            false
        } else if (!isBluetoothEnabled && gpsHelper.isGPSenabled) {
            dialogUtils.showAlertWithButton(activity, activity.getString(R.string.title_bluetooth_settings), activity.getString(R.string.lbl_ok), null,false)
            false
        } else {
            true
        }
    }
}
