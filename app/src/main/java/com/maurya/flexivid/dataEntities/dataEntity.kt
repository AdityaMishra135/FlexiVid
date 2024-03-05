package com.maurya.flexivid.dataEntities

import android.net.Uri


data class VideoDataClass(
    val id: String,
    var videoName: String,
    var folderName: String,
    val durationText: Long,
    val size: String,
    var path: String,
    var image: Uri,
    val dateModified: String,
    var isChecked: Boolean = false
)

data class FolderDataClass(
    val id: String,
    val folderName: String,
    val folderPath: String,
    val folderItemCount: Int,
    var isChecked: Boolean = false
)
