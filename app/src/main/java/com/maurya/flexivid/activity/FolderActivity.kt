package com.maurya.flexivid.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.ActivityFolderBinding
import com.maurya.flexivid.databinding.ActivityMainBinding

class FolderActivity : AppCompatActivity() {

    private lateinit var activityFolderBinding: ActivityFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderBinding = ActivityFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderBinding.root)
    }
}