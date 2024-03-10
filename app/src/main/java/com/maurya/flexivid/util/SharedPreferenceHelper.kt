package com.maurya.flexivid.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.maurya.flexivid.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceHelper @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(context.packageName, AppCompatActivity.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()
    private val keyTheme = "theme"
    var theme
        get() = sharedPreferences.getInt(keyTheme, 2)
        set(value) {
            editor.putInt(keyTheme, value)
            editor.apply()
        }

    val themeFlag = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )


    fun saveSortingOrder(sortingOrder: String) {
        sharedPreferences.edit().putString("sorting_order", sortingOrder).apply()
    }

    fun getSortingOrder(): String? {
        return sharedPreferences.getString("sorting_order", null)
    }



    //Ui SKin
    /*
    val colorList = listOf(
        R.color.light_green,
        R.color.yellow,
        R.color.light_blue,
        R.color.light_red,
        R.color.pink,
        R.color.purple,
        R.color.light_orange,
        R.color.deep_blue,
        R.color.light_brown
    )
    fun saveUiColor(color: Int) {
        sharedPreferences.edit().putInt("view_color", color).apply()
    }

    fun getUiColor(): Int {
        return sharedPreferences.getInt("view_color", -1)
    }
     */
}

