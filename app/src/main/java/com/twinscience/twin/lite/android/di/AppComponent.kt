package com.twinscience.twin.lite.android.di

import android.app.Application
import android.content.Context
import com.twinscience.twin.lite.android.blockly.BlocklyFragment
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.db.DbModule
import com.twinscience.twin.lite.android.project.HomeFragment
import com.twinscience.twin.lite.android.project.MyProjectsFragment
import com.twinscience.twin.lite.android.project.TwinProjectsFragment
import com.twinscience.twin.lite.android.viewmodel.ViewModelModule
import dagger.Component
import javax.inject.Singleton

/**
 * Created by mertselcukdemir on 6.04.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
@Singleton
@Component(modules = [ViewModelModule::class, AppModule::class, DbModule::class])
interface AppComponent {

    fun application(): Application
    fun context(): Context
    fun inject(mainActivity: MainActivity)
    fun inject(homeFragment: HomeFragment)
    fun inject(myProjectsFragment: MyProjectsFragment)
    fun inject(twinProjectsFragment: TwinProjectsFragment)
    fun inject(blocklyFragment: BlocklyFragment)
}
