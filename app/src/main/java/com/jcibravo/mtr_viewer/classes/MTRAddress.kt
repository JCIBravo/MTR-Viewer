package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MTRAddress(
    val name: String,
    val uri: String,
): Parcelable
