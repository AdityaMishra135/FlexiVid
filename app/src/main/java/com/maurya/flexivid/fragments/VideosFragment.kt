package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SearchEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.ComponentActivity
import androidx.annotation.MenuRes
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.MainActivity.Companion.videoList
import com.maurya.flexivid.R
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.util.OnItemClickListener
import kotlinx.coroutines.MainScope


class VideosFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentVideosBinding: FragmentVideosBinding
    private lateinit var adapterVideo: AdapterVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }
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


    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view, menu)
        val searchItem = menu.findItem(R.id.searchViewVideo)
        val searchView = searchItem?.actionView as SearchView?

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    MainActivity.searchList = ArrayList()
                    for (video in videoList) {
                        if (video.videoName.lowercase().contains(newText.lowercase())) {
                            MainActivity.searchList.add(video)
                        }
                    }
                    MainActivity.search = true
                    adapterVideo.updateSearchList(MainActivity.searchList)
                }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }


}