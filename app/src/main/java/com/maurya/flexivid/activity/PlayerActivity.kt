package com.maurya.flexivid.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding


    private var isPause: Boolean = false

    companion object {
        lateinit var player: ExoPlayer
        var playerList: ArrayList<VideoDataClass> = arrayListOf()
        var position: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(activityPlayerBinding.root)


        activityPlayerBinding.videoTitlePlayerActivity.isSelected = true

        initializeLayout()
        listeners()

    }

    private fun listeners() {

        activityPlayerBinding.backPlayerActivity.setOnClickListener {
            finish()
        }

        activityPlayerBinding.playPausePlayerActivity.setOnClickListener {
            if (player.isPlaying) {
                pauseVideo()
            } else {
                playVideo()
            }

        }

    }

    private fun initializeLayout() {
        when (intent.getStringExtra("class")) {
            "allVideos" -> {
                playerList.addAll(MainActivity.videoList)
                createPlayer()


            }

            "folderActivity" -> {
                playerList.addAll(FolderActivity.currentFolderVideos)
                createPlayer()
            }

        }


    }


    private fun createPlayer() {
        activityPlayerBinding.videoTitlePlayerActivity.text = playerList[position].videoName
        val player = Builder(this).build()
        activityPlayerBinding.playerViewPlayerActivity.player = player

        val mediaItem = MediaItem.fromUri(playerList[position].image)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
    }


    private fun playVideo() {
        activityPlayerBinding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
        player.play()
    }

    private fun pauseVideo() {
        activityPlayerBinding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()

    }


}