package com.maurya.flexivid.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.net.Uri
import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.io.Files.getFileExtension
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.databinding.PopupAboutDialogBinding
import com.maurya.flexivid.databinding.PopupDetailsBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.getAllVideos
import com.maurya.flexivid.util.getFormattedDate
import com.maurya.flexivid.util.getFormattedFileSize
import com.maurya.flexivid.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class VideosFragment : Fragment(), OnItemClickListener {

    private lateinit var adapterVideo: AdapterVideo


    var isLoading = false
    var isLastPage = false
    var currentPage = 0
    companion object {
        var videoList: ArrayList<VideoDataClass> = arrayListOf()
        lateinit var fragmentVideosBinding: FragmentVideosBinding
    }

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentVideosBinding = FragmentVideosBinding.inflate(inflater, container, false)
        val view = fragmentVideosBinding.root

        fragmentVideosBinding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
           videoList = getAllVideos(requireContext(),20,0)
        }

        val layoutManagerR = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        fragmentVideosBinding.recyclerViewVideosFragment.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = layoutManagerR
            adapterVideo = AdapterVideo(requireContext(), this@VideosFragment, videoList)
            adapter = adapterVideo
        }



        fragmentVideosBinding.recyclerViewVideosFragment.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManagerR.childCount
                val totalItemCount = layoutManagerR.itemCount
                val firstVisibleItemPosition = layoutManagerR.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE
                    ) {
                        isLoading = true
                        currentPage++
                        // Fetch next page of data
                        lifecycleScope.launch {
                            val newItems = getAllVideos(requireContext(), currentPage * PAGE_SIZE, PAGE_SIZE)
                            if (newItems.isNotEmpty()) {
                                adapterVideo.addItems(newItems)
                            } else {
                                isLastPage = true
                            }
                            isLoading = false
                        }
                    }
                }
            }
        })

        fragmentVideosBinding.nowPlayingVideoFragment.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("class", "nowPlaying")
            startActivity(intent)

        }

        fragmentVideosBinding.sortingVideoFragment.setOnClickListener {
            showSortingMenu()
        }

        fragmentVideosBinding.topToolbarClose.setOnClickListener {
            changeVisibility(false)
        }


        val sortingOrder = sharedPreferencesHelper.getSortingOrder()
        sortMusicList(sortingOrder.toString())


        changeVisibility(false)

        return view
    }


    private fun showSortingMenu() {

        val inflater =
            requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_home_menu, null)

        val wid = LinearLayout.LayoutParams.WRAP_CONTENT
        val high = LinearLayout.LayoutParams.WRAP_CONTENT
        val focus = true
        val popupWindow = PopupWindow(popupView, wid, high, focus)

        val location = IntArray(2)
        fragmentVideosBinding.sortingVideoFragment.getLocationOnScreen(location)
        val x = location[0] + fragmentVideosBinding.sortingVideoFragment.width
        val y = location[1] + fragmentVideosBinding.sortingVideoFragment.height


        popupWindow.showAtLocation(fragmentVideosBinding.root, Gravity.NO_GRAVITY, x, y)


        val layoutIds = arrayOf(
            R.id.nameAtoZLayoutPopUpMenu,
            R.id.nameZtoALayoutPopUpMenu,
            R.id.largestFirstLayoutPopUpMenu,
            R.id.smallestFirstLayoutPopUpMenu,
            R.id.oldestFirstLayoutPopUpMenu,
            R.id.newestFirstLayoutPopUpMenu
        )

        val sortOptions = arrayOf(
            "a_to_z", "z_to_a", "largest_size_first",
            "smallest_size_first", "oldest_date_first", "newest_date_first"
        )

        layoutIds.forEachIndexed { index, layoutId ->
            val layout = popupView.findViewById<LinearLayout>(layoutId)
            layout.setOnClickListener {
                sortMusicList(sortOptions[index])
                popupWindow.dismiss()
            }
        }


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

    private fun changeVisibility(visible: Boolean) {
        if (!visible) {
            (activity as MainActivity).visibilityBottomNav(false)
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.videoNameLayout.visibility = View.VISIBLE
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE


        } else {
            (activity as MainActivity).visibilityBottomNav(true)
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.videoNameLayout.visibility = View.GONE
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.GONE
        }

    }

    override fun onItemLongClickListener(position: Int) {
        changeVisibility(true)


        val selectedFiles = videoList.filter { it.isChecked }



        bottomDialogFunction(position, selectedFiles)


    }


    @SuppressLint("SetTextI18n", "InflateParams", "NotifyDataSetChanged")
    private fun bottomDialogFunction(
        position: Int,
        selectedFiles: List<VideoDataClass>
    ) {

        if (selectedFiles.size < 2) {

            fragmentVideosBinding.bottomRenameVideoFragment.setOnClickListener {
                val tempFile = adapterVideo.getFile(position)
                val renameSheetDialog =
                    BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
                val renameSheetView: View =
                    LayoutInflater.from(requireContext()).inflate(R.layout.popup_rename, null)
                renameSheetDialog.setContentView(renameSheetView)
                renameSheetDialog.setCanceledOnTouchOutside(true)

                val renameEditText = renameSheetView.findViewById<EditText>(R.id.rename_EditText)
                val renameCancelText =
                    renameSheetView.findViewById<TextView>(R.id.rename_CancelText)
                val renameOKText = renameSheetView.findViewById<TextView>(R.id.rename_OKText)

                renameEditText.requestFocus()
                renameEditText.setText(tempFile.videoName)
                renameEditText.setSelection(0, tempFile.videoName.lastIndexOf('.'))
                renameSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                renameCancelText.setOnClickListener {
                    renameSheetDialog.dismiss()
                }

                renameOKText.setOnClickListener {
                    val newName = renameEditText.text.toString().trim()

                    if (newName.isNotEmpty()) {
                        val currentFile = File(tempFile.path)
                        val newFile =
                            File(currentFile.parentFile, "$newName.${currentFile.extension}")

                        if (!newFile.exists() || newFile == currentFile) {
                            if (currentFile.renameTo(newFile)) {
                                MediaScannerConnection.scanFile(
                                    requireContext(),
                                    arrayOf(newFile.toString()),
                                    arrayOf("video/*"),
                                    null
                                )
                                videoList[position].apply {
                                    videoName = newName
                                    path = newFile.path
                                    image = Uri.fromFile(newFile)
                                }
                                adapterVideo.notifyItemChanged(position)
                            } else {
                                showToast(requireContext(), "Error in renaming File!!")
                            }
                        } else {
                            showToast(requireContext(), "File with the same name already exists!")
                        }
                    } else {
                        showToast(requireContext(), "Please provide a valid name!")
                    }
                    renameSheetDialog.dismiss()
                }

                changeVisibility(false)
                renameSheetDialog.show()
            }

        } else {
            fragmentVideosBinding.bottomRenameIMG.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                ), PorterDuff.Mode.SRC_ATOP
            )
            fragmentVideosBinding.bottomRenameTXT.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
        }


        fragmentVideosBinding.bottomDetailsVideoFragment.setOnClickListener {

            val popUpDialog = LayoutInflater.from(requireContext())
                .inflate(R.layout.popup_details, fragmentVideosBinding.root, false)

            val bindingPopUp = PopupDetailsBinding.bind(popUpDialog)
            val dialog =
                MaterialAlertDialogBuilder(requireContext(), R.style.PopUpWindowStyle).setView(
                    popUpDialog
                )
                    .setOnCancelListener {
                        it.dismiss()
                    }
                    .create()


            val popupDetailsNameText = bindingPopUp.popupDetailsNameText
            val popupDetailsPathText = bindingPopUp.popupDetailsPathText
            val popupDetailsSizeText = bindingPopUp.popupDetailsSizeText
            val popupDetailsLastModifiedText = bindingPopUp.popupDetailsSizeText
            val popupDetailsOKText = bindingPopUp.popupDetailsOKText

            if (selectedFiles.size == 1) {
                val selectedFile = selectedFiles[0]
                popupDetailsNameText.text = "Name: ${selectedFile.videoName}"
                popupDetailsPathText.text = "Path: ${selectedFile.path}"
            } else {
                popupDetailsNameText.visibility = View.GONE
                popupDetailsPathText.visibility = View.GONE
            }

            var totalSizeInBytes: Long = 0
            selectedFiles.forEach { selectedFile ->
                totalSizeInBytes += selectedFile.size.toLong()
            }
            popupDetailsSizeText.text = "Total Size: ${getFormattedFileSize(totalSizeInBytes)}"
            val lastModified = selectedFiles.maxByOrNull { it.dateModified }?.dateModified ?: 0
            popupDetailsLastModifiedText.text = lastModified.toString()

            popupDetailsOKText.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        fragmentVideosBinding.bottomSendVideoFragment.setOnClickListener {

            if (selectedFiles.isNotEmpty()) {
                val fileUris = ArrayList<Uri>()
                val fileNames = ArrayList<String>()

                for (selectedFile in selectedFiles) {
                    val file = File(selectedFile.path)
                    val fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        file
                    )
                    fileUris.add(fileUri)
                    fileNames.add(file.name)
                }

                if (fileUris.isNotEmpty()) {
                    val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
                    shareIntent.type = "*/*"
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Share ${fileNames.size} files"
                        )
                    )
                }
            }
        }


        fragmentVideosBinding.bottomDeleteVideoFragment.setOnClickListener {

            val deleteSheetDialog =
                BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
            val deleteSheetView = layoutInflater.inflate(R.layout.popup_delete, null)
            deleteSheetDialog.setContentView(deleteSheetView)
            deleteSheetDialog.setCanceledOnTouchOutside(true)
            val deleteSelectedText =
                deleteSheetView.findViewById<TextView>(R.id.deleteSelectedText)
            val deleteDeleteText = deleteSheetView.findViewById<TextView>(R.id.deleteDeleteText)
            val deleteCancelText = deleteSheetView.findViewById<TextView>(R.id.deleteCancelText)

            deleteSelectedText.text = "Delete $selectedFiles selected items"

            deleteDeleteText.setOnClickListener {
                val toDelete = videoList.filter { it.isChecked }
                toDelete.forEach { videoItem ->
                    val file = File(videoItem.path)
                    if (file.exists()) {
                        file.delete()
                        MediaScannerConnection.scanFile(
                            requireContext(), arrayOf(file.toString()),
                            arrayOf("video/*"), null
                        )
                    }
                }
                videoList.removeAll(toDelete.toSet())
                adapterVideo.notifyDataSetChanged()
                deleteSheetDialog.dismiss()
            }

            deleteCancelText.setOnClickListener {
                deleteSheetDialog.dismiss()
            }

            deleteSheetDialog.show()


        }


    }


    override fun onResume() {
        super.onResume()
        if (PlayerActivity.position != -1) {
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            videoList = getAllVideos(requireContext(),20,0)
            fragmentVideosBinding.progressBar.visibility = View.GONE
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.search_view, menu)
//        val searchItem = menu.findItem(R.id.searchViewVideo)
//        val searchView = searchItem?.actionView as SearchView?
//
//        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText != null) {
//                    MainActivity.searchList = ArrayList()
//                    for (video in videoList) {
//                        if (video.videoName.lowercase().contains(newText.lowercase())) {
//                            MainActivity.searchList.add(video)
//                        }
//                    }
//                    MainActivity.search = true
//                    adapterVideo.updateSearchList(MainActivity.searchList)
//                }
//                return true
//            }
//        })
//
//        super.onCreateOptionsMenu(menu, inflater)
//    }


}