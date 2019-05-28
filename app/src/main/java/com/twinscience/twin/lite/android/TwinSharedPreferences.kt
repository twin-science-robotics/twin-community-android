package com.twinscience.twin.lite.android

import android.content.Context

/**
 * Created by mertselcukdemir on 4.11.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
class TwinSharedPreferences {


    companion object {
        const val PREF: String = "PREF"
        const val IS_WARNED_ORIENTATION: String = "IS_WARNED_ORIENTATION"


        fun saveString(context: Context, key: String, path: String) {
            val sharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(key, path).apply()
        }

        fun loadString(context: Context, key: String, defValue: String): String? {
            val sharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, defValue)
        }

        fun saveBoolean(context: Context, key: String, mode: String, path: Boolean) {
            val sharedPreferences = context.getSharedPreferences(mode, Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(key, path).apply()

        }

        fun loadBoolean(context: Context, key: String, mode: String, defValue: Boolean): Boolean {
            val sharedPreferences = context.getSharedPreferences(mode, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(key, defValue)
        }
    }
}
