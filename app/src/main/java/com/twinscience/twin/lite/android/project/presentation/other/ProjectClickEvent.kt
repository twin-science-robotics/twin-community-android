package com.twinscience.twin.lite.android.project.presentation.other

/**
 * Created by mertselcukdemir on 12.10.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
interface ProjectClickEvent {
    fun onProjectClicked(position: Int)

    fun onRemoveProjectClicked(position: Int)
}
