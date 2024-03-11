package com.maurya.flexivid.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.MainActivity.Companion.folderList
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.OnVideoFetchListener
import com.maurya.flexivid.util.getAllVideos
import com.maurya.flexivid.util.getVideosFromFolderPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FolderActivity : AppCompatActivity(), OnItemClickListener, OnVideoFetchListener {

    private lateinit var activityFolderBinding: ActivityFolderBinding

    private lateinit var adapterVideo: AdapterVideo

    companion object {
        var currentFolderVideos: ArrayList<VideoDataClass> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderBinding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderBinding.root)


        val position = intent.getIntExtra("position", 0)

        activityFolderBinding.folderNameFolderActivity.isSelected = true

        activityFolderBinding.folderNameFolderActivity.text = folderList[position].folderName

        activityFolderBinding.backImgFolderActivity.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            currentFolderVideos =
                getVideosFromFolderPath(
                    this@FolderActivity,
                    folderList[position].id,
                    this@FolderActivity
                )
        }

        activityFolderBinding.recyclerViewFolderActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                this@FolderActivity, LinearLayoutManager.VERTICAL, false
            )
            adapterVideo = AdapterVideo(
                this@FolderActivity, this@FolderActivity, ArrayList(), isFolder = true
            )
            adapter = adapterVideo
        }


        Log.d("UpdateItemClassFolderActivity", currentFolderVideos.size.toString())

    }


    override fun onItemClickListener(position: Int) {


    }

    override fun onItemLongClickListener(position: Int) {


    }


    override suspend fun onVideosFetched(videoList: ArrayList<VideoDataClass>) {
//        fragmentVideosBinding.progressBar.visibility = View.GONE
//        fragmentVideosBinding.recyclerViewVideosFragment.visibility = View.VISIBLE
        Log.d("UpdateItemClassOnVideoFetchedFolder", videoList.size.toString())

        adapterVideo =
            AdapterVideo(this, this, videoList, isFolder = true)
        activityFolderBinding.recyclerViewFolderActivity.adapter = adapterVideo
        adapterVideo.notifyDataSetChanged()
    }

}