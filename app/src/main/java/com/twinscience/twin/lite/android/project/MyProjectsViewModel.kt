package com.twinscience.twin.lite.android.project

import androidx.lifecycle.MutableLiveData
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.project.data.ProjectEntity
import com.twinscience.twin.lite.android.project.db.ProjectDao
import com.twinscience.twin.lite.android.project.def.ProjectDef
import com.twinscience.twin.lite.android.viewmodel.BaseViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MyProjectsViewModel
@Inject
constructor(private val projectDao: ProjectDao) : BaseViewModel() {

    var projects: MutableLiveData<List<ProjectModel>> = MutableLiveData()
    var disposableInit: Disposable? = null
    var disposableRemove: Disposable? = null


    fun initProjects(mainActivity: MainActivity) {
        var disposableInit = Observable.just(projectDao)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    val entities = it.getAllProjects().reversed()
                    run {
                        val projects = mutableListOf<ProjectModel>()
                        val newItemId = UUID.randomUUID().toString()
                        projects += ProjectModel(newItemId, ProjectDef.NEW, mainActivity.getString(R.string.lbl_new_project), "Current Date TODO", null, null, newItemId)
                        if (entities.isNotEmpty()) {
                            entities.forEach { projectEntity: ProjectEntity ->
                                projects += ProjectModel(projectEntity.id.toString(), ProjectDef.PERSONAL, projectEntity.name, projectEntity.date, null, null, projectEntity.id.toString())
                            }
                        }
                        this.projects.postValue(projects)
                    }
                }

    }

    fun removeProject(project: ProjectModel) {
        disposableRemove = Observable.just(projectDao)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    it.deleteProject(project.id.toLong())
                    projects.postValue(projects.value?.minus(project))
                }
    }

    override fun onCleared() {
        super.onCleared()
        disposeItem(disposableInit)
        disposeItem(disposableRemove)
    }

    private fun disposeItem(disposable: Disposable?) {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }
}
