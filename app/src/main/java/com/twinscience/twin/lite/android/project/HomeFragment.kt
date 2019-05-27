package com.twinscience.twin.lite.android.project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.TwinLiteApplication
import com.twinscience.twin.lite.android.base.BaseFragment
import com.twinscience.twin.lite.android.databinding.FragmentHomeBinding
import com.twinscience.twin.lite.android.project.presentation.adapter.HomePagerAdapter
import com.twinscience.twin.lite.android.viewmodel.ViewModelFactory
import javax.inject.Inject

class HomeFragment : BaseFragment() {

    @Inject
    lateinit var factory: ViewModelFactory

    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentHomeBinding


    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        TwinLiteApplication.getAppComponent(mainActivity)?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        viewModel.fetchTabTitles(mainActivity)
        observeViewModel()
    }

    override fun observeViewModel() {
        viewModel.titles.observe(this, Observer<List<String>?> { it ->
            it?.let {
                binding.projectsViewpager.adapter =
                    HomePagerAdapter(mainActivity.supportFragmentManager, it, mainActivity)
                binding.projectsTabLayout.setupWithViewPager(binding.projectsViewpager)
                binding.projectsViewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.projectsTabLayout))
            }
        })
    }

    fun onBackClicked(view: View) {
        mainActivity.onBackPressed()
    }


    companion object {
        fun newInstance() = HomeFragment()
        val TAG: String = this::class.java.simpleName
    }

}
