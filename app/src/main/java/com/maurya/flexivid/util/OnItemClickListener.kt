package com.maurya.flexivid.util

import java.io.File


interface OnItemClickListener {
    fun onItemClickListener(position: Int)
    fun onItemLongClickListener(currentFile: File, position: Int)

}

