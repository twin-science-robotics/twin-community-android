package com.twinscience.twin.lite.android.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by mertselcukdemir on 29.03.2019
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
object ScreenUtils {
    fun setFullScreen(activity: Activity) {
        val visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (activity.window.decorView.systemUiVisibility != visibility) {
            activity.window.decorView.systemUiVisibility = visibility
        }
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }

        imm.let { it.hideSoftInputFromWindow(view.windowToken, 0) }
        //Arrange this later
    }
}
