package com.ioh_c22_h2_4.hy_ponics.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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

    fun Uri.uriToFile(context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = context.createTempFile()

        val inputStream = contentResolver.openInputStream(this) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    fun Context.createTempFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp =
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }
}