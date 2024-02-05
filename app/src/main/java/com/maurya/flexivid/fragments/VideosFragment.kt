package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding


class VideosFragment : Fragment() {

    private lateinit var fragmentVideosBinding: FragmentVideosBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentVideosBinding = FragmentVideosBinding.inflate(inflater, container, false)
        val view = fragmentVideosBinding.root




        listeners()
        return view
    }

    private fun listeners() {

    }


}