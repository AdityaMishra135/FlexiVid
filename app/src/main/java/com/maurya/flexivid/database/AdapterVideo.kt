package com.maurya.flexivid.database

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ItemVideoBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.sendIntent

class AdapterVideo(
    private val context: Context,
    private var listener: OnItemClickListener,
    private var itemList: ArrayList<VideoDataClass> = arrayListOf(),
    private val isFolder: Boolean = false
) : RecyclerView.Adapter<AdapterVideo.VideoHolder>() {


    private var itemSelectedList = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)

        return VideoHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val currentItem = itemList[position]

        with(holder) {
            videoTitle.isSelected = true

            val videoNameWithoutExtension = currentItem.videoName.substringBeforeLast('.')
            videoTitle.text = videoNameWithoutExtension
            folderName.text = currentItem.folderName
            durationText.text = DateUtils.formatElapsedTime(currentItem.durationText / 1000)

            Log.d("videoNameItemClass", currentItem.videoName)
            Log.d("videoPathItemClass", currentItem.path)

            Glide.with(context)
                .asBitmap()
                .load(currentItem.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.mp4)
                .into(image)

            root.setOnClickListener {
                when {
                    itemList[position].id == PlayerActivity.nowPlayingId -> {
                        sendIntent(context, position, "nowPlaying")
                    }

                    isFolder -> {
                        PlayerActivity.pipStatus = 1
                        sendIntent(context, position, "folderActivity")
                    }

                    MainActivity.search -> {
                        PlayerActivity.pipStatus = 2
                        sendIntent(context, position, "searchView")
                    }

                    else -> {
                        PlayerActivity.pipStatus = 3
                        sendIntent(context, position, "allVideos")
                    }
                }
            }


            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (itemList[position].isChecked != isChecked) {
                    itemList[position].isChecked = isChecked
                }
            }

        }


    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchList(searchList: ArrayList<VideoDataClass>) {
        itemList = ArrayList()
        itemList.addAll(searchList)
        notifyDataSetChanged()
    }


    inner class VideoHolder(binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener {
        val videoTitle = binding.videoNameVideoItem
        val folderName = binding.pathNameVideoItem
        val durationText = binding.videoDurationVideoItem
        val image = binding.videoImageVideoItem
        val root = binding.root
        val checkBox = binding.checkBoxVideoItem


        init {
            root.setOnClickListener(this)
            root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                for (i in itemList.indices) {
                    checkBox.visibility = View.VISIBLE
                }
                itemList[position].isChecked = true
                checkBox.isChecked = true
                listener.onItemLongClickListener(position)
            }
            return true
        }


    }

}