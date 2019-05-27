package com.twinscience.twin.lite.android.project.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.presentation.view.BaseRecyclerAdapter
import com.twinscience.twin.lite.android.project.presentation.other.ProjectClickEvent
import com.twinscience.twin.lite.android.project.presentation.view.ProjectViewHolder


/**
 * Created by mertselcukdemir on 11.10.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
class ProjectRecyclerAdapter(mDataSet: List<ProjectModel>, private val clickEvent: ProjectClickEvent) : BaseRecyclerAdapter<ProjectViewHolder, ProjectModel>(mDataSet) {

    override fun createView(view: ViewGroup?, viewType: Int): ProjectViewHolder {
        val binding = com.twinscience.twin.lite.android.databinding.AdapterProjectItemBinding.inflate(LayoutInflater.from(view?.context), view, false)
        binding.clickEvent = clickEvent
        return ProjectViewHolder(binding)
    }

    override fun bindView(view: ProjectViewHolder?, position: Int) {
        view?.setItem(mDataSet[position])
    }
}
