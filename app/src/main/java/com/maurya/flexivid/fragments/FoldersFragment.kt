package com.maurya.flexivid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.dataEntities.FolderDataClass
import com.maurya.flexivid.database.AdapterFolder
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentFoldersBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.sortMusicList
import com.maurya.flexivid.viewModelsObserver.ModelResult
import com.maurya.flexivid.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class FoldersFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentFoldersBinding: FragmentFoldersBinding
    private lateinit var adapterFolder: AdapterFolder

    companion object {
        var folderList: ArrayList<FolderDataClass> = arrayListOf()
    }

    private val viewModel by viewModels<ViewModelObserver>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFoldersBinding = FragmentFoldersBinding.inflate(inflater, container, false)
        val view = fragmentFoldersBinding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(viewModel)
        viewModel.fetchFolders(requireContext())

        fragmentFoldersBinding.recyclerViewFoldersFragment.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterFolder = AdapterFolder(requireContext(), this@FoldersFragment, folderList)
            adapter = adapterFolder
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foldersStateFLow.collect {
                    fragmentFoldersBinding.progressBar.visibility = View.GONE
                    when (it) {
                        is ModelResult.Success -> {
                            folderList.clear()
                            folderList.addAll(it.data!!)
                            adapterFolder.notifyDataSetChanged()
                        }

                        is ModelResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is ModelResult.Loading -> {
                            fragmentFoldersBinding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }

        listeners()
    }

    private fun listeners() {


    }


    override fun onItemClickListener(position: Int) {


    }

    override fun onItemLongClickListener(position: Int) {


    }


}