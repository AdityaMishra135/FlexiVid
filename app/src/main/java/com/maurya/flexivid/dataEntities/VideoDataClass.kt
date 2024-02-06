package com.maurya.flexivid.dataEntities

import android.net.Uri

class VideoDataClass(
    val id: String,
    val videoName: String,
    val folderName: String,
    val durationText: Long,
    val size: String,
    val path: String,
    val image: Uri
)
