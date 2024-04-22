package com.jcibravo.mtr_viewer.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jcibravo.mtr_viewer.DIMENSION
import com.jcibravo.mtr_viewer.R
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.convertDecimalToHex
import com.jcibravo.mtr_viewer.databinding.ItemRouteBinding
import com.jcibravo.mtr_viewer.getOriginID
import com.jcibravo.mtr_viewer.getDestinationID
import com.jcibravo.mtr_viewer.getFirstLanguage
import com.jcibravo.mtr_viewer.GLOBAL
import com.jcibravo.mtr_viewer.createAcronym
import com.jcibravo.mtr_viewer.getAllLanguages
import java.lang.IllegalArgumentException

class RouteAdapter(private val routes: List<RouteData>, private val listener: OnClickListener): RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = routes[position]
        with(holder){
            setListener(route)

            val iconText = createAcronym(getAllLanguages(route.number))
            binding.itemRouteIcon.text = iconText.ifEmpty { createAcronym(getFirstLanguage(route.name)) }
            binding.itemRouteIcon.setBackgroundColor( try { Color.parseColor(convertDecimalToHex(route.color)) } catch (e: IllegalArgumentException) {Color.GRAY} )
            binding.itemRouteOrigin.text = GLOBAL!![DIMENSION].stations[getOriginID(route.stationIDs)]?.name?.substringBefore('|') ?: ""
            binding.itemRouteDestination.text = GLOBAL!![DIMENSION].stations[getDestinationID(route.stationIDs)]?.name?.substringBefore('|') ?: ""
        }
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = ItemRouteBinding.bind(view)
        fun setListener(route: RouteData){
            binding.root.setOnClickListener {
                listener.onClick(route)
            }
        }
    }
}