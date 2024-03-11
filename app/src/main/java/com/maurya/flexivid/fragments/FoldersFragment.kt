package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.database.AdapterFolder
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.util.OnItemClickListener
import java.io.File


class FoldersFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentFoldersBinding: FragmentFoldersBinding
    private lateinit var adapterFolder: AdapterFolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFoldersBinding = FragmentFoldersBinding.inflate(inflater, container, false)
        val view = fragmentFoldersBinding.root


        fragmentFoldersBinding.recyclerViewFoldersFragment.setHasFixedSize(true)
        fragmentFoldersBinding.recyclerViewFoldersFragment.setItemViewCacheSize(13)
        fragmentFoldersBinding.recyclerViewFoldersFragment.layoutManager =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
        adapterFolder = AdapterFolder(requireContext(), this, MainActivity.folderList)
        fragmentFoldersBinding.recyclerViewFoldersFragment.adapter = adapterFolder


        listeners()
        return view
    }

    private fun listeners() {


    }


    override fun onItemClickListener(position: Int) {


    }

    override fun onItemLongClickListener(position: Int) {


    }


}