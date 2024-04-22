package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataResponse(
    val routes: List<RouteData>,
    val positions: Map<String, PositionData>,
    val stations: Map<String, StationData>,
    val types: List<String>
): Parcelable