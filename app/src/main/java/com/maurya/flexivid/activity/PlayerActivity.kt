package com.maurya.flexivid.activity

import android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    private lateinit var trackSelector: DefaultTrackSelector


    companion object {
        private var repeat: Boolean = false
        lateinit var player: ExoPlayer
        var playerList: ArrayList<VideoDataClass> = arrayListOf()
        var position: Int = -1
        var isFullScreen: Boolean = false
        var isLocked: Boolean = false
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

        activityPlayerBinding.maximizeMinimizePlayerActivity.setOnClickListener {
            if (isFullScreen) {
                isFullScreen = false
                fullScreen(false)
            } else {
                isFullScreen = true
                fullScreen(true)
            }
        }

        activityPlayerBinding.lockPlayerActivity.setOnClickListener {
            if (!isLocked) {
                isLocked = true
                activityPlayerBinding.playerViewPlayerActivity.hideController()
                activityPlayerBinding.playerViewPlayerActivity.useController = false
                activityPlayerBinding.lockPlayerActivity.setImageResource(R.drawable.icon_lock)

            } else {
                isLocked = false
                activityPlayerBinding.playerViewPlayerActivity.useController = true
                activityPlayerBinding.playerViewPlayerActivity.showController()
                activityPlayerBinding.lockPlayerActivity.setImageResource(R.drawable.icon_unlock)

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
        if (repeat) {
            activityPlayerBinding.repeatPlayerActivity.setImageResource(R.drawable.icon_repeat_one)
        } else {
            activityPlayerBinding.repeatPlayerActivity.setImageResource(R.drawable.icon_repeat)
        }

    }

    private fun visibilityControl() {
        if (isLocked){
            activityPlayerBinding.lockPlayerActivity.visibility=View.VISIBLE
        }

        runnable = Runnable {
            if (activityPlayerBinding.playerViewPlayerActivity.isControllerVisible) {
                activityPlayerBinding.topController.visibility = View.VISIBLE
                activityPlayerBinding.bottomController.visibility = View.VISIBLE
                activityPlayerBinding.playPausePlayerActivity.visibility = View.VISIBLE
            } else {
                activityPlayerBinding.topController.visibility = View.INVISIBLE
                activityPlayerBinding.bottomController.visibility = View.INVISIBLE
                activityPlayerBinding.playPausePlayerActivity.visibility = View.GONE
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 100)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun createPlayer() {

        try {
            player.release()
        } catch (e: Exception) {
        }

        trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()

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
        fullScreen(enable = isFullScreen)
        visibilityControl()
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

    private fun fullScreen(enable: Boolean) {
        if (enable) {
            activityPlayerBinding.playerViewPlayerActivity.resizeMode =
                RESIZE_MODE_FILL
            player.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            activityPlayerBinding.maximizeMinimizePlayerActivity.setImageResource(R.drawable.icon_minimize)
        } else {
            activityPlayerBinding.playerViewPlayerActivity.resizeMode =
                RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            activityPlayerBinding.maximizeMinimizePlayerActivity.setImageResource(R.drawable.icon_maximize)

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()

    }


}