package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.R
import com.maurya.flexivid.databinding.FragmentFoldersBinding


class FoldersFragment : Fragment() {

    private lateinit var fragmentFoldersBinding: FragmentFoldersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFoldersBinding = FragmentFoldersBinding.inflate(inflater, container, false)
        val view = fragmentFoldersBinding.root




        listeners()
        return view
    }

    private fun listeners() {


    }


}