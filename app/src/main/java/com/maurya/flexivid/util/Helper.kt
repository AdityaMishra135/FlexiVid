package com.maurya.flexivid.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.provider.MediaStore.Video.*
import android.provider.MediaStore.Video.Media.*
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.maurya.flexivid.dataEntities.VideoDataClass
import java.io.File
import java.io.IOException


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


fun uriToFile(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName) // Change to an appropriate file name

    if (inputStream != null) {
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } else {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        if (documentFile != null && documentFile.isDirectory) {
            throw IOException("Selected item is a directory, not a file")
        } else {
            throw IOException("Could not open the file")
        }
    }

    return file
}


fun getAllVideos(context: Context): ArrayList<VideoDataClass> {
    val tempList = ArrayList<VideoDataClass>()

    val projection = arrayOf(
        TITLE,
        ALBUM,
        _ID,
        BUCKET_DISPLAY_NAME,
        SIZE,
        DATA,
        DATE_ADDED,
        DURATION
    )

    val cursor = context.contentResolver.query(
        EXTERNAL_CONTENT_URI, projection, null, null,
        "$DATE_ADDED DESC"
    )

    cursor?.use {
        while (it.moveToNext()) {
            val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
            val videoNameCursor =
                it.getString(it.getColumnIndexOrThrow(TITLE))
            val folderNameCursor =
                it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
            val durationCursor =
                it.getLong(it.getColumnIndexOrThrow(DURATION))
            val imagePathCursor =
                it.getString(it.getColumnIndexOrThrow(DATA))
            val videoSizeCursor =
                it.getString(it.getColumnIndexOrThrow(SIZE))

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