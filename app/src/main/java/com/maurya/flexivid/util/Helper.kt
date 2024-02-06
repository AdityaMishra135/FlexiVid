package com.maurya.flexivid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.provider.MediaStore.Video.*
import android.provider.MediaStore.Video.Media.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.maurya.flexivid.MainActivity.Companion.folderList
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import java.io.File
import java.io.IOException
import java.util.UUID


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


fun getAllVideos(context: Context): ArrayList<VideoDataClass> {
    val tempList = ArrayList<VideoDataClass>()
    val tempFolderList = ArrayList<String>()

    val projection = arrayOf(
        _ID, TITLE, BUCKET_DISPLAY_NAME, BUCKET_ID, DURATION, DATA, SIZE
    )

    val cursor = context.contentResolver.query(
        EXTERNAL_CONTENT_URI, projection, null, null, "$DATE_ADDED DESC"
    )

    cursor?.use {
        while (it.moveToNext()) {
            val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
            val videoNameCursor = it.getString(it.getColumnIndexOrThrow(TITLE))
            val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
            val folderIdCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_ID))
            val durationCursor = it.getLong(it.getColumnIndexOrThrow(DURATION))
            val imagePathCursor = it.getString(it.getColumnIndexOrThrow(DATA))
            val videoSizeCursor = it.getString(it.getColumnIndexOrThrow(SIZE))
            val folderPath = imagePathCursor.substringBeforeLast("/")

            try {
                val fileCursor = File(imagePathCursor)
                val imageUri = Uri.fromFile(fileCursor)
                val videoData = VideoDataClass(
                    idCursor,
                    videoNameCursor,
                    folderNameCursor,
                    durationCursor,
                    videoSizeCursor,
                    imagePathCursor,
                    imageUri
                )
                if (fileCursor.exists()) {
                    tempList.add(videoData)
                }

                if (!tempFolderList.contains(folderNameCursor)) {
                    tempFolderList.add(folderNameCursor)
                    folderList.add(FolderDataClass(folderIdCursor, folderNameCursor, folderPath, 0))
                }

            } catch (e: Exception) {
                showToast(context, "Error in Fetching Video File")
            }


        }
    }

    return tempList
}

fun getVideosFromFolderPath(context: Context, folderId: String): ArrayList<VideoDataClass> {
    val tempList = ArrayList<VideoDataClass>()

    val selection = "$BUCKET_ID like? "


    val projection = arrayOf(
        _ID, TITLE, BUCKET_DISPLAY_NAME, BUCKET_ID, DURATION, DATA, SIZE
    )

    val cursor = context.contentResolver.query(
        EXTERNAL_CONTENT_URI, projection, selection, arrayOf(folderId), "$DATE_ADDED DESC"
    )

    cursor?.use {
        while (it.moveToNext()) {
            val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
            val videoNameCursor = it.getString(it.getColumnIndexOrThrow(TITLE))
            val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
            val durationCursor = it.getLong(it.getColumnIndexOrThrow(DURATION))
            val imagePathCursor = it.getString(it.getColumnIndexOrThrow(DATA))
            val videoSizeCursor = it.getString(it.getColumnIndexOrThrow(SIZE))

            try {
                val fileCursor = File(imagePathCursor)
                val imageUri = Uri.fromFile(fileCursor)
                val videoData = VideoDataClass(
                    idCursor,
                    videoNameCursor,
                    folderNameCursor,
                    durationCursor,
                    videoSizeCursor,
                    imagePathCursor,
                    imageUri
                )
                if (fileCursor.exists()) {
                    tempList.add(videoData)
                }


            } catch (e: Exception) {
                showToast(context, "Error in Fetching Video File")
            }


        }
    }

    return tempList
}

fun countVideoFilesInFolder(folderPath: String): Int {
    val folder = File(folderPath)
    if (!folder.exists() || !folder.isDirectory) {
        return 0
    }

    val videoFileExtensions =
        listOf(".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv")

    var videoFileCount = 0

    val folderFiles = folder.listFiles()
    if (folderFiles != null) {
        for (file in folderFiles) {
            if (file.isFile) {
                val fileName = file.name.toLowerCase()
                if (videoFileExtensions.any { fileName.endsWith(it) }) {
                    videoFileCount++
                }
            }
        }
    }

    return videoFileCount
}

fun sendIntent(context: Context,position: Int, reference: String) {
    PlayerActivity.position = position
    val intent = Intent(context, PlayerActivity::class.java)
    intent.putExtra("class", reference)
    ContextCompat.startActivity(context, intent, null)
}
