package com.ioh_c22_h2_4.hy_ponics.util

import android.content.Context
import android.os.Environment
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    fun Context.createTempFile(): File {

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }
}