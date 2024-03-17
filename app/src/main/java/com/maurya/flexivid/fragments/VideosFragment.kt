package com.maurya.flexivid.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
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
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maurya.flexivid.MainActivity
import com.maurya.flexivid.MainActivity.Companion.searchList
import com.maurya.flexivid.R
import com.maurya.flexivid.activity.PlayerActivity
import com.maurya.flexivid.dataEntities.VideoDataClass
import com.maurya.flexivid.database.AdapterVideo
import com.maurya.flexivid.databinding.FragmentVideosBinding
import com.maurya.flexivid.databinding.PopupDetailsBinding
import com.maurya.flexivid.util.OnItemClickListener
import com.maurya.flexivid.util.SharedPreferenceHelper
import com.maurya.flexivid.util.getFormattedDate
import com.maurya.flexivid.util.getFormattedFileSize
import com.maurya.flexivid.util.showToast
import com.maurya.flexivid.util.sortMusicList
import com.maurya.flexivid.viewModelsObserver.ModelResult
import com.maurya.flexivid.viewModelsObserver.ViewModelObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class VideosFragment : Fragment(), OnItemClickListener {


    private lateinit var fragmentVideosBinding: FragmentVideosBinding
    private lateinit var adapterVideo: AdapterVideo

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    private var sortingOrder: String = ""
    private var searchViewVisible: Boolean = false

    private val viewModel by viewModels<ViewModelObserver>()

    private val sortOptions = arrayOf(
        "DISPLAY_NAME ASC",
        "DISPLAY_NAME DESC",
        "SIZE DESC",
        "SIZE ASC",
        "DATE_ADDED DESC",
        "DATE_ADDED ASC"
    )

    companion object {
        var videoList: ArrayList<VideoDataClass> = arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentVideosBinding = FragmentVideosBinding.inflate(inflater, container, false)
        val view = fragmentVideosBinding.root
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        changeVisibility(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesHelper = SharedPreferenceHelper(requireContext())
        sortingOrder = sharedPreferencesHelper.getSortingOrder().toString()

        lifecycle.addObserver(viewModel)

        fragmentVideosBinding.recyclerViewVideosFragment.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapterVideo = AdapterVideo(requireContext(), this@VideosFragment, videoList)
            adapter = adapterVideo
        }
        fetchVideosUsingViewModel()

        listener()
    }

    private fun fetchVideosUsingViewModel() {
        viewModel.fetchVideos(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videosStateFLow.collect {
                    when (it) {
                        is ModelResult.Success -> {
                            fragmentVideosBinding.progressBar.visibility = View.GONE
                            videoList.clear()
                            videoList.addAll(it.data!!)
                            sortMusicList(sortingOrder, videoList, adapterVideo)
                        }

                        is ModelResult.Error -> {
                            fragmentVideosBinding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is ModelResult.Loading -> {
                            fragmentVideosBinding.progressBar.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun listener() {

        searchVisibility(false)

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


        fragmentVideosBinding.searchViewImage.setOnClickListener {
            searchVisibility(true)
            searchViewVisible = true
            fragmentVideosBinding.searchViewVideoFragment.requestFocus()
            fragmentVideosBinding.searchViewVideoFragment.onActionViewExpanded()
        }


        fragmentVideosBinding.searchViewVideoFragment.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                searchList = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in videoList) {
                        if (song.videoName.lowercase().contains(userInput))
                            searchList.add(song)
                    }
                    MainActivity.search = true
                    adapterVideo.updateSearchList(searchList)
                }
                return true
            }
        })


        fragmentVideosBinding.searchViewClose.setOnClickListener {
            fragmentVideosBinding.searchViewVideoFragment.setQuery("", false)
            searchList.clear()
            MainActivity.search = false
            searchVisibility(false)
        }

    }

    private fun searchVisibility(isVisible: Boolean) {
        if (isVisible) {
            fragmentVideosBinding.searchViewImage.visibility = View.GONE
            fragmentVideosBinding.videoTitle.visibility = View.GONE
            fragmentVideosBinding.sortingVideoFragment.visibility = View.GONE
            fragmentVideosBinding.searchViewVideoFragment.visibility = View.VISIBLE
            fragmentVideosBinding.searchViewClose.visibility = View.VISIBLE
        } else {
            fragmentVideosBinding.searchViewImage.visibility = View.VISIBLE
            fragmentVideosBinding.videoTitle.visibility = View.VISIBLE
            fragmentVideosBinding.sortingVideoFragment.visibility = View.VISIBLE
            fragmentVideosBinding.searchViewVideoFragment.visibility = View.GONE
            fragmentVideosBinding.searchViewClose.visibility = View.GONE

        }
    }

    fun handleBackPressed() {
        if (searchViewVisible) {
            searchVisibility(false)
            searchViewVisible = false
        } else {
            activity?.onBackPressed()
        }
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
            R.id.newestFirstLayoutPopUpMenu,
            R.id.oldestFirstLayoutPopUpMenu
        )

        layoutIds.forEachIndexed { index, layoutId ->
            val layout = popupView.findViewById<LinearLayout>(layoutId)
            layout.setOnClickListener {
                sortMusicList(sortOptions[index], videoList, adapterVideo)
                sharedPreferencesHelper.saveSortingOrder(sortOptions[index])
                popupWindow.dismiss()
            }
        }


    }


    override fun onItemClickListener(position: Int) {

    }

    private fun changeVisibility(visible: Boolean) {
        if (!visible) {
            (activity as MainActivity).visibilityBottomNav(false)
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.GONE
            fragmentVideosBinding.videoNameLayout.visibility = View.VISIBLE
        } else {
            (activity as MainActivity).visibilityBottomNav(true)
            fragmentVideosBinding.topToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.bottomToolBarLayoutLongCLick.visibility = View.VISIBLE
            fragmentVideosBinding.videoNameLayout.visibility = View.GONE
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

        //for deleting file
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

            deleteSelectedText.text = "Delete ${selectedFiles.size} selected items"

            deleteCancelText.setOnClickListener {
                deleteSheetDialog.dismiss()
            }

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


            deleteSheetDialog.show()


        }

        //details of file
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
            val popupDetailsLastModifiedText = bindingPopUp.popupDetailsLastModifiedText
            val popupDetailsOKText = bindingPopUp.popupDetailsOKText
            val popupLocationLayout = bindingPopUp.popUpLocationLayout
            val popupLastModifiedLayout = bindingPopUp.popUpLastModifiedLayout

            popupDetailsPathText.isSelected = true

            if (selectedFiles.size == 1) {
                val selectedFile = selectedFiles[0]
                popupDetailsNameText.text = selectedFile.videoName
                popupDetailsPathText.text = selectedFile.path

                Log.d("fragmentDateItemClass", getFormattedDate(selectedFile.dateModified))
                popupDetailsLastModifiedText.text = getFormattedDate(selectedFile.dateModified)
            } else {
                popupDetailsNameText.visibility = View.GONE
                popupLocationLayout.visibility = View.GONE
                popupLastModifiedLayout.visibility = View.GONE
            }

            var totalSizeInBytes: Long = 0
            selectedFiles.forEach { selectedFile ->
                totalSizeInBytes += selectedFile.size.toLong()
            }

            popupDetailsSizeText.text = getFormattedFileSize(totalSizeInBytes)

            popupDetailsOKText.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        //for renaming file
        if (selectedFiles.size == 1) {

            fragmentVideosBinding.bottomRenameVideoFragment.setOnClickListener {
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

                val videoNameWithoutExtension = videoList[position].videoName.substringBeforeLast('.')
                renameEditText.setText(videoNameWithoutExtension)
                renameEditText.requestFocus()
                renameEditText.selectAll()
                renameSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                renameCancelText.setOnClickListener {
                    renameSheetDialog.dismiss()
                }

                renameOKText.setOnClickListener {
                    val newName = renameEditText.text.toString().trim()
                    val currentFile = File(videoList[position].path)

                    if (currentFile.exists() && newName.isNotEmpty()) {
                        val newFile =
                            File(currentFile.parent, "$newName.${currentFile.extension}")

                        if (currentFile.renameTo(newFile)) {
                            videoList[position].videoName = newFile.name
                            videoList[position].path = newFile.path
                            adapterVideo.notifyDataSetChanged()
                            fetchVideosUsingViewModel()
                        } else {
                            showToast(requireContext(), "Failed to rename file")
                        }
                    } else {
                        showToast(requireContext(), "Please provide a valid name!")
                    }

                    renameSheetDialog.dismiss()
                }
                renameSheetDialog.show()
            }

        } else {
            fragmentVideosBinding.bottomRenameVideoFragment.isClickable = false
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

        //for sending files
        fragmentVideosBinding.bottomSendVideoFragment.setOnClickListener {

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
            shareIntent.type = "video/*"

            val fileUris = ArrayList<Uri>()
            val fileNames = ArrayList<String>()
            for (videoData in selectedFiles) {
                val file = File(videoData.path)
                val uri = Uri.parse(file.path)
                fileUris.add(uri)
                fileNames.add(file.name)
            }

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
            val chooserTitle = "Share ${fileNames.size} Video Files"
            ContextCompat.startActivity(
                requireContext(),
                Intent.createChooser(shareIntent, chooserTitle),
                null
            )

        }

    }


    override fun onResume() {
        super.onResume()
        if (PlayerActivity.position != -1) {
            fragmentVideosBinding.nowPlayingVideoFragment.visibility = View.VISIBLE
        }
        checkProgress()
    }

    private fun checkProgress() {
        if (fragmentVideosBinding.recyclerViewVideosFragment.isEmpty()) {
            fragmentVideosBinding.progressBar.visibility = View.VISIBLE
        } else {
            fragmentVideosBinding.progressBar.visibility = View.GONE
        }
    }


}