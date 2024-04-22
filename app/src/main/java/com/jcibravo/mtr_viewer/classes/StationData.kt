package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StationData(
    val name: String,
    val color: Int,
    val zone: Int,
    val x: Int,
    val z: Int,
    val connections: List<String>
): Parcelable
