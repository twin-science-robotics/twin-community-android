package com.twinscience.twin.lite.android.project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by mertselcukdemir on 25.11.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
@Entity(tableName = "ProjectEntity")
class ProjectEntity(@field:PrimaryKey
                    var id: String, var name: String?, var date: String?, var filePath: String?, var imgUrl: String?)
