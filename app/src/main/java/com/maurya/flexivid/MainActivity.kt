package com.maurya.flexivid

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.databinding.ActivityMainBinding
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.getAllVideos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding


    //    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    companion object {
        var videoList: ArrayList<VideoDataClass> = arrayListOf()
        var searchList: ArrayList<VideoDataClass> = arrayListOf()
        var folderList: ArrayList<FolderDataClass> = arrayListOf()
        var search: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sharedPreferencesHelper = SharedPreferenceHelper(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.navigate(R.id.videosFragment)
//        navController.navigate(R.id.settingsFragment)


        //ui skin
        /*
        val drawableIds = arrayOf(
            R.drawable.icon_theme,
            R.drawable.icon_ui_skin,
            R.drawable.icon_feedback,
            R.drawable.icon_about,
            R.drawable.icon_video,
            R.drawable.icon_folder,
            R.drawable.icon_settings,
            R.drawable.icon_rightarrow,
            R.drawable.icon_exit,
        )

        drawableIds.forEachIndexed { _, drawableId ->
            tintDrawable(
                this,
                drawableId,
                sharedPreferencesHelper.colorList[sharedPreferencesHelper.getUiColor()]
            )
        }
        */



        binding.bottomNavMainActivity.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.foldersBottomNav -> navController.navigate(R.id.foldersFragment)
                R.id.videosBottomNav -> navController.navigate(R.id.videosFragment)
                R.id.settingsBottomNav -> navController.navigate(R.id.settingsFragment)
                else -> navController.navigate(R.id.videosFragment)
            }

            true
        }

        permission()



        lifecycleScope.launch {
//            videoList= getAllVideos(applicationContext,20,0)
            videoList = getAllVideos(applicationContext)
        }

    }


    fun visibilityBottomNav(visible: Boolean) {
        if (!visible) {
            binding.bottomNavMainActivity.visibility = View.VISIBLE
        } else {
            binding.bottomNavMainActivity.visibility = View.GONE
        }
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ), 1
                )
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 2
                )
            }
        } else {
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 || requestCode == 2) {
            var allPermissionsGranted = true
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (!allPermissionsGranted) {
                showPermissionRequiredDialog()
            }
        }
    }


    private fun showPermissionRequiredDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
            .setMessage("This permission is required to access the app.")
            .setPositiveButton(
                "Go to Settings"
            ) { _, _ ->
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.setData(uri)
                startActivity(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }


}