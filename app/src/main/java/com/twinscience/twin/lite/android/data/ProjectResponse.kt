package com.twinscience.twin.lite.android.data

import com.squareup.moshi.Json

/**
 * Created by mertselcukdemir on 5.04.2019
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
data class ProjectResponse(
        @Json(name = "projects")
        val projects: List<ProjectModel>
)
