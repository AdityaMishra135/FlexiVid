package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.util.getAllFolders
import com.maurya.flexivid.util.getAllVideos
import com.maurya.flexivid.util.getVideosFromFolderPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class Repository @Inject constructor() {


    private var videos: ArrayList<VideoDataClass>? = null
    private var folders: ArrayList<FolderDataClass>? = null

    private val _videosStateFlow =
        MutableStateFlow<ModelResult<ArrayList<VideoDataClass>>>(ModelResult.Loading())

    val videosStateFlow: StateFlow<ModelResult<ArrayList<VideoDataClass>>> get() = _videosStateFlow

    private val _foldersStateFlow =
        MutableStateFlow<ModelResult<ArrayList<FolderDataClass>>>(ModelResult.Loading())

    val foldersStateFlow: StateFlow<ModelResult<ArrayList<FolderDataClass>>> get() = _foldersStateFlow


    private val _videosFromFolderStateFlow =
        MutableStateFlow<ModelResult<ArrayList<VideoDataClass>>>(ModelResult.Loading())

    val videosFromFolderStateFlow: StateFlow<ModelResult<ArrayList<VideoDataClass>>> get() = _videosFromFolderStateFlow




    private val _statusStateFlow = MutableStateFlow<ModelResult<String>>(ModelResult.Loading())
    val statusStateFlow: StateFlow<ModelResult<String>> get() = _statusStateFlow

    suspend fun getVideos(context: Context) {
        if (videos == null) {
            _videosStateFlow.emit(ModelResult.Loading())
            try {
                val fetchedVideos = getAllVideos(context)
                videos = fetchedVideos
                _videosStateFlow.emit(ModelResult.Success(fetchedVideos))
            } catch (e: Exception) {
                _videosStateFlow.emit(ModelResult.Error("Failed to fetch videos: ${e.message}"))
            }
        } else {
            _videosStateFlow.emit(ModelResult.Success(videos!!))
        }
    }

    suspend fun getFolders(context: Context) {
        if (folders == null) {
            _foldersStateFlow.emit(ModelResult.Loading())
            try {
                val fetchedFolders = getAllFolders(context)
                folders = fetchedFolders
                _foldersStateFlow.emit(ModelResult.Success(fetchedFolders))
            } catch (e: Exception) {
                _foldersStateFlow.emit(ModelResult.Error("Failed to fetch folders: ${e.message}"))
            }
        } else {
            _foldersStateFlow.emit(ModelResult.Success(folders!!))
        }
    }

    suspend fun getVideosFromFolder(context: Context, folderId: String) {
        _videosFromFolderStateFlow.emit(ModelResult.Loading())
        try {
            val videos = getVideosFromFolderPath(context, folderId)
            _videosFromFolderStateFlow.emit(ModelResult.Success(videos))
        } catch (e: Exception) {
            _videosFromFolderStateFlow.emit(ModelResult.Error("Failed to fetch videos: ${e.message}"))
        }

    }


}

