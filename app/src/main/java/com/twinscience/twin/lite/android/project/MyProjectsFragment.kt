package com.twinscience.twin.lite.android.project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.TwinLiteApplication
import com.twinscience.twin.lite.android.base.BaseFragment
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.databinding.FragmentMyProjectsBinding
import com.twinscience.twin.lite.android.dialog.RemoveDialogFragment
import com.twinscience.twin.lite.android.project.presentation.adapter.ProjectRecyclerAdapter
import com.twinscience.twin.lite.android.project.presentation.other.ProjectClickEvent
import com.twinscience.twin.lite.android.viewmodel.ViewModelFactory
import java.util.*
import javax.inject.Inject


class MyProjectsFragment : BaseFragment() {

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var mainActivity: MainActivity
    private lateinit var adapter: ProjectRecyclerAdapter
    private lateinit var binding: FragmentMyProjectsBinding
    private var projects: List<ProjectModel>? = null
    private val viewModel: MyProjectsViewModel by lazy {
        ViewModelProviders.of(this, factory).get(MyProjectsViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        TwinLiteApplication.getAppComponent(mainActivity)?.inject(this)
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
                showDialog(RemoveDialogFragment.newInstance(TAG), "remove", position)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyProjectsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.fragment = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        binding.myProjectsRecycler.adapter = adapter
        binding.myProjectsRecycler.layoutManager = GridLayoutManager(mainActivity, 3)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.initProjects(mainActivity)
    }

    override fun observeViewModel() {
        viewModel.projects.observe(this, Observer<List<ProjectModel>?> {
            it?.let {
                this.projects = it
                adapter.swapDataSet(this.projects)
            }
        })
    }

    /**
     * This method shows alert dialogs for remove saved project or add new project
     */
    fun showDialog(fragment: DialogFragment, tag: String, position: Int) {
        val fm = mainActivity.supportFragmentManager
        fragment.show(fm, tag)
        if (fragment.tag.equals("remove")) {
            val dialogFragment = fragment as RemoveDialogFragment
            dialogFragment.onResult = { isRemoved: Boolean ->
                if (isRemoved) {
                    viewModel.removeProject(projects!![position])
                    adapter.removeItem(position)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        val TAG: String = this::class.java.simpleName
    }

}
