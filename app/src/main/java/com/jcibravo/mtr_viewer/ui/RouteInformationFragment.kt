package com.jcibravo.mtr_viewer.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jcibravo.mtr_viewer.DIMENSION
import com.jcibravo.mtr_viewer.R
import com.jcibravo.mtr_viewer.StationInfoFragment
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.convertDecimalToHex
import com.jcibravo.mtr_viewer.databinding.FragmentRouteDataBinding
import com.jcibravo.mtr_viewer.getOriginID
import com.jcibravo.mtr_viewer.getDestinationID
import com.jcibravo.mtr_viewer.getFirstLanguage
import com.jcibravo.mtr_viewer.GLOBAL
import com.jcibravo.mtr_viewer.createAcronym
import com.jcibravo.mtr_viewer.getAllLanguages
import com.jcibravo.mtr_viewer.normalizeStationIDList
import java.lang.IllegalArgumentException

class RouteInformationFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentRouteDataBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRouteDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onClick(route: RouteData) {
        TODO("Nothing to implement")
    }

    override fun onClick(stationID: String) {
        parentFragmentManager.setFragmentResult(
            "Station", bundleOf("Station" to stationID)
        )
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, StationInfoFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
            commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigation = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        parentFragmentManager.setFragmentResultListener("Route", this
        ) { _: String, result: Bundle ->
            println(result)
            val route = result.getParcelable<RouteData>("Route")

            if (route != null) {
                val iconText = createAcronym(getFirstLanguage(route.number))
                binding.routeIcon.text = iconText.ifEmpty { createAcronym(getFirstLanguage(route.name)) }
                binding.routeIcon.setBackgroundColor(try { Color.parseColor(convertDecimalToHex(route.color)) } catch (e: IllegalArgumentException) { Color.GRAY })
                binding.routeOrigin.text = GLOBAL!![DIMENSION].stations[getOriginID(route.stationIDs)]?.name?.substringBefore('|')
                binding.routeDestination.text = GLOBAL!![DIMENSION].stations[getDestinationID(route.stationIDs)]?.name?.substringBefore('|')
                val stationList = normalizeStationIDList(route.stationIDs) //getOneWayStationIDs(route.stationIDs)
                for (i in 0..stationList.lastIndex) {
                    val layout = when(i) {
                        0 -> R.layout.item_node_start
                        stationList.lastIndex -> R.layout.item_node_end
                        else -> R.layout.item_node
                    }

                    val inflatedView = layoutInflater.inflate(layout, null)
                    inflatedView.findViewById<ImageView>(R.id.node_icon).setColorFilter( try { Color.parseColor(convertDecimalToHex(route.color)) } catch (e: Exception) { Color.GRAY } )
                    inflatedView.findViewById<TextView>(R.id.node_station_name).text = getAllLanguages(GLOBAL!![DIMENSION].stations[stationList[i]]!!.name)
                    inflatedView.setOnClickListener { onClick(stationList[i]) }
                    binding.termo.addView(inflatedView)
                }
            }
        }

        navigation.post {
            navigation.menu.findItem(R.id.navigation_routes).isChecked = true
        }
    }
}