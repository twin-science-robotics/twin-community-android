package com.twinscience.twin.lite.android.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.base.BaseFragment

/**
 * Created by mertselcukdemir on 2019-05-23
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
class SplashFragment : BaseFragment() {
    private var mainActivity: MainActivity? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed({
            mainActivity?.let {
                findNavController(it, R.id.nav_host_fragment).navigate(R.id.action_splashFragment_to_homeFragment)
            }
        }, 1000)
    }

    override fun observeViewModel() {

    }
}