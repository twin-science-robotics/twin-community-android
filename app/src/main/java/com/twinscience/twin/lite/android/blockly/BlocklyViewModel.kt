package com.twinscience.twin.lite.android.blockly

import com.twinscience.twin.lite.android.project.db.ProjectDao
import com.twinscience.twin.lite.android.viewmodel.BaseViewModel

import javax.inject.Inject

/**
 * Created by mertselcukdemir on 2019-05-07
 * Copyright (c) 2019 YGA to present
 * All rights reserved.
 */
class BlocklyViewModel @Inject
constructor(private val dao: ProjectDao) : BaseViewModel(){

}
