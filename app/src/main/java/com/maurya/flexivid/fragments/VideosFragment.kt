package com.maurya.flexivid.fragments

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.io.Files.getFileExtension
import com.maurya.flexivid.MainActivity.Companion.videoList
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class VideosFragment : Fragment(), OnItemClickListener {

    private lateinit var fragmentVideosBinding: FragmentVideosBinding
    private lateinit var adapterVideo: AdapterVideo

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper


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
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.videoNameLayout.visibility = View.VISIBLE
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE
        } else {
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.videoNameLayout.visibility = View.GONE
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.GONE
        }

    }

    override fun onItemLongClickListener(currentFile: File, position: Int) {
        changeVisibility(true)




        bottomDialogFunction(position, currentFile, videoList)


    }


    private fun bottomDialogFunction(
        position: Int,
        currentFile: File,
        videoList: ArrayList<VideoDataClass>
    ) {

        //detail option
//        if (selectedFiles.size == 1) {
//            fragmentVideosBinding.bottomDetailsVideoFragment.isClickable = true
//            fragmentVideosBinding.bottomDetailsVideoFragment.setOnClickListener {
//                val DetailSheetDialog =
//                    BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
//                val detailsSheetView: View =
//                    getLayoutInflater().inflate(R.layout.popup_details, null)
//                DetailSheetDialog.setContentView(detailsSheetView)
//                DetailSheetDialog.setCanceledOnTouchOutside(true)
//                val popupDetailsNameText =
//                    detailsSheetView.findViewById<TextView>(R.id.popupDetailsNameText)
//                val popupDetailsPathText =
//                    detailsSheetView.findViewById<TextView>(R.id.popupDetailsPathText)
//                val popupDetailsSizeText =
//                    detailsSheetView.findViewById<TextView>(R.id.popupDetailsSizeText)
//                val popupDetailsLastModifiedText =
//                    detailsSheetView.findViewById<TextView>(R.id.popupDetailsLastModifiedText)
//                val popupDetailsOKText =
//                    detailsSheetView.findViewById<TextView>(R.id.popupDetailsOKText)
//                selectedFile = fileAdapter.getFile(position)
//                val builder = StringBuilder()
//                val pathBuilder = StringBuilder()
//                for (file in selectedFiles) {
//                    builder.append(file.name)
//                    builder.append("\n")
//                    pathBuilder.append(file.path)
//                    pathBuilder.append("\n")
//                }
//                val selectedFileText = builder.toString().trim { it <= ' ' }
//                val selectedFilesPaths = pathBuilder.toString().trim { it <= ' ' }
//                val lastModified: Long = selectedFile.lastModified()
//                val formattedLastModified: String = formatLastModified(lastModified)
//                if (selectedFile != null) {
//                    popupDetailsNameText.text = selectedFileText
//                    popupDetailsPathText.text = selectedFilesPaths
//                    if (selectedFile.isDirectory()) {
//                        val totalSize: Long = getTotalDirectorySize(selectedFile)
//                        popupDetailsSizeText.setText(formatFileSize(totalSize))
//                    } else {
//                        val fileSize: Long = selectedFile.length()
//                        popupDetailsSizeText.setText(formatFileSize(fileSize))
//                    }
//                    popupDetailsLastModifiedText.text = formattedLastModified
//                }
//                popupDetailsOKText.setOnClickListener { // Handle OK button click
//                    DetailSheetDialog.dismiss()
//                }
//                changeVisibility(false)
//                DetailSheetDialog.show()
//            }
//        } else {
//            fragmentVideosBinding.bottomDetailsVideoFragment.isClickable = false
//        }

        //rename option
//        if (selectedFiles.size == 1) {
        fragmentVideosBinding.bottomRenameVideoFragment.isClickable = true
        fragmentVideosBinding.bottomRenameVideoFragment.setOnClickListener {
            val tempFile = adapterVideo.getFile(position)
            val renameSheetDialog =
                BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
            val renameSheetView: View =
                getLayoutInflater().inflate(R.layout.popup_rename, null)
            renameSheetDialog.setContentView(renameSheetView)
            renameSheetDialog.setCanceledOnTouchOutside(true)
            val renameEditText = renameSheetView.findViewById<EditText>(R.id.rename_EditText)

            renameSheetView.findViewById<TextView>(R.id.rename_CancelText).setOnClickListener {
                renameSheetDialog.dismiss()
            }
            val rename_OKText =
                renameSheetView.findViewById<TextView>(R.id.rename_OKText)

            renameEditText.requestFocus()
            renameEditText.setText(tempFile.videoName)
            val nameWithoutExtension: String = tempFile.videoName.substring(
                0,
                tempFile.videoName.length - getFileExtension(tempFile.videoName).length
            )
            renameEditText.setSelection(0, nameWithoutExtension.length)
            renameSheetDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


            rename_OKText.setOnClickListener {
                val newName = renameEditText.text.toString().trim()

                if (currentFile.exists() && newName.isNotEmpty()) {
                    val newFile =
                        File(currentFile.parentFile, newName + "." + currentFile.extension)

                    if (!newFile.exists() || newFile == currentFile) {
                        if (currentFile.renameTo(newFile)) {
                            MediaScannerConnection.scanFile(
                                requireContext(),
                                arrayOf(newFile.toString()),
                                arrayOf("video/*"),
                                null
                            )
                            videoList[position].videoName = newName
                            videoList[position].path = newFile.path
                            videoList[position].image = Uri.fromFile(newFile)
                            adapterVideo.notifyItemChanged(position)
                        } else {
                            showToast(requireContext(), "Error in renaming File!!")
                        }
                    } else {
                        showToast(requireContext(), "File with the same name already exists!")
                    }
                }
                renameSheetDialog.dismiss()
            }


            changeVisibility(false)
            renameSheetDialog.show()
        }

        /*
        } else {
            fragmentVideosBinding.bottomRenameVideoFragment.isClickable = false
        }

            copy option
        if (!selectedFiles.isEmpty()) {
            bottomMenuCopyOption.isClickable = true
            bottomMenuCopyOption.setTextColor(Color.WHITE)
            bottomMenuCopyOption.setOnClickListener { showCopyDialog() }
        } else {
            bottomMenuCopyOption.isClickable = false
            bottomMenuCopyOption.setTextColor(Color.RED)
        }

        //delete option
        if (selectedFiles.isNotEmpty()) {
            fragmentVideosBinding.bottomDeleteVideoFragment.setOnClickListener {
                selectedFile = fileAdapter.getFile(position)
                val deleteSheetDialog = BottomSheetDialog(
                    requireContext(),
                    R.style.ThemeOverlay_App_BottomSheetDialog
                )
                val deleteSheetView: View =
                    getLayoutInflater().inflate(R.layout.popup_delete, null)
                deleteSheetDialog.setContentView(deleteSheetView)
                deleteSheetDialog.setCanceledOnTouchOutside(true)
                val deleteSelectedText =
                    deleteSheetView.findViewById<TextView>(R.id.deleteSelectedText)
                val deleteDeleteText =
                    deleteSheetView.findViewById<TextView>(R.id.deleteDeleteText)
                val deleteCancelText =
                    deleteSheetView.findViewById<TextView>(R.id.deleteCancelText)
                deleteSelectedText.text = "Delete " + selectedFiles.size + " selected items?"

                //delete ok
                deleteDeleteText.setOnClickListener {
                    val filesToRemove: MutableList<File> =
                        ArrayList()
                    for (selectedFile in selectedFiles) {
                        if (selectedFile.exists()) {
                            if (selectedFile.isDirectory) {
                                val isDeleted: Boolean = deleteFolder(selectedFile)
                                if (isDeleted) {
                                    Toast.makeText(
                                        context,
                                        "Folder deleted",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    filesToRemove.add(selectedFile)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to delete folder",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (selectedFile.isFile) {
                                val isDeleted = selectedFile.delete()
                                if (isDeleted) {
                                    Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT)
                                        .show()
                                    filesToRemove.add(selectedFile)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to delete file",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Folder/File doesn't exist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "No file or folder selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                        fileList.removeAll(filesToRemove) // Remove the deleted files/folders from fileList
                        fileAdapter.notifyDataSetChanged()
                        deleteSheetDialog.dismiss()
                        selectedFiles.clear() // Clear the selectedFiles list
                        fileAdapter.onFileSelectionChanged(
                            selectedFiles,
                            position
                        ) // Notify the selection change
                }

                //dismiss dialog
                deleteCancelText.setOnClickListener { deleteSheetDialog.dismiss() }
                deleteSheetDialog.show()
                changeVisibility(false)
            }
        } else {
            fragmentVideosBinding.bottomDeleteVideoFragment.isClickable = false
        }


        //move option
        if (selectedFiles.isNotEmpty()) {
            fragmentVideosBinding.bottomMoveVideoFragment.isClickable = true

            fragmentVideosBinding.bottomMoveVideoFragment.setOnClickListener {

            }
        } else {
            fragmentVideosBinding.bottomMoveVideoFragment.isClickable = false

        }


        //share option
        var isFolderSelected = false
        for (selectedFile in selectedFiles) {
            if (selectedFile.isDirectory) {
                isFolderSelected = true
                break
            }
        }
        if (!isFolderSelected && selectedFiles.isNotEmpty()) {
            fragmentVideosBinding.bottomSendVideoFragment.setOnClickListener(
                View.OnClickListener {
                    val fileUris = ArrayList<Uri>()
                    val fileNames = ArrayList<String>()
                    for (selectedFile in selectedFiles) {
                        if (!selectedFile.isDirectory) {
                            fileUris.add(
                                FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().packageName + ".provider",
                                    selectedFile
                                )
                            )
                            fileNames.add(selectedFile.name)
                        }
                    }
                    if (!fileUris.isEmpty()) {
                        val share = Intent(Intent.ACTION_SEND_MULTIPLE)
                      /* share.setType("*/ * ")*/
//            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
//            startActivity(
//                Intent.createChooser(
//                    share,
//                    "Share " + fileNames.size + " files"
//                )
//            )
//        }
//    })
//} else {
//    // Disable the share button and set the unselected icon
//    fragmentVideosBinding.bottomSendVideoFragment.isClickable = false


    }


    override fun onResume() {
        super.onResume()
        if (PlayerActivity.position != -1) {
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE
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