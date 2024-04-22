package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StationTimetableData (
    var arrival: Long,
    var name: String,
    var destination: String,
    var circular: String,
    var route: String,
    var platform: String,
    var color: Int
) : Parcelable