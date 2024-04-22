package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PositionData(
    val x: Int,
    val y: Int,
    val vertical: Boolean
): Parcelable