package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.flexivid.dataEntities.VideoDataClass
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

    //checkBox
    private val _selectedItems = MutableLiveData<ArrayList<VideoDataClass>>()
    val selectedItems: MutableLiveData<ArrayList<VideoDataClass>> = _selectedItems

    init {
        _selectedItems.value = arrayListOf()
    }

    fun toggleSelection(item: VideoDataClass) {
        val selectedSet = _selectedItems.value ?: arrayListOf()
        if (selectedSet.contains(item)) {
            selectedSet.remove(item)
        } else {
            selectedSet.add(item)
        }
        _selectedItems.value = selectedSet
    }
    fun selectAllItems(items: List<VideoDataClass>) {
        _selectedItems.value?.addAll(items)
        _selectedItems.notifyObserver()
    }

    fun clearSelection() {
        _selectedItems.value?.clear()
        _selectedItems.notifyObserver()
    }

    private fun MutableLiveData<ArrayList<VideoDataClass>>.notifyObserver() {
        this.value = this.value
    }


    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)


    }



}