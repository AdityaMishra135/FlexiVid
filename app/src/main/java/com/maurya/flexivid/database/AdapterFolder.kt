package com.maurya.flexivid.database

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.maurya.flexivid.activity.FolderActivity
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.databinding.ItemFolderBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.countVideoFilesInFolder

class AdapterFolder(
    private val context: Context,
    private var listener: OnItemClickListener,
    private val itemList: MutableList<FolderDataClass> = mutableListOf()
) : RecyclerView.Adapter<AdapterFolder.FolderHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(context), parent, false)

        return FolderHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FolderHolder, position: Int) {
        val currentItem = itemList[position]

        with(holder) {
            folderName.isSelected = true

            folderName.text = currentItem.folderName

            val textToShow = countVideoFilesInFolder(currentItem.folderPath)

            if (textToShow == 1) {
                folderItemCount.text = "1 video"
            } else {
                folderItemCount.text = "$textToShow videos"
            }

            root.setOnClickListener {
                val intent = Intent(context, FolderActivity::class.java)
                intent.putExtra("position", position)
                ContextCompat.startActivity(context, intent, null)

            }
        }


    }


    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class FolderHolder(binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val folderName = binding.folderNameFolderItem
        val folderItemCount = binding.folderItemCountFolderItem
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