package com.maurya.flexivid.viewModelsObserver

sealed class VideoResult<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T) : VideoResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : VideoResult<T>(data, message)
    class Loading<T> : VideoResult<T>()
}