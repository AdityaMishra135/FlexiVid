package com.maurya.flexivid.activity

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding


    private var repeat: Boolean = false

    companion object {
        lateinit var player: ExoPlayer
        var playerList: ArrayList<VideoDataClass> = arrayListOf()
        var position: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)

        setTheme(R.style.playerActivityTheme)
        setContentView(activityPlayerBinding.root)
        //
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, activityPlayerBinding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        activityPlayerBinding.videoTitlePlayerActivity.isSelected = true

        player = Builder(this).build()

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

        activityPlayerBinding.nextPlayerActivity.setOnClickListener {
            nextPrevVideo(true)
        }
        activityPlayerBinding.previousPlayerActivity.setOnClickListener {
            nextPrevVideo(false)
        }

        activityPlayerBinding.repeatPlayerActivity.setOnClickListener {
            if (repeat) {
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                activityPlayerBinding.repeatPlayerActivity.setImageResource(R.drawable.icon_repeat)

            } else {
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                activityPlayerBinding.repeatPlayerActivity.setImageResource(R.drawable.icon_repeat_one)

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

        try {
            player.release()
        } catch (e: Exception) {
        }

        activityPlayerBinding.videoTitlePlayerActivity.text = playerList[position].videoName
        activityPlayerBinding.playerViewPlayerActivity.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].image)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextPrevVideo(true)
                }
            }

        })
    }

    private fun nextPrevVideo(isNext: Boolean = true) {

//        if (!repeat){

        if (isNext) {
            setPosition(true)
        } else {
            setPosition(false)
        }
        createPlayer()
    }

    private fun setPosition(isIncrement: Boolean = true) {
        if (isIncrement) {
            if (playerList.size - 1 == position) {
                position = 0
            } else {
                ++position
            }
        } else {
            if (position == 0) {
                position = playerList.size - 1
            } else {
                --position
            }

        }
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