package com.maurya.flexivid.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.MainActivity.Companion.folderList
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityMainBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.getVideosFromFolderPath

class FolderActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var activityFolderBinding: ActivityFolderBinding

    companion object {
        var currentFolderVideos: ArrayList<VideoDataClass> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderBinding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderBinding.root)


        val position = intent.getIntExtra("position", 0)

        activityFolderBinding.folderNameFolderActivity.text = folderList[position].folderName

        activityFolderBinding.backImgFolderActivity.setOnClickListener {
            finish()
        }


        currentFolderVideos = getVideosFromFolderPath(this, folderList[position].id)


        activityFolderBinding.recyclerViewFolderActivity.setHasFixedSize(true)
        activityFolderBinding.recyclerViewFolderActivity.setItemViewCacheSize(13)
        activityFolderBinding.recyclerViewFolderActivity.layoutManager =
            LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
            )
        activityFolderBinding.recyclerViewFolderActivity.adapter =
            AdapterVideo(this, this, currentFolderVideos)


    }


    override fun onItemClickListener(position: Int) {


    }
}