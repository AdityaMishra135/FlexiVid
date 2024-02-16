package com.maurya.flexivid.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceHelper @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(context.packageName, AppCompatActivity.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()
    private val keyTheme = "theme"

    var theme: Int
        get() = sharedPreferences.getInt(keyTheme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) {
            editor.putInt(keyTheme, value)
            editor.apply()
        }


    val themeFlag = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )


    fun saveUiColor(themeIndex: Int) {
        editor.putInt(keyTheme, themeIndex)
        editor.apply()
    }

    fun getUiColor(): Int {
        return sharedPreferences.getInt(keyTheme, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
    }

}
