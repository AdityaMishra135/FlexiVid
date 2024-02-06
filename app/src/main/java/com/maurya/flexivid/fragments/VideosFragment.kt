package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity.Companion.videoList
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.util.OnItemClickListener


class VideosFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentVideosBinding: FragmentVideosBinding
    private lateinit var adapterVideo: AdapterVideo


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentVideosBinding = FragmentVideosBinding.inflate(inflater, container, false)
        val view = fragmentVideosBinding.root





        fragmentVideosBinding.recyclerViewVideosFragment.setHasFixedSize(true)
        fragmentVideosBinding.recyclerViewVideosFragment.setItemViewCacheSize(13)
        fragmentVideosBinding.recyclerViewVideosFragment.layoutManager =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
        adapterVideo = AdapterVideo(requireContext(), this, videoList)
        fragmentVideosBinding.recyclerViewVideosFragment.adapter = adapterVideo


        listeners()
        return view
    }

    private fun listeners() {

    }

    override fun onItemClickListener(position: Int) {


    }


}