package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.flexivid.dataEntities.VideoDataClass
import kotlinx.coroutines.launch

class ViewModelObserverVideoFragment : ViewModel(), DefaultLifecycleObserver {


    private val _videoList = MutableLiveData<ArrayList<VideoDataClass>>()
    val videoList: LiveData<ArrayList<VideoDataClass>> = _videoList

    fun fetchVideos(context: Context) {
        // Fetch video data asynchronously and update _videoList LiveData
        viewModelScope.launch {
            _videoList.value = getAllVideos(context)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)


    }


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

}