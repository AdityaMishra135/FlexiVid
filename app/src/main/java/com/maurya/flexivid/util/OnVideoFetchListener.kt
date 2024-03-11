package com.maurya.flexivid.util

import com.maurya.flexivid.dataEntities.VideoDataClass
import java.io.File


interface OnVideoFetchListener {
    suspend fun onVideosFetched(videoList: ArrayList<VideoDataClass>)

}

