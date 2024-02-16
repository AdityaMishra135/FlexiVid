package com.maurya.flexivid

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.maurya.flexivid.util.SharedPreferenceHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApplicationFlexiVid : Application() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(sharedPreferencesHelper.themeFlag[sharedPreferencesHelper.theme])

        setTheme(sharedPreferencesHelper.getUiColor())


    }
}