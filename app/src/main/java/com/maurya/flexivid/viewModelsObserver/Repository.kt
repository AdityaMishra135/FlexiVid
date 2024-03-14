package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import android.util.Log
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.util.getAllVideos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class Repository @Inject constructor(){

    private val _videosStateFlow =
        MutableStateFlow<VideoResult<ArrayList<VideoDataClass>>>(VideoResult.Loading())

    val videosStateFlow: StateFlow<VideoResult<ArrayList<VideoDataClass>>> get() = _videosStateFlow

    private val _statusStateFlow = MutableStateFlow<VideoResult<String>>(VideoResult.Loading())
    val statusStateFlow: StateFlow<VideoResult<String>> get() = _statusStateFlow

    suspend fun getVideos(context: Context) {
        _videosStateFlow.emit(VideoResult.Loading())
        try {
            val videos = getAllVideos(context)
            _videosStateFlow.emit(VideoResult.Success(videos))
        } catch (e: Exception) {
            _videosStateFlow.emit(VideoResult.Error("Failed to fetch videos: ${e.message}"))
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