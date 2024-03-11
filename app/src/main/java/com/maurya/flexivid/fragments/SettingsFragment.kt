package com.maurya.flexivid.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.FragmentSettingsBinding
import com.maurya.flexivid.databinding.PopupAboutDialogBinding
import com.maurya.flexivid.databinding.PopupThemeBinding
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var fragmentSettingsBinding: FragmentSettingsBinding

    private val themeList = arrayOf("Light Mode", "Dark Mode", "Auto")


    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    companion object{
        var isInitialized:Boolean =false
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = fragmentSettingsBinding.root

        isInitialized=true

        sharedPreferencesHelper = SharedPreferenceHelper(requireContext())

        fragmentSettingsBinding.darkModeText.text =
            "Theme: ${themeList[sharedPreferencesHelper.theme]}"

        listeners()


        return view

    }

    private fun listeners() {

        //Theme
        fragmentSettingsBinding.themeLayout.setOnClickListener {
            var checkedTheme = sharedPreferencesHelper.theme
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change theme")
                .setPositiveButton("Ok") { _, _ ->
                    sharedPreferencesHelper.theme = checkedTheme
                    AppCompatDelegate.setDefaultNightMode(sharedPreferencesHelper.themeFlag[checkedTheme])
                    fragmentSettingsBinding.darkModeText.text =
                        "Theme: ${themeList[sharedPreferencesHelper.theme]}"
                }
                .setSingleChoiceItems(themeList, checkedTheme) { _, which ->
                    checkedTheme = which
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()

            dialog.setCanceledOnTouchOutside(true)

            dialog.setOnDismissListener {
                dialog.dismiss()
            }
        }

        //Ui Skin
        fragmentSettingsBinding.uiSkinLayout.setOnClickListener {
            showToast(requireContext(), "Feature Coming Soon...")

            /*
            val customView = layoutInflater.inflate(R.layout.popup_theme, null)
            val bindingCustomTheme = PopupThemeBinding.bind(customView)

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(customView)
                .create()

            dialog.setCanceledOnTouchOutside(true)
            dialog.show()

uiIndex = sharedPreferencesHelper.getUiColor()

            when (uiIndex) {
                0 -> bindingCustomTheme.themeLightGreenPopUpTheme.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle)

                1 -> bindingCustomTheme.themeYellowPopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                2 -> bindingCustomTheme.themeLightBluePopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                3 -> bindingCustomTheme.themeLightRedPopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                4 -> bindingCustomTheme.themePinkPopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                5 -> bindingCustomTheme.themePurplePopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                6 -> bindingCustomTheme.themeLightOrangePopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                7 -> bindingCustomTheme.themeBluePopUpTheme.setBackgroundResource(R.drawable.bg_circle)
                8 -> bindingCustomTheme.themeLightBrownPopUpTheme.setBackgroundResource(R.drawable.bg_circle)
            }

            bindingCustomTheme.themeLightGreenPopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(0)
////                requireActivity().recreate()
//                restartApp(requireContext())
            }

            bindingCustomTheme.themeYellowPopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(1)
//                restartApp(requireContext())
            }

            bindingCustomTheme.themeLightBluePopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(2)
//                restartApp(requireContext())
            }

            bindingCustomTheme.themeLightRedPopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(3)
//                restartApp(requireContext())
            }

            bindingCustomTheme.themePinkPopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(4)
//                restartApp(requireContext())
            }

            bindingCustomTheme.themePurplePopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(5)
                restartApp(requireContext())
            }

            bindingCustomTheme.themeLightOrangePopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(6)
                restartApp(requireContext())
            }

            bindingCustomTheme.themeBluePopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(7)
                restartApp(requireContext())
            }

            bindingCustomTheme.themeLightBrownPopUpTheme.setOnClickListener {
                sharedPreferencesHelper.saveUiColor(8)
                restartApp(requireContext())
            }


             */

        }

        //About
        fragmentSettingsBinding.aboutLayout.setOnClickListener {
            val popUpDialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.popup_about_dialog, fragmentSettingsBinding.root, false)
            val bindingPopUp = PopupAboutDialogBinding.bind(popUpDialog)
            val dialog =
                MaterialAlertDialogBuilder(requireContext(), R.style.PopUpWindowStyle).setView(
                    popUpDialog
                )
                    .setOnCancelListener {

                    }
                    .create()

            bindingPopUp.aboutDialogThankyouButton.setOnClickListener {
                dialog.dismiss()
            }

            val textView = bindingPopUp.spannableTextViewDialog
            val spannableString =
                SpannableString("If you'd like to share your thoughts or provide Feedback , please feel free to do so. Your input is valuable, and I'd appreciate hearing from you.❤\uFE0F\"\n ")

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val websiteUrl =
                        "https://forms.gle/4gC2XzHDCaio7hUh8"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                    startActivity(intent)
                }
            }

            spannableString.setSpan(
                clickableSpan,
                48, 56,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val blueColor = Color.BLUE
            spannableString.setSpan(
                ForegroundColorSpan(blueColor),
                48, 56,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()

            dialog.show()

        }

        //Feedback
        fragmentSettingsBinding.feedbackLayout.setOnClickListener {
            val websiteUrl =
                "https://github.com/notrealmaurya"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
            startActivity(intent)
        }

        //Exit App
        fragmentSettingsBinding.exitLayout.setOnClickListener {
            requireActivity().finish()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        isInitialized =false
    }
}