package com.maurya.flexivid.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.fragments.FoldersFragment.Companion.folderList
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.showToast
import com.maurya.flexivid.viewModelsObserver.ModelResult
import com.maurya.flexivid.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var activityFolderBinding: ActivityFolderBinding

    private lateinit var adapterVideo: AdapterVideo

    companion object {
        var currentFolderVideosList: ArrayList<VideoDataClass> = arrayListOf()
    }

    private val viewModel by viewModels<ViewModelObserver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderBinding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderBinding.root)

        currentFolderVideosList.clear()

        lifecycle.addObserver(viewModel)

        val position = intent.getIntExtra("position", 0)

        viewModel.fetchVideosFromFolder(this, folderList[position].id)


        activityFolderBinding.recyclerViewFolderActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                this@FolderActivity, LinearLayoutManager.VERTICAL, false
            )
            adapterVideo = AdapterVideo(
                this@FolderActivity, this@FolderActivity, currentFolderVideosList, isFolder = true
            )
            adapter = adapterVideo
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videoFromFoldersStateFLow.collect {
                    activityFolderBinding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            currentFolderVideosList.clear()
                            currentFolderVideosList.addAll(it.data!!)
                            adapterVideo.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            showToast(
                                this@FolderActivity,
                                it.message.toString()
                            )
                        }

                        is ModelResult.Loading -> {
                            activityFolderBinding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }

        activityFolderBinding.folderNameFolderActivity.isSelected = true

        activityFolderBinding.folderNameFolderActivity.text = folderList[position].folderName

        activityFolderBinding.backImgFolderActivity.setOnClickListener {
            finish()
        }

    }


    override fun onItemClickListener(position: Int) {


    }

    override fun onItemLongClickListener(position: Int) {


    }


//    override suspend fun onVideosFetched(videoList: ArrayList<VideoDataClass>) {
////        fragmentVideosBinding.progressBar.visibility = View.GONE
////        fragmentVideosBinding.recyclerViewVideosFragment.visibility = View.VISIBLE
//        Log.d("UpdateItemClassOnVideoFetchedFolder", videoList.size.toString())
//
//        adapterVideo =
//            AdapterVideo(this, this, videoList, isFolder = true)
//        activityFolderBinding.recyclerViewFolderActivity.adapter = adapterVideo
//        adapterVideo.notifyDataSetChanged()
//    }

}