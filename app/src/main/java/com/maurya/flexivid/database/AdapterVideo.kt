package com.maurya.flexivid.database

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ItemVideoBinding
import com.maurya.flexivid.util.OnItemClickListener

class AdapterVideo(
    private val context: Context,
    private var listener: OnItemClickListener,
    private val itemList: MutableList<VideoDataClass> = mutableListOf()
) : RecyclerView.Adapter<AdapterVideo.DayHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)

        return DayHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        val currentItem = itemList[position]

        with(holder) {
            videoTitle.isSelected=true

            videoTitle.text = currentItem.videoName
            folderName.text = currentItem.folderName
            durationText.text=DateUtils.formatElapsedTime(currentItem.durationText/1000)

            Glide.with(context)
                .asBitmap()
                .load(currentItem.image)
                .centerCrop()
                .error(R.drawable.mp4)
                .into(image)


        }


    }


    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class DayHolder(binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val videoTitle = binding.videoNameVideoItem
        val folderName = binding.pathNameVideoItem
        val durationText = binding.videoDurationVideoItem
        val image = binding.videoImageVideoItem
        val root = binding.root



        init {
            root.setOnClickListener(this)
        }


        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClickListener(position)
            }
        }

    }
}