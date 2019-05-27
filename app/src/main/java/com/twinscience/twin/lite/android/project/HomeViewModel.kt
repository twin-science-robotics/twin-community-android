package com.twinscience.twin.lite.android.project

import androidx.lifecycle.MutableLiveData
import com.twinscience.twin.lite.android.main.MainActivity
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.viewmodel.BaseViewModel
import javax.inject.Inject

class HomeViewModel
@Inject constructor() : BaseViewModel() {

    var titles: MutableLiveData<List<String>> = MutableLiveData()

    /**
     * This will fetch tab titles for TabBarLayout.
     * @param activity required for localization.
     * @sample activity.getString(...)
     */
    fun fetchTabTitles(activity: MainActivity?) {
        activity?.let {
            val titles =
                listOf<String>(it.getString(R.string.title_my_projects), it.getString(R.string.title_twin_projects))
            this.titles.value = titles
        }
    }
}
