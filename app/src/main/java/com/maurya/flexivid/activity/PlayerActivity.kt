package com.maurya.flexivid.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
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
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityPlayerBinding
import com.maurya.flexivid.databinding.PopupAudioBoosterBinding
import com.maurya.flexivid.databinding.PopupMoreFeaturesBinding
import com.maurya.flexivid.databinding.PopupVideoSpeedBinding
import com.maurya.flexivid.util.showToast
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var loudnessEnhancer: LoudnessEnhancer

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    private var timer: Timer? = null
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

        activityPlayerBinding.orientationPlayerActivity.setOnClickListener {
            val currentOrientation = resources.configuration.orientation
            requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        activityPlayerBinding.menuPlayerActivity.setOnClickListener {
            pauseVideo()
            val popUpDialog = LayoutInflater.from(this)
                .inflate(R.layout.popup_more_features, activityPlayerBinding.root, false)
            val bindingPopUp = PopupMoreFeaturesBinding.bind(popUpDialog)
            val dialog =
                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle).setView(popUpDialog)
                    .setOnCancelListener {
                        playVideo()
                    }
                    .create()


            dialog.show()

            bindingPopUp.subtitlePopUp.setOnClickListener {
                dialog.dismiss()
                playVideo()

                val subtitleTracks = ArrayList<String>()
                subtitleTracks.add("Subtitle Off")

                for (i in 0 until player.currentTrackGroups.length) {
                    if (player.currentTrackGroups.get(i)
                            .getFormat(0).sampleMimeType == MimeTypes.TEXT_VTT
                    ) {
                        subtitleTracks.add(
                            Locale(
                                player.currentTrackGroups.get(i).getFormat(0).language.toString()
                            ).displayLanguage
                        )
                    }
                }

                val tempTracks = subtitleTracks.toTypedArray<CharSequence>()

                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle)
                    .setTitle("Select Subtitle Track")
                    .setItems(tempTracks) { _, position ->
                        if (position == 0) {
                            trackSelector.setParameters(
                                trackSelector.buildUponParameters().clearSelectionOverrides()
                            )
                            showToast(this, "Subtitles Off")
                        } else {
                            showToast(this, subtitleTracks[position] + " Selected")
                            trackSelector.setParameters(
                                trackSelector.buildUponParameters()
                                    .setPreferredTextLanguage(subtitleTracks[position])
                            )
                        }
                    }
                    .setOnCancelListener {
                        playVideo()
                    }
                    .create()
                    .show()
            }

            bindingPopUp.audioBoosterPopUp.setOnClickListener {
                dialog.dismiss()
                val popUpDialogBooster = LayoutInflater.from(this)
                    .inflate(R.layout.popup_audio_booster, activityPlayerBinding.root, false)
                val bindingPopUpBooster = PopupAudioBoosterBinding.bind(popUpDialogBooster)

                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle).setView(
                    popUpDialogBooster
                )
                    .setOnCancelListener {
                        playVideo()
                    }
                    .create()
                    .show()

                bindingPopUpBooster.verticalSeekbar.setOnProgressChangeListener {
                    loudnessEnhancer.setTargetGain(it * 100)
                }

            }

            bindingPopUp.audioTracksPopUp.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTracks = ArrayList<String>()

                for (i in 0 until player.currentTrackGroups.length) {
                    if (player.currentTrackGroups.get(i)
                            .getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT
                    ) {
                        audioTracks.add(
                            Locale(
                                player.currentTrackGroups.get(i).getFormat(0).language.toString()
                            ).displayLanguage
                        )


                    }
                }

                val tempTracks = audioTracks.toArray(arrayOfNulls<CharSequence>(audioTracks.size))

                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle)
                    .setTitle("Select Audio Track")
                    .setOnCancelListener {
                        playVideo()
                    }
                    .setItems(tempTracks) { _, position ->
                        showToast(this, audioTracks[position] + "Selected")
                        trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                .setPreferredAudioLanguage(audioTracks[position])
                        )

                    }
                    .create()
                    .show()

            }

            bindingPopUp.speedPopUp.setOnClickListener {
                dialog.dismiss()

                val speedOptions =
                    arrayOf("0.25x", "0.50x", "1.0x", "1.25x", "1.50x", "1.75x", "2.0x")

                val popUpDialogSpeed = LayoutInflater.from(this)
                    .inflate(R.layout.popup_video_speed, activityPlayerBinding.root, false)
                val bindingSpeed = PopupVideoSpeedBinding.bind(popUpDialogSpeed)

                bindingSpeed.speedSlider.valueFrom = 0f
                bindingSpeed.speedSlider.valueTo = speedOptions.size.toFloat()

                bindingSpeed.speedSlider.setLabelFormatter { value ->
                    speedOptions[value.toInt()]
                }

                bindingSpeed.speedSlider.value = 2f

                MaterialAlertDialogBuilder(this)
                    .setTitle("Playback Speed")
                    .setView(popUpDialogSpeed)
                    .setPositiveButton("Set") { self, _ ->
                        val selectedSpeedIndex = bindingSpeed.speedSlider.value.toInt()
                        val selectedSpeed = speedOptions[selectedSpeedIndex]
                        showToast(this, "Selected playback speed: $selectedSpeed")
                        self.dismiss()
                    }
                    .create()
                    .show()

            }



            bindingPopUp.sleepTimerPopUp.setOnClickListener {
                dialog.dismiss()

                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(0)
                    .setMinute(0)
                    .setTitleText("Select Sleep Timer")
                    .build()

                timePicker.addOnPositiveButtonClickListener {self->
                    val selectedHour = timePicker.hour
                    val selectedMinute = timePicker.minute

                    val selectedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        selectedHour,
                        selectedMinute
                    )
                    val selectedTimeMillis = (selectedHour * 60 + selectedMinute) * 60 * 1000L

                    timer = Timer()
                    val task = object :TimerTask(){
                        override fun run() {
                            moveTaskToBack(true)
                            exitProcess(1)
                        }
                    }
                    timer!!.schedule(task, selectedTimeMillis)
                    showToast(this, "Sleep timer set for $selectedTime")
                }

                timePicker.addOnNegativeButtonClickListener {
                    showToast(this,"Sleep timer reset")
                }

                timePicker.show(supportFragmentManager, "SleepTimerPicker")
            }



            bindingPopUp.pipPopUp.setOnClickListener { }


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
        if (isLocked) {
            activityPlayerBinding.lockPlayerActivity.visibility = View.VISIBLE
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
        loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
        loudnessEnhancer.enabled = true
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