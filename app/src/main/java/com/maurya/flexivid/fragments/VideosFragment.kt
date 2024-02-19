package com.maurya.flexivid.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SearchEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.activity.ComponentActivity
import androidx.annotation.MenuRes
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.MainActivity.Companion.videoList
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.sendIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import javax.inject.Inject


@AndroidEntryPoint
class VideosFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentVideosBinding: FragmentVideosBinding
    private lateinit var adapterVideo: AdapterVideo

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        fragmentVideosBinding.nowPlayingVideoFragment.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("class", "nowPlaying")
            startActivity(intent)

        }

        fragmentVideosBinding.sortingVideoFragment.setOnClickListener {
            showSortingMenu(view)
        }


        val sortingOrder = sharedPreferencesHelper.getSortingOrder()
        sortMusicList(sortingOrder.toString())


        return view
    }


    private fun showSortingMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.sort_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.newestDatefirst -> {
                    sortMusicList("newest_date_first")
                    true
                }

                R.id.oldestDatefirst -> {
                    sortMusicList("oldest_date_first")
                    true
                }

                R.id.largestSizefirst -> {
                    sortMusicList("largest_size_first")
                    true
                }

                R.id.smallestSizefirst -> {
                    sortMusicList("smallest_size_first")
                    true
                }

                R.id.nameAtoZ -> {
                    sortMusicList("a_to_z")
                    true
                }

                R.id.nameZtoA -> {
                    sortMusicList("z_to_a")
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun sortMusicList(sortBy: String) {
        when (sortBy) {
            "newest_date_first" -> videoList.sortByDescending { it.dateModified }
            "oldest_date_first" -> videoList.sortBy { it.dateModified }
            "largest_size_first" -> videoList.sortByDescending { it.durationText }
            "smallest_size_first" -> videoList.sortBy { it.durationText }
            "a_to_z" -> videoList.sortBy { it.videoName }
            "z_to_a" -> videoList.sortByDescending { it.videoName }
            else -> {
                videoList.sortByDescending { it.dateModified }
            }
        }

        adapterVideo.updateVideoList(videoList)
        adapterVideo.notifyDataSetChanged()
        sharedPreferencesHelper.saveSortingOrder(sortBy)
    }

    override fun onItemClickListener(position: Int) {

    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.position != -1) {
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE
        }
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