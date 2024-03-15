package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.flexivid.util.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelObserver @Inject constructor(private val repository: Repository) :
    ViewModel(), DefaultLifecycleObserver {

    val videosStateFLow get() = repository.videosStateFlow

    val foldersStateFLow get() = repository.foldersStateFlow
    val videoFromFoldersStateFLow get() = repository.videosFromFolderStateFlow
    val statusStateFlow get() = repository.statusStateFlow

    fun fetchVideos(context: Context) {
        viewModelScope.launch {
            try {
                repository.getVideos(context)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }
    }

    fun fetchFolders(context: Context) {
        viewModelScope.launch {
            try {
                repository.getFolders(context)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }
    }

    fun fetchVideosFromFolder(context: Context, folderId: String) {
        viewModelScope.launch {
            try {
                repository.getVideosFromFolder(context, folderId)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }

    }


//    private val _videoList = MutableLiveData<ArrayList<VideoDataClass>>()
//    val videoList: LiveData<ArrayList<VideoDataClass>> = _videoList
//
//    fun fetchVideos(context: Context) {
//        viewModelScope.launch {
//            _videoList.value = getAllVideos(context)
//        }
//    }

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