package com.maurya.flexivid.dataEntities

import android.net.Uri


data class VideoDataClass(
    val id: String,
    val videoName: String,
    val folderName: String,
    val durationText: Long,
    val size: String,
    val path: String,
    val image: Uri,
    val dateModified:String,

)

data class FolderDataClass(
    val id: String,
    val folderName: String,
    val folderPath: String,
    val folderItemCount: Int
)
