package com.twinscience.twin.lite.android.project.presentation.view

import android.content.Context
import android.net.Uri
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.databinding.AdapterProjectItemBinding
import com.twinscience.twin.lite.android.utils.ResUtils


/**
 * Created by mertselcukdemir on 11.10.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
class ProjectViewHolder(private val binding: AdapterProjectItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setItem(projectModel: ProjectModel?) {
        binding.position = layoutPosition

        val context = binding.root.context
        projectModel?.let { project ->
            binding.project = project
                setBackground(context, binding.projectItemImg)
            project.image?.let {
                val nameWithoutExtension = it.split(".")
                val path = ResUtils.getPathByResourceName(context, nameWithoutExtension[0], "drawable")
                Glide.with(context)
                        .load(Uri.parse(path))
                        .error(Glide.with(context)
                                .load(R.drawable.twin_main_exp))
                        .apply(RequestOptions().fitCenter().placeholder(R.drawable.img_sample_project))
                        .into(binding.projectItemImg)
            }

        }

    }

    private fun setBackground(context: Context, image: ImageButton) {
        when (layoutPosition % 5) {
            0 -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.aquamarine))
            1 -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.salmon_pink))
            2 -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.third_purple))
            3 -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.project_yellow))
            4 -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.project_green))
            else -> image.setBackgroundColor(ContextCompat.getColor(context, R.color.pale_purple))
        }
    }
}
