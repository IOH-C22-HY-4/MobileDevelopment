package com.ioh_c22_h2_4.hy_ponics.util

import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )
}