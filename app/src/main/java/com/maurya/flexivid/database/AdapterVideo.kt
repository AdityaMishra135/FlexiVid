package com.maurya.flexivid.database

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
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
import com.maurya.flexivid.viewModelsObserver.ViewModelObserver

class AdapterVideo(
    private val context: Context,
    private var listener: OnItemClickListener,
    private var itemList: ArrayList<VideoDataClass> = arrayListOf(),
    private val viewModel: ViewModelObserver,
    private val lifecycleOwner: LifecycleOwner,
    private val isFolder: Boolean = false
) : RecyclerView.Adapter<AdapterVideo.VideoHolder>() {

    private var isLongClickMode = false

//    val itemSelectedList: ArrayList<VideoDataClass> = arrayListOf()
    val itemSelectedList: MutableLiveData<ArrayList<VideoDataClass>> = MutableLiveData()

    fun setLongClickMode(enabled: Boolean) {
        isLongClickMode = enabled
        notifyDataSetChanged()
    }

    fun toggleSelection(item: VideoDataClass) {
        val currentItems = itemSelectedList.value ?: ArrayList()
        if (currentItems.contains(item)) {
            currentItems.remove(item)
        } else {
            currentItems.add(item)
        }
        itemSelectedList.value = currentItems
    }

    fun selectAllItems(items: ArrayList<VideoDataClass>) {
        val currentItems = itemSelectedList.value ?: ArrayList()
        currentItems.clear()
        currentItems.addAll(items)
        itemSelectedList.value = currentItems
        notifyDataSetChanged()
    }
    fun unSelectAllItems(items: ArrayList<VideoDataClass>) {
        val currentItems = itemSelectedList.value ?: ArrayList()
        currentItems.removeAll(items.toSet())
        itemSelectedList.value = currentItems
        notifyDataSetChanged()
    }

    fun clearSelection() {
        itemSelectedList.value?.clear()
    }



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

            Glide.with(context)
                .asBitmap()
                .load(currentItem.image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .error(R.drawable.mp4)
                .into(image)

            root.setOnClickListener {
                if (!isLongClickMode) {
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
                } else {
                    toggleSelection(currentItem)
                    checkBox.isChecked = itemSelectedList.value?.contains(currentItem) == true
                }
            }
            checkBox.isChecked = itemSelectedList.value?.contains(currentItem) == true


            checkBox.visibility = if (isLongClickMode) View.VISIBLE else View.GONE


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
        RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {
        val videoTitle = binding.videoNameVideoItem
        val folderName = binding.pathNameVideoItem
        val durationText = binding.videoDurationVideoItem
        val image = binding.videoImageVideoItem
        val root = binding.root
        val checkBox = binding.checkBoxVideoItem

        init {
            root.setOnLongClickListener(this)
        }


        override fun onLongClick(view: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                isLongClickMode = !isLongClickMode
//                viewModel.toggleSelection(itemList[position])
                toggleSelection(itemList[position])
                listener.onItemLongClickListener(position)
            }
            return true
        }

    }


}