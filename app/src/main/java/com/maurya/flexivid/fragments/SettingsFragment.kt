package com.maurya.flexivid.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.FragmentSettingsBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.util.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var fragmentSettingsBinding: FragmentSettingsBinding

    private val themeList = arrayOf("Light Mode", "Dark Mode", "Auto")

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = fragmentSettingsBinding.root

        sharedPreferencesHelper = SharedPreferenceHelper((requireContext()))

        fragmentSettingsBinding.darkModeText.text =
            "Theme: ${themeList[sharedPreferencesHelper.theme]}"


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

        fragmentSettingsBinding.aboutLayout.setOnClickListener {
            val customDialogFragment = AboutDialogFragment()
            customDialogFragment.show(childFragmentManager, "CustomDialogFragment")
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