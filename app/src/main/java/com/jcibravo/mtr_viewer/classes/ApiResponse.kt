package com.jcibravo.mtr_viewer.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiResponse(
    val data: List<DataResponse>
): Parcelable
