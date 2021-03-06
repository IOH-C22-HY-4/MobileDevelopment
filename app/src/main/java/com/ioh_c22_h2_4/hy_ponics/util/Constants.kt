package com.ioh_c22_h2_4.hy_ponics.util

import com.ioh_c22_h2_4.hy_ponics.BuildConfig
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FIREBASE_DATABASE_URL

object Constants {
    const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_EXTENSION = ".jpg"
    const val OBJECT_COUNT = 6
    const val MODEL_PATH = "model.tflite"
    const val LABELS_PATH = "model.txt"
    const val ACCURACY_THRESHOLD = 0.5f
    const val IMAGE_INPUT_SIZE = 150
    const val FIREBASE_DATABASE_URL = BuildConfig.FIREBASE_DATABASE_URL
    const val NOTIFICATION_CHANNEL = "Sensor Warning"
    const val NOTIFICATION_ID = "NOTIFICATION_ID"
}