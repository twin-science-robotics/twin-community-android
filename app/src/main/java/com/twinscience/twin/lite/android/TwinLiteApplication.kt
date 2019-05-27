package com.twinscience.twin.lite.android

import android.app.Application
import android.content.Context
import com.twinscience.twin.lite.android.di.AppComponent
import com.twinscience.twin.lite.android.di.AppModule
import com.twinscience.twin.lite.android.di.DaggerAppComponent


/**
 * Created by mertselcukdemir on 9.10.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
class TwinLiteApplication : Application() {
    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

    }

    companion object {
        fun getAppComponent(context: Context): AppComponent? {
            return (context.applicationContext as TwinLiteApplication).component
        }
    }

}