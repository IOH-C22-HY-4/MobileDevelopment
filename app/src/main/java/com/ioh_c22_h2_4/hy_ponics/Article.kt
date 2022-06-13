package com.ioh_c22_h2_4.hy_ponics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    var title: String? = "",
    var content: String? = "",
    var img: String? = ""
):Parcelable