package com.maurya.flexivid.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.ActivityPlayerBinding
import com.maurya.flexivid.databinding.PopupVideoSpeedBinding
import com.maurya.flexivid.fragments.VideosFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow


//for fetching video files and video folders
suspend fun getAllVideos(
    context: Context
): ArrayList<VideoDataClass> =
    withContext(Dispatchers.IO) {
        val tempList = ArrayList<VideoDataClass>()

        val projection = arrayOf(
            _ID,
            TITLE,
            DISPLAY_NAME,
            BUCKET_DISPLAY_NAME,
            BUCKET_ID,
            DURATION,
            DATA,
            SIZE,
            DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, null, null,
            "DATE_ADDED DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
                val videoNameCursor = it.getString(it.getColumnIndexOrThrow(DISPLAY_NAME))
                val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val durationCursor = it.getLong(it.getColumnIndexOrThrow(DURATION))
                val data = it.getString(it.getColumnIndexOrThrow(DATA))
                val videoSizeCursor = it.getString(it.getColumnIndexOrThrow(SIZE))
                val dateModified = it.getLong(it.getColumnIndexOrThrow(DATE_MODIFIED))
                val fileCursor = File(data)

                if (fileCursor.exists()) {
                    val imageUri = Uri.fromFile(fileCursor)
                    val videoData = VideoDataClass(
                        idCursor,
                        videoNameCursor,
                        folderNameCursor,
                        durationCursor,
                        videoSizeCursor,
                        data,
                        imageUri,
                        dateModified
                    )
                    tempList.add(videoData)
                } else {
                    Log.w("getAllVideos", "File does not exist: $data")
                }
            }
        }

        return@withContext tempList
    }

//for fetching folders
suspend fun getAllFolders(
    context: Context
): ArrayList<FolderDataClass> =
    withContext(Dispatchers.IO) {

        val tempFolderList = ArrayList<String>()
        val folderList = ArrayList<FolderDataClass>()

        val projection = arrayOf(
            _ID, TITLE, BUCKET_DISPLAY_NAME, BUCKET_ID, DURATION, DATA, SIZE, DATE_MODIFIED
        )
        val cursor = context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, null, null,
            "DATE_ADDED DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val folderIdCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_ID))
                val data = it.getString(it.getColumnIndexOrThrow(DATA))
                val folderPath = data.substringBeforeLast("/")

                if (!tempFolderList.contains(folderNameCursor) && !folderNameCursor.contains("Internal Storage")) {
                    tempFolderList.add(folderNameCursor)
                    folderList.add(FolderDataClass(folderIdCursor, folderNameCursor, folderPath, 0))
                }
            }
        }
        return@withContext folderList
    }


//using in Folder Activity to retrieve video files from path
suspend fun getVideosFromFolderPath(
    context: Context,
    folderId: String
): ArrayList<VideoDataClass> =
    withContext(Dispatchers.IO) {
        val tempList = ArrayList<VideoDataClass>()

        val selection = "$BUCKET_ID like? "

        val projection = arrayOf(
            _ID, TITLE, BUCKET_DISPLAY_NAME, BUCKET_ID, DURATION, DATA, SIZE, DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, selection, arrayOf(folderId), "$DATE_ADDED DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
                val videoNameCursor = it.getString(it.getColumnIndexOrThrow(TITLE))
                val folderNameCursor =
                    it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val durationCursor = it.getLong(it.getColumnIndexOrThrow(DURATION))
                val imagePathCursor = it.getString(it.getColumnIndexOrThrow(DATA))
                val videoSizeCursor = it.getString(it.getColumnIndexOrThrow(SIZE))
                val dateModified = it.getLong(it.getColumnIndexOrThrow(DATE_MODIFIED))


                try {
                    val fileCursor = File(imagePathCursor)
                    if (fileCursor.exists()) {
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
                        tempList.add(videoData)
                    } else {
                        Log.w("getAllVideos", "File does not exist: $imagePathCursor")
                    }


                } catch (e: Exception) {
                    showToast(context, "Error in Fetching Video File")
                }


            }
        }

        return@withContext tempList
    }


