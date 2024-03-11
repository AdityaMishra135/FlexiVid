package com.maurya.flexivid.activity

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityPlayerBinding
import com.maurya.flexivid.databinding.PopupMoreFeaturesBinding
import com.maurya.flexivid.databinding.PopupVideoSpeedBinding
import com.maurya.flexivid.util.OnDoubleClickListener
import com.maurya.flexivid.util.getPathFromURI
import com.maurya.flexivid.util.showToast
import com.maurya.flexivid.util.showTrackSelectionDialog
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


class PlayerActivity : AppCompatActivity() {
    private lateinit var activityPlayerBinding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var loudnessEnhancer: LoudnessEnhancer
    private lateinit var mediaSession: MediaSessionCompat


    private var timer: Timer? = null

    companion object {
        private var repeat: Boolean = false
        lateinit var player: ExoPlayer
        var playerList: ArrayList<VideoDataClass> = arrayListOf()
        var position: Int = -1

        //timer
        var isTimerOn: Boolean = false
        var timerText: String = ""

        //speed
        var lastSpeedIndex: Int = 3

        var isFullScreen: Boolean = false
        var isLocked: Boolean = false
        var pipStatus: Int = 0
        var nowPlayingId: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        activityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)

        setContentView(activityPlayerBinding.root)

