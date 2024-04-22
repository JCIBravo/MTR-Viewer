package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RouteData(
    val color: Int,
    val name: String,
    val number: String,
    val type: String,
    @SerializedName("stations") val stationIDs: List<String>,
    val durations: List<Int>,
    val densities: List<Int>,
    val circular: String
): Parcelable