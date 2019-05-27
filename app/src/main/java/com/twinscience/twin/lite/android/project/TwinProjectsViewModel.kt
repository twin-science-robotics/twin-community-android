package com.twinscience.twin.lite.android.project

import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import com.twinscience.twin.lite.android.data.ProjectModel
import com.twinscience.twin.lite.android.data.ProjectResponse
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.utils.JsonUtils
import com.twinscience.twin.lite.android.utils.LocaleManager
import com.twinscience.twin.lite.android.viewmodel.BaseViewModel
import javax.inject.Inject

class TwinProjectsViewModel
@Inject constructor() : BaseViewModel() {
    var projects: MutableLiveData<List<ProjectModel>> = MutableLiveData()

    fun initProjects(mainActivity: MainActivity) {

        val fileName = if (LocaleManager.getLocaleLanguage() == "tr") "projects_tr.json" else "projects_en.json"
        val loadJSONFromAsset = JsonUtils.loadJSONFromAsset(mainActivity, fileName)

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(ProjectResponse::class.java)
        val projectResponse = jsonAdapter.fromJson(loadJSONFromAsset)
        projectResponse?.let {
            projects.value = it.projects
        }

    }
}
