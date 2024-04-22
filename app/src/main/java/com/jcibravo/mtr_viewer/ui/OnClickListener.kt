package com.jcibravo.mtr_viewer.ui

import com.jcibravo.mtr_viewer.classes.RouteData

interface OnClickListener {
    fun onClick(route: RouteData)
    fun onClick(stationID: String)
}