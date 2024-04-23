package com.jcibravo.mtr_viewer

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.jcibravo.mtr_viewer.classes.MTRAddress
import com.jcibravo.mtr_viewer.ui.OnListClickListener
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class AdressesListAdapter(context: Context, private val addresses: List<MTRAddress>, private val itemClickListener: OnListClickListener) : ArrayAdapter<MTRAddress>(context, R.layout.item_list_item, addresses) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_list_item, parent, false)
        }

        val address = addresses[position]
        itemView!!.findViewById<TextView>(R.id.name).text = address.name
        itemView.findViewById<TextView>(R.id.uri).text = "(${address.uri})"
        itemView.findViewById<ImageButton>(R.id.delBtn).setOnClickListener {
            savedAddresses.removeAt(position)
            val json = gson.toJson(savedAddresses)
            val filePath = File(context.applicationContext.filesDir, "hosts.json")
            FileOutputStream(filePath).use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }

            notifyDataSetChanged()
        }

        itemView.findViewById<LinearLayout>(R.id.listItemContainer).setOnClickListener {
            itemClickListener.onClick(address)
        }

        return itemView
    }
}
