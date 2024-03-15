package com.maurya.flexivid.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.activity.PlayerActivity.Companion.position
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterFolder
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.fragments.FoldersFragment.Companion.folderList
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.getVideosFromFolderPath
import com.maurya.flexivid.viewModelsObserver.ModelResult
import com.maurya.flexivid.viewModelsObserver.ViewModelObserver
import kotlinx.coroutines.launch

class FolderActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var activityFolderBinding: ActivityFolderBinding

    private lateinit var adapterVideo: AdapterVideo

    companion object {
        var currentFolderVideos: ArrayList<VideoDataClass> = arrayListOf()
    }

    private val viewModel by viewModels<ViewModelObserver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderBinding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderBinding.root)


        lifecycle.addObserver(viewModel)
        viewModel.fetchVideosFromFolder(this, folderList[position].id)

        val position = intent.getIntExtra("position", 0)

        activityFolderBinding.recyclerViewFolderActivity.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(
                this@FolderActivity, LinearLayoutManager.VERTICAL, false
            )
            adapterVideo = AdapterVideo(
                this@FolderActivity, this@FolderActivity, currentFolderVideos, isFolder = true
            )
            adapter = adapterVideo
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersStateFLow.collect {
                    fragmentFoldersBinding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            folderList.clear()
                            folderList.addAll(it.data!!)
                            adapterFolder.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is ModelResult.Loading -> {
                            fragmentFoldersBinding.progressBar.visibility = View.VISIBLE
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


//        lifecycleScope.launch {
//            currentFolderVideos =
//                getVideosFromFolderPath(
//                    this@FolderActivity,
//                    folderList[position].id
//                )
//        }




        Log.d("UpdateItemClassFolderActivity", currentFolderVideos.size.toString())

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