        setTheme(R.style.playerActivityTheme)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, activityPlayerBinding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }


        activityPlayerBinding.videoTitlePlayerActivity.isSelected = true
        activityPlayerBinding.lockDisablePlayerActivity.visibility = View.GONE

        player = Builder(this).build()

        try {
            //for handling video file intent (Improved Version)
            if (intent.data?.scheme.contentEquals("content")) {
                playerList = ArrayList()
                position = 0
                val cursor = contentResolver.query(
                    intent.data!!, arrayOf(MediaStore.Video.Media.DATA), null, null,
                    null
                )
                cursor?.let {
                    it.moveToFirst()
                    try {
                        val path =
                            it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        val file = File(path)
                        val video = VideoDataClass(
                            id = "",
                            videoName = file.name,
                            folderName = file.parentFile.name,
                            durationText = 0L,
                            size = "",
                            path = path,
                            image = Uri.fromFile(file),
                            dateModified = file.lastModified().toString()
                        )
                        playerList.add(video)
                        cursor.close()
                    } catch (e: Exception) {
                        val tempPath = getPathFromURI(context = this, intent.data!!)
                        val tempFile = File(tempPath)
                        val video = VideoDataClass(
                            id = "",
                            videoName = tempFile.name,
                            folderName = tempFile.parentFile.name,
                            durationText = 0L,
                            size = "",
                            path = tempPath,
                            image = Uri.fromFile(tempFile),
                            dateModified = tempFile.lastModified().toString()
                        )
                        playerList.add(video)
                        cursor.close()
                    }
                }
                createPlayer()
            } else {
                initializeLayout()
            }
        } catch (e: Exception) {
            showToast(this, e.message.toString())
        }

        listeners()


        //for pip Mode to setup Control in pip Mode
        mediaSession = MediaSessionCompat(this, "YourMediaSessionTag")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val callback = object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                player.play()
            }

            override fun onPause() {
                player.pause()
            }

            override fun onSkipToNext() {
                nextPrevVideo(true)
            }

            override fun onSkipToPrevious() {
                nextPrevVideo(false)
            }
        }

        mediaSession.setCallback(callback)
        mediaSession.isActive = true

    }

    @SuppressLint("ObsoleteSdkInt")
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

        activityPlayerBinding.fastForwardPlayerActivity.setOnClickListener {
            OnDoubleClickListener(callback = object : OnDoubleClickListener.Callback {
                override fun doubleClicked() {
                    activityPlayerBinding.playerViewPlayerActivity.showController()
                    activityPlayerBinding.fastForwardPlayerActivity.visibility = View.VISIBLE
                    player.seekTo(player.currentPosition + 1000)
                }

            })
        }

        activityPlayerBinding.fastBackwardPlayerActivity.setOnClickListener {
            OnDoubleClickListener(callback = object : OnDoubleClickListener.Callback {
                override fun doubleClicked() {
                    activityPlayerBinding.playerViewPlayerActivity.showController()
                    activityPlayerBinding.fastBackwardPlayerActivity.visibility = View.VISIBLE
                    player.seekTo(player.currentPosition - 1000)
                }

            })
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

        activityPlayerBinding.lockEnablePlayerActivity.setOnClickListener {
            isLocked = true
            activityPlayerBinding.playerViewPlayerActivity.hideController()
            activityPlayerBinding.playerViewPlayerActivity.useController = false
            activityPlayerBinding.topController.visibility = View.GONE
            activityPlayerBinding.bottomController.visibility = View.GONE
            activityPlayerBinding.lockDisablePlayerActivity.visibility = View.VISIBLE
        }

        activityPlayerBinding.lockDisablePlayerActivity.setOnClickListener {
            isLocked = false
            activityPlayerBinding.playerViewPlayerActivity.useController = true
            activityPlayerBinding.playerViewPlayerActivity.showController()
            activityPlayerBinding.topController.visibility = View.VISIBLE
            activityPlayerBinding.bottomController.visibility = View.VISIBLE
            activityPlayerBinding.lockDisablePlayerActivity.visibility = View.GONE
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
            val popUpDialog = LayoutInflater.from(this)
                .inflate(R.layout.popup_more_features, activityPlayerBinding.root, false)
            val bindingPopUp = PopupMoreFeaturesBinding.bind(popUpDialog)
            val dialog =
                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle).setView(popUpDialog)
                    .create()

            dialog.show()

            val audioLanguages = ArrayList<Pair<String?, String?>>()
            val subtitleLanguages = ArrayList<Pair<String?, String?>>()

            for (i in 0 until player.currentTrackGroups.length) {
                val trackGroup = player.currentTrackGroups[i]
                for (j in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(j)
                    val mimeType = format.sampleMimeType
                    val language = format.language
                    val id = format.id
                    if (mimeType?.contains("audio") == true && id != null && language != null) {
                        audioLanguages.add(Pair(id, language))
                    } else if (mimeType?.contains("text") == true && id != null && language != null) {
                        subtitleLanguages.add(Pair(id, language))
                    }
                }
            }

            val audioNames = audioLanguages.mapIndexed { index, pair ->
                "Audio Track #${index + 1} ${pair.second}"
            }.toTypedArray()

            val subtitleNames = subtitleLanguages.mapIndexed { index, pair ->
                "Subtitle Track #${index + 1} ${pair.second}"
            }.toTypedArray()

            //for subtitle
            bindingPopUp.subtitlePopUp.setOnClickListener {
                dialog.dismiss()

                val subtitleOptions = arrayOfNulls<CharSequence>(subtitleNames.size + 1)
                subtitleOptions[0] = "Disable Subtitle"
                for (i in subtitleNames.indices) {
                    subtitleOptions[i + 1] = subtitleNames[i]
                }

                showTrackSelectionDialog(
                    this,
                    trackSelector,
                    "Select Subtitle Track",
                    subtitleOptions,
                    false,
                    subtitleLanguages
                )


            }

            //for audio Tracks
            bindingPopUp.audioTracksPopUp.setOnClickListener {
                dialog.dismiss()

                val audioOptions = arrayOfNulls<CharSequence>(audioNames.size + 1)
                audioOptions[0] = "Disable Audio"
                for (i in audioNames.indices) {
                    audioOptions[i + 1] = audioNames[i]
                }

                showTrackSelectionDialog(
                    this,
                    trackSelector,
                    "Select Audio Track",
                    audioOptions,
                    true,
                    audioLanguages
                )
            }

            //for speed I am using here slider
            bindingPopUp.speedPopUp.setOnClickListener {
                dialog.dismiss()

                val speedOptions =
                    arrayOf("0.25x", "0.50x", "0.75x", "1.0x", "1.25x", "1.50x", "1.75x", "2.0x")

                val popUpDialogSpeed = LayoutInflater.from(this)
                    .inflate(R.layout.popup_video_speed, activityPlayerBinding.root, false)
                val bindingSpeed = PopupVideoSpeedBinding.bind(popUpDialogSpeed)

                bindingSpeed.speedSlider.valueFrom = 0f
                bindingSpeed.speedSlider.valueTo = speedOptions.size - 1.toFloat()

                bindingSpeed.speedSlider.setLabelFormatter { value ->
                    speedOptions[value.toInt()]
                }

                bindingSpeed.speedSlider.value = lastSpeedIndex.toFloat()

                bindingSpeed.speedSlider.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        val selectedSpeedIndex = value.toInt()
                        val selectedSpeed = speedOptions[selectedSpeedIndex]
                        player.setPlaybackSpeed(selectedSpeed.substringBefore("x").toFloat())
                        showToast(this, "Selected playback speed: $selectedSpeed")
                        lastSpeedIndex = selectedSpeedIndex
                    }
                }

                MaterialAlertDialogBuilder(this)
                    .setTitle("Playback Speed")
                    .setView(popUpDialogSpeed)
                    .setNegativeButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }

            //for sleep using same slider as in speed
            bindingPopUp.sleepTimerPopUp.setOnClickListener {
                dialog.dismiss()

                if (!isTimerOn) {
                    timerMainDialog()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Reset Time")
                        .setMessage("Do you want to reset time? $timerText min")
                        .setPositiveButton("Yes") { _, _ ->
                            timer?.cancel()
                            isTimerOn = false
                        }
                        .setNeutralButton("Modify Timer") { _, _ ->
                            timerMainDialog()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                            showToast(this, "Sleep timer is reset.")
                            isTimerOn = false
                            timer?.cancel()
                        }
                        .setCancelable(false)
                        .create()
                        .show()

                }

            }

            //for audio booster
            bindingPopUp.audioBoosterPopUp.setOnClickListener {
                dialog.dismiss()

                val popUpDialogBooster = LayoutInflater.from(this)
                    .inflate(R.layout.popup_video_speed, activityPlayerBinding.root, false)
                val bindingPopUpBooster = PopupVideoSpeedBinding.bind(popUpDialogBooster)

                bindingPopUpBooster.speedSlider.valueFrom = 0f
                bindingPopUpBooster.speedSlider.valueTo = 100f


                MaterialAlertDialogBuilder(this, R.style.PopUpWindowStyle)
                    .setView(popUpDialogBooster)
                    .create()
                    .show()

                bindingPopUpBooster.speedSlider.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        loudnessEnhancer.setTargetGain((value * 100).toInt())
                        showToast(this, "Audio boosted to ${value.toInt()}%")
                    }
                }

            }

            //for pip Mode
            bindingPopUp.pipPopUp.setOnClickListener {
                activityPlayerBinding.topController.visibility = View.GONE
                activityPlayerBinding.bottomController.visibility = View.GONE
                activityPlayerBinding.playPausePlayerActivity.visibility = View.GONE


                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (VERSION.SDK_INT >= VERSION_CODES.O) {
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        android.os.Process.myUid(),
                        packageName
                    ) == AppOpsManager.MODE_ALLOWED
                } else {
                    false
                }

                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                    if (status) {
                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        pipStatus = 0
                    } else {
                        val intent = Intent(
                            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                } else {
                    showToast(this, "Feature Not Supported!!")
                    dialog.dismiss()
                }
            }

        }

    }

    private fun timerMainDialog() {

        val presetDurations = arrayOf("15 min", "30 min", "45 min", "1 hr")
        val timeIntervals = intArrayOf(15, 30, 45, 60)

        val customDuration = "Set Custom Time"

        val customView = LayoutInflater.from(this)
            .inflate(R.layout.popup_video_speed, activityPlayerBinding.root, false)
        val bindingSlider = PopupVideoSpeedBinding.bind(customView)

        bindingSlider.speedSlider.valueFrom = 0f
        bindingSlider.speedSlider.valueTo = presetDurations.size - 1.toFloat()
        bindingSlider.speedSlider.setLabelFormatter { value ->
            presetDurations[value.toInt()]
        }


        MaterialAlertDialogBuilder(this)
            .setTitle("Select Sleep Timer")
            .setView(customView)
            .setNeutralButton(customDuration) { _, _ ->
                timer?.cancel()
                showCustomTimePicker()
            }
            .setPositiveButton("Set") { _, _ ->
                timer?.cancel()
                val selectedDurationIndex = bindingSlider.speedSlider.value.toInt()
                startCountdownTimer(timeIntervals[selectedDurationIndex] * 60 * 1000L)
            }
            .setNegativeButton("Cancel") { self, _ ->
                self.dismiss()
            }
            .create()
            .show()
    }

    private fun showCustomTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Set Custom Time")
            .build()

        timePicker.addOnPositiveButtonClickListener { _ ->
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTimeMillis = (selectedHour * 60 + selectedMinute) * 60 * 1000L

            startCountdownTimer(selectedTimeMillis)
        }

        timePicker.show(supportFragmentManager, "CustomTimePicker")
    }

    private fun startCountdownTimer(durationMillis: Long) {
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                timerExpired()
            }
        }
        timer!!.schedule(task, durationMillis)

        val durationMinutes =
            TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        showToast(this, "Sleep timer set for $durationMinutes min")
        timerText = durationMinutes.toString()

        isTimerOn = true
    }

    private fun timerExpired() {
        runOnUiThread {
            pauseVideo()
            MaterialAlertDialogBuilder(this)
                .setTitle("Timer Expired")
                .setMessage("Do you want to exit the application?")
                .setPositiveButton("Yes") { _, _ ->
                    moveTaskToBack(true)
                    exitProcess(1)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    showToast(this, "Sleep timer is reset.")
                    isTimerOn = false
                    timer?.cancel()
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun initializeLayout() {
        when (intent.getStringExtra("class")) {
            "allVideos" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                createPlayer()
            }

            "searchView" -> {
                playerList = ArrayList()
                playerList.addAll(MainActivity.searchList)
                createPlayer()
            }

            "folderActivity" -> {
                playerList = ArrayList()
                playerList.addAll(FolderActivity.currentFolderVideos)
                createPlayer()
            }

            "nowPlaying" -> {
                playerList = ArrayList()
                activityPlayerBinding.videoTitlePlayerActivity.text = playerList[position].videoName
                activityPlayerBinding.playerViewPlayerActivity.player = player
                playVideo()
                fullScreen(enable = isFullScreen)
                visibilityControl()

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
            activityPlayerBinding.lockEnablePlayerActivity.visibility = View.VISIBLE
        }
        if (isInPictureInPictureMode) {
            activityPlayerBinding.playerViewPlayerActivity.hideController()
            activityPlayerBinding.topController.visibility = View.GONE
            activityPlayerBinding.bottomController.visibility = View.GONE
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
        nowPlayingId = playerList[position].id

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
        player.pause()
        mediaSession.release()

    }


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (pipStatus != 0) {
            finish()
            val intent = Intent(this, PlayerActivity::class.java)
            when (pipStatus) {
                1 -> intent.putExtra("class", "folderActivity")
                2 -> intent.putExtra("class", "searchView")
                3 -> intent.putExtra("class", "allVideos")
                4 -> intent.putExtra("class", "nowPlaying")
            }
            activityPlayerBinding.playerViewPlayerActivity.showController()
            activityPlayerBinding.topController.visibility = View.VISIBLE
            activityPlayerBinding.bottomController.visibility = View.VISIBLE
            startActivity(intent)
        }
        if (!isInPictureInPictureMode) pauseVideo()
    }
}