//sorting Video List
fun sortMusicList(
    sortBy: String,
    videoList: ArrayList<VideoDataClass>,
    adapterVideo: AdapterVideo
) {
    when (sortBy) {
        "DATE_ADDED ASC" -> videoList.sortBy { it.dateModified }
        "DATE_ADDED DESC" -> videoList.sortByDescending { it.dateModified }
        "SIZE ASC" -> videoList.sortBy { it.durationText }
        "SIZE DESC" -> videoList.sortByDescending { it.durationText }
        "DISPLAY_NAME ASC" -> videoList.sortBy { it.videoName.lowercase() }
        "DISPLAY_NAME DESC" -> videoList.sortByDescending { it.videoName.lowercase() }
        else -> {
            videoList.sortByDescending { it.dateModified }
        }
    }
    adapterVideo.notifyDataSetChanged()
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

// for converting bytes to MB and GB
fun getFormattedFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt()

    val sizeInUnit = sizeInBytes / 1024.0.pow(digitGroups.toDouble())
    return "%.1f %s".format(sizeInUnit, units[digitGroups])
}


@SuppressLint("UseCompatTextViewDrawableApis")
fun setTextViewColorsForChangingSelection(
    context: Context,
    textViews: Array<TextView>,
    textColorId: Int,
    clickable:Boolean
) {
    val redColor = ContextCompat.getColor(context, textColorId)
    textViews.forEachIndexed { _, textView ->
        textView.setTextColor(redColor)
        textView.compoundDrawableTintList = ColorStateList.valueOf(redColor)
        textView.isClickable = clickable
    }
}

fun getFormattedDate(epochTime: Long): String {
    val date = Date(epochTime * 1000)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    return dateFormat.format(date)
}

fun getPathFromURI(context: Context, uri: Uri): String {
    var filePath = ""
    // ExternalStorageProvider
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(':')
    val type = split[0]

    return if ("primary".equals(type, ignoreCase = true)) {
        "${Environment.getExternalStorageDirectory()}/${split[1]}"
    } else {
        //getExternalMediaDirs() added in API 21
        val external = context.externalMediaDirs
        if (external.size > 1) {
            filePath = external[1].absolutePath
            filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1]
        }
        filePath
    }
}


//for sending intent from activity/fragment/ adapter to player activity
fun sendIntent(context: Context, position: Int, reference: String) {
    PlayerActivity.position = position
    val intent = Intent(context, PlayerActivity::class.java)
    intent.putExtra("class", reference)
    ContextCompat.startActivity(context, intent, null)
}


///for subtitle and audio track
fun showTrackSelectionDialog(
    context: Context,
    trackSelector: DefaultTrackSelector,
    title: String,
    options: Array<CharSequence?>,
    isAudioTrack: Boolean,
    trackLanguages: List<Pair<String?, String?>>
) {
    MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setItems(options) { _, position ->
            val parameters = trackSelector.buildUponParameters()
            if (position == 0) {
                showToast(context, "${title.split(" ")[0]} Disabled")
                for (rendererIndex in 0 until PlayerActivity.player.rendererCount) {
                    if (isAudioTrack && PlayerActivity.player.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO ||
                        !isAudioTrack && PlayerActivity.player.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT
                    ) {
                        parameters.setRendererDisabled(rendererIndex, true)
                    }
                }
            } else {
                val selectedLanguage = trackLanguages[position - 1].second
                showToast(context, "$selectedLanguage Selected")
                for (rendererIndex in 0 until PlayerActivity.player.rendererCount) {
                    if (isAudioTrack && PlayerActivity.player.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO ||
                        !isAudioTrack && PlayerActivity.player.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT
                    ) {
                        parameters.setRendererDisabled(rendererIndex, false)
                    }
                }
                if (!isAudioTrack) {
                    parameters.setPreferredTextLanguage(selectedLanguage)
                } else {
                    parameters.setPreferredAudioLanguage(selectedLanguage)
                }
            }
            trackSelector.setParameters(parameters)
        }
        .show()
}


//toast
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


//Ui SKin
/*
fun tintDrawable(context: Context, drawableResId: Int, colorResId: Int): Drawable? {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
    drawable?.let {
        val wrappedDrawable = DrawableCompat.wrap(it)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, colorResId))
        return wrappedDrawable
    }
    return null
}

fun restartApp(context: Context) {
    val packageName = context.packageName
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
*/