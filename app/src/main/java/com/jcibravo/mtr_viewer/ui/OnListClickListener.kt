package com.jcibravo.mtr_viewer.ui

import com.jcibravo.mtr_viewer.classes.MTRAddress

interface OnListClickListener {
    fun onClick(address: MTRAddress)
}