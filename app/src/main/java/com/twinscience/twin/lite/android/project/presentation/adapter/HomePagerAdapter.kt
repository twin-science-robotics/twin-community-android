package com.twinscience.twin.lite.android.project.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.project.MyProjectsFragment
import com.twinscience.twin.lite.android.project.TwinProjectsFragment


/**
 * Created by mertselcukdemir on 11.10.2018
 * Copyright (c) 2018 Twin Science & Robotics to present
 * All rights reserved.
 */
class HomePagerAdapter constructor(
    fm: FragmentManager,
    private var tabTitles: List<String>,
    private var activity: MainActivity
) : FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 2//My Projects & Twin Projects
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TwinProjectsFragment()
            1 -> MyProjectsFragment()
            else -> TwinProjectsFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

}