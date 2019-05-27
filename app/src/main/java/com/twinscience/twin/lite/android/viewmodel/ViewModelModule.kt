package com.twinscience.twin.lite.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twinscience.twin.lite.android.main.MainViewModel
import com.twinscience.twin.lite.android.project.HomeViewModel
import com.twinscience.twin.lite.android.project.MyProjectsViewModel
import com.twinscience.twin.lite.android.project.TwinProjectsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by mertselcukdemir on 9.10.2018
 * Copyright (c) 2019 Twin Science & Robotics to present
 * All rights reserved.
 */
@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(BaseViewModel::class)
    abstract fun bindBaseViewModel(viewModel: BaseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyProjectsViewModel::class)
    abstract fun bindMyProjectsViewModel(viewModel: MyProjectsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TwinProjectsViewModel::class)
    abstract fun bindTwinProjectsViewModel(viewModel: TwinProjectsViewModel): ViewModel

}