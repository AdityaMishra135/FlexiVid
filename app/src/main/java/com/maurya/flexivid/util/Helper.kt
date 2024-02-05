package com.maurya.flexivid.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.IOException




fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


fun uriToFile(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName) // Change to an appropriate file name

    if (inputStream != null) {
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } else {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        if (documentFile != null && documentFile.isDirectory) {
            throw IOException("Selected item is a directory, not a file")
        } else {
            throw IOException("Could not open the file")
        }
    }

    return file
}


fun extractUuidFromLink(link: String): String {
    val parts = link.split("/")
    return parts.last()
}
