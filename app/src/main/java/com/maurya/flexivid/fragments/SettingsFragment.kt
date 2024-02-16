package com.maurya.flexivid.fragments

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
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.FragmentSettingsBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.databinding.PopupAboutDialogBinding
import com.maurya.flexivid.databinding.PopupMoreFeaturesBinding
import com.maurya.flexivid.databinding.PopupThemeBinding
import com.maurya.flexivid.util.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var fragmentSettingsBinding: FragmentSettingsBinding

    private val themeList = arrayOf("Light Mode", "Dark Mode", "Auto")
    private val customThemeList = arrayOf(R.style.themeLightGreen, R.style.themeLightred)

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    companion object {
        var themeIndex: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = fragmentSettingsBinding.root

        sharedPreferencesHelper = SharedPreferenceHelper((requireContext()))

        fragmentSettingsBinding.darkModeText.text =
            "Theme: ${themeList[sharedPreferencesHelper.theme]}"

        requireContext().theme.applyStyle(customThemeList[themeIndex], true)

        listeners()
        return view
    }

    private fun listeners() {

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

            dialog.setOnDismissListener {
                dialog.dismiss()
            }
        }

        fragmentSettingsBinding.uiSkinLayout.setOnClickListener {
            val customView = layoutInflater.inflate(R.layout.popup_theme, null)
            val bindingCustomTheme = PopupThemeBinding.bind(customView)
            val dialogTheme =
                MaterialAlertDialogBuilder(requireContext()).setView(customView)
                    .setTitle("Custom Theme Selection")
                    .setView(customView)
                    .setPositiveButton("Apply") { _, _ ->

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

            dialogTheme.show()

            when (themeIndex) {
                0 -> bindingCustomTheme.themeLightGreenPopUpTheme.setBackgroundColor(
                    Color.GRAY
                )

                1 -> bindingCustomTheme.themeYellowPopUpTheme.setBackgroundColor(Color.GRAY)

                2 -> bindingCustomTheme.themeLightBluePopUpTheme.setBackgroundColor(
                    Color.GRAY
                )

                3 -> bindingCustomTheme.themeLightRedPopUpTheme.setBackgroundColor(Color.GRAY)

                4 -> bindingCustomTheme.themePinkPopUpTheme.setBackgroundColor(Color.GRAY)
                5 -> bindingCustomTheme.themePurplePopUpTheme.setBackgroundColor(Color.GRAY)
                6 -> bindingCustomTheme.themeLightOrangePopUpTheme.setBackgroundColor(
                    Color.GRAY
                )

                7 ->
                    bindingCustomTheme.themeBluePopUpTheme.setBackgroundColor(Color.GRAY)

                8 -> bindingCustomTheme.showColourPickUpPopUpTheme.setBackgroundColor(
                    Color.GRAY
                )

            }

            bindingCustomTheme.themeLightGreenPopUpTheme.setOnClickListener {
                themeIndex = 0
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themeYellowPopUpTheme.setOnClickListener {
                themeIndex = 1
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themeLightBluePopUpTheme.setOnClickListener {
                themeIndex = 2
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themeLightRedPopUpTheme.setOnClickListener {
                themeIndex = 3
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themePinkPopUpTheme.setOnClickListener {
                themeIndex = 4
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themePurplePopUpTheme.setOnClickListener {
                themeIndex = 5
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themeLightOrangePopUpTheme.setOnClickListener {
                themeIndex = 6
                dialogTheme.dismiss()
            }

            bindingCustomTheme.themeBluePopUpTheme.setOnClickListener {
                themeIndex = 7
                dialogTheme.dismiss()
            }

            bindingCustomTheme.showColourPickUpPopUpTheme.setOnClickListener {
                themeIndex = 8
                dialogTheme.dismiss()
            }

        }



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

        fragmentSettingsBinding.feedbackLayout.setOnClickListener {
            val websiteUrl =
                "https://github.com/notrealmaurya"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
            startActivity(intent)
        }

        fragmentSettingsBinding.exitLayout.setOnClickListener {
            requireActivity().finish()
        }
    }

}