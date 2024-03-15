package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.util.getAllFolders
import com.maurya.flexivid.util.getAllVideos
import com.maurya.flexivid.util.getVideosFromFolderPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class Repository @Inject constructor() {

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
        _videosStateFlow.emit(ModelResult.Loading())
        try {
            val videos = getAllVideos(context)
            _videosStateFlow.emit(ModelResult.Success(videos))
        } catch (e: Exception) {
            _videosStateFlow.emit(ModelResult.Error("Failed to fetch videos: ${e.message}"))
        }
    }

    suspend fun getFolders(context: Context) {
        _foldersStateFlow.emit(ModelResult.Loading())
        try {
            val folders = getAllFolders(context)
            _foldersStateFlow.emit(ModelResult.Success(folders))
        } catch (e: Exception) {
            _foldersStateFlow.emit(ModelResult.Error("Failed to fetch folders: ${e.message}"))
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


    /*
        suspend fun createNotes(noteRequest: NoteRequest) {
            _statusStateFlow.emit(Result.Loading())
            try {
                val response = notesAPI.createNote(noteRequest)
                handleResponse(response, "Notes Created")
            } catch (e: Exception) {
                _notesStateFlow.emit(Result.Error("Something went wrong"))
            }
        }

        suspend fun updateVideos(noteId: String, noteRequest: NoteRequest) {
            _statusStateFlow.emit(VideoResult.Loading())
            try {
                val response = notesAPI.updateNote(noteId, noteRequest)
                handleResponse(response, "Note Updated")
            } catch (e: Exception) {
                _notesStateFlow.emit(VideoResult.Error("Something went wrong"))
            }
        }

        suspend fun deleteVideos(noteId: String) {
            _statusStateFlow.emit(VideoResult.Loading())
            try {
                val response = notesAPI.deleteNote(noteId)
                handleResponse(response, "Note Deleted")
            } catch (e: Exception) {
                _notesStateFlow.emit(VideoResult.Error("Something went wrong"))
            }

        }


        private suspend fun handleResponse(response: Response<VideoDataClass>, message: String) {
            if (response.isSuccessful && response.body() != null) {
                _statusStateFlow.emit(VideoResult.Success(message))
            } else {
                _videosStateFlow.emit(VideoResult.Error("Something went wrong"))
            }
        }

    */
}

