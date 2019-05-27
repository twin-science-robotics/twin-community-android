package com.twinscience.twin.lite.android.utils

import android.content.Context

/**
 * Created by mertselcukdemir on 19.03.2019
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */

object ResUtils {
    /**
     * This utils used for resources. If you want to get path or string from file(should be under res/.. folders)
     * use this functions.
     */

    /** IMPORTANT
     * @param resLabel must be without file type(for example: remote_control.mp4 must be like "remote_control" ).
     * @param defType can be any directory under the res folder(for example: raw, drawable).
     */
    fun getIdentifierByResourceName(context: Context, resLabel: String, defType: String): Int {
        return context.resources.getIdentifier(resLabel, defType, context.packageName)
    }

    fun getResourcePathById(context: Context, resId: Int): String {
        return "android.resource://" + context.packageName + "/" + resId
    }

    fun getPathByResourceName(context: Context, resLabel: String, defType: String): String {
        val identifier = context.resources.getIdentifier(resLabel, defType, context.packageName)
        return "android.resource://" + context.packageName + "/" + identifier
    }
}