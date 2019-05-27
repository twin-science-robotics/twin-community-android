package com.twinscience.twin.lite.android.base

import androidx.fragment.app.Fragment

/**
 * Created by mertselcukdemir on 2019-05-23
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
abstract class BaseFragment : Fragment() {



    /**
     * Observes initialized or updated LiveData values from ViewModels
     */
    protected abstract fun observeViewModel()

}