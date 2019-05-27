package com.twinscience.twin.lite.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

/**
 * Created by mertselcukdemir on 12.06.2018
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
open class BaseViewModel
@Inject constructor() : ViewModel() {
    var loading: MutableLiveData<Boolean>
        protected set

    init {
        this.loading = MutableLiveData()
    }
}
