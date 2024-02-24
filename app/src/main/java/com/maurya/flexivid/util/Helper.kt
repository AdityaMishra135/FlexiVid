package com.maurya.flexivid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.*
import android.provider.MediaStore.Video.*
import android.provider.MediaStore.Video.Media.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.maurya.flexivid.MainActivity.Companion.folderList
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


suspend fun getAllVideos(context: Context): ArrayList<VideoDataClass> =
    withContext(Dispatchers.IO) {
        val tempList = ArrayList<VideoDataClass>()
        val tempFolderList = ArrayList<String>()

        val projection = arrayOf(
            _ID, TITLE, BUCKET_DISPLAY_NAME, BUCKET_ID, DURATION, DATA, SIZE, DATE_MODIFIED
        )

        context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, null, null, "$DATE_ADDED DESC"
        )?.use {
            while (it.moveToNext()) {
                val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
                val videoNameCursor = it.getString(it.getColumnIndexOrThrow(TITLE))
                val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val folderIdCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_ID))
                val durationCursor = it.getLong(it.getColumnIndexOrThrow(DURATION))
                val imagePathCursor = it.getString(it.getColumnIndexOrThrow(DATA))
                val videoSizeCursor = it.getString(it.getColumnIndexOrThrow(SIZE))
                val folderPath = imagePathCursor.substringBeforeLast("/")
                val dateModified = it.getString(it.getColumnIndexOrThrow(DATE_MODIFIED))

                Log.d("getAllVideos", "Processing video: $videoNameCursor")

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
                        imageUri,
                        dateModified
                    )
                    Log.w("getAllVideos", "Try Block")
                    if (fileCursor.exists()) {
                        tempList.add(videoData)
                        Log.d("getAllVideos", "Added video to list: $videoNameCursor")
                    } else {
                        Log.w("getAllVideos", "File does not exist: $imagePathCursor")
                    }

                    if (!tempFolderList.contains(folderNameCursor)) {
                        tempFolderList.add(folderNameCursor)
                        folderList.add(
                            FolderDataClass(
                                folderIdCursor,
                                folderNameCursor,
                                folderPath,
                                0
                            )
                        )
                        Log.d("getAllVideos", "Added folder to list: $folderNameCursor")
                    }

                } catch (e: Exception) {
                    Log.e("getAllVideos", "Error processing video: $videoNameCursor", e)
                    showToast(context, "Error in Fetching Video File")
                    // Log additional information to help diagnose the issue
                    Log.e("getAllVideos", "Exception: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        return@withContext tempList
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
            val dateModified = it.getString(it.getColumnIndexOrThrow(DATE_MODIFIED))

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
                    imageUri,
                    dateModified
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


fun getFormattedFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        "%.1f %s",
        sizeInBytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun getFormattedDate(lastModified: String): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
    return sdf.format(Date(lastModified))
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

fun sendIntent(context: Context, position: Int, reference: String) {
    PlayerActivity.position = position
    val intent = Intent(context, PlayerActivity::class.java)
    intent.putExtra("class", reference)
    ContextCompat.startActivity(context, intent, null)
}
