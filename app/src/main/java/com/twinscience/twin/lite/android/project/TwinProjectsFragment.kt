package com.twinscience.twin.lite.android.project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.TwinLiteApplication
import com.twinscience.twin.lite.android.base.BaseFragment
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.project.presentation.adapter.ProjectRecyclerAdapter
import com.twinscience.twin.lite.android.project.presentation.other.ProjectClickEvent
import com.twinscience.twin.lite.android.viewmodel.ViewModelFactory
import java.util.*
import javax.inject.Inject

class TwinProjectsFragment : BaseFragment() {

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var mainActivity: MainActivity
    private lateinit var adapter: ProjectRecyclerAdapter
    private lateinit var binding: com.twinscience.twin.lite.android.databinding.FragmentTwinProjectsBinding
    private var projects: List<ProjectModel>? = null
    private val viewModel: TwinProjectsViewModel by lazy {
        ViewModelProviders.of(this, factory).get(TwinProjectsViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        TwinLiteApplication.getAppComponent(mainActivity)!!.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ProjectRecyclerAdapter(Collections.emptyList(), object : ProjectClickEvent {
            override fun onProjectClicked(position: Int) {
                projects?.let {
                    mainActivity.mainViewModel.checkServicesForCoding(it[position], mainActivity)
                }
            }

            override fun onRemoveProjectClicked(position: Int) {

            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = com.twinscience.twin.lite.android.databinding.FragmentTwinProjectsBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        binding.twinProjectsRecycler.adapter = adapter
        binding.twinProjectsRecycler.layoutManager = GridLayoutManager(mainActivity, 3)
        viewModel.initProjects(mainActivity)
        observeViewModel()
    }

    override fun observeViewModel() {
        viewModel.projects.observe(this, Observer<List<ProjectModel>?> {
            it?.let {
                this.projects = it
                adapter.swapDataSet(this.projects)
            }
        })
    }


    companion object {
        val TAG: String = this::class.java.simpleName
    }
}
