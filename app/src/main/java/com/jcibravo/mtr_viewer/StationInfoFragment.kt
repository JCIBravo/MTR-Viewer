package com.jcibravo.mtr_viewer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jcibravo.mtr_viewer.api.RetrofitSingleton
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.classes.StationTimetableData
import com.jcibravo.mtr_viewer.databinding.FragmentStationInfoBinding
import com.jcibravo.mtr_viewer.ui.OnClickListener
import com.jcibravo.mtr_viewer.ui.RouteInformationFragment
import com.tomer.fadingtextview.FadingTextView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class StationInfoFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentStationInfoBinding
    private var response: List<StationTimetableData> = emptyList()
    private var apiCoroutine: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStationInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        apiCoroutine?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCoroutine?.cancel()
    }

    override fun onResume() {
        super.onResume()
        apiCoroutine
    }

    override fun onClick(route: RouteData) {
        parentFragmentManager.setFragmentResult(
            "Route", bundleOf("Route" to route)
        )
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, RouteInformationFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
            commit()
        }
    }

    override fun onClick(stationID: String) {
        TODO("Nothing to implement")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigation = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        lateinit var inflatedView: View

        parentFragmentManager.setFragmentResultListener("Station", this
        ) { _: String, result: Bundle ->
            val stationID = result.getString("Station")
            val departureListLayout = binding.departureList
            apiCoroutine = lifecycleScope.launchWhenCreated {
                while (isActive) {
                    departureListLayout.removeAllViews()
                    binding.loadingProgress.visibility = View.VISIBLE
                    navigation.menu.forEach { it.isEnabled = false }

                    try {
                        response = RetrofitSingleton.api.getTimetables(0, stationID)
                    } catch (e: Exception) {
                        binding.loadingProgress.visibility = View.GONE
                        Log.e("MTR API", e.toString())
                        navigation.menu.forEach { it.isEnabled = true }

                        inflatedView = layoutInflater.inflate(R.layout.item_no_internet, null)
                        departureListLayout.gravity = Gravity.CENTER
                        departureListLayout.addView(inflatedView)

                        binding.lastUpdated.text = getString(R.string.fragment_station_last_updated, convertMillisToTime(System.currentTimeMillis(), true, true))
                        return@launchWhenCreated
                    }

                    Log.d("MTR API", "Result for stationID=$stationID: $response")
                    binding.loadingProgress.visibility = View.GONE
                    binding.stationName.text = getStationDataFromID(DIMENSION, stationID!!).name.replace("|", "\n")
                    binding.stationName.setOnClickListener { Toast.makeText(requireContext(), stationID, Toast.LENGTH_LONG).show() }

                    if (response.isNotEmpty()) {
                        departureListLayout.gravity = Gravity.NO_GRAVITY
                        for (i in 0..response.lastIndex) {
                            //Set inflated view
                            inflatedView = layoutInflater.inflate(R.layout.item_departure, null)
                            inflatedView.setOnClickListener { onClick(getRouteDataFromName(DIMENSION, response[i].name)) }

                            //Name
                            val service = getFirstLanguage(response[i].name)
                            inflatedView.findViewById<TextView>(R.id.item_line_name).text = service
                            inflatedView.findViewById<TextView>(R.id.item_line_name).setBackgroundColor(try { Color.parseColor(convertDecimalToHex(response[i].color)) } catch (_: Exception) { Color.GRAY })

                            //Icon
                            when {
                                service.contains("バス", true) ||
                                service.contains("公共汽车", true) ||
                                service.contains("버스", true) ||
                                service.contains("автобус", true) ||
                                service.contains("омнибус", true) ||
                                service.contains("公車", true) ||
                                service.contains("bus", true) ||
                                service.contains("omnibus", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_bus)

                                service.contains("mtr", true) ||
                                service.contains("metro", true) ||
                                service.contains("метро", true) ||
                                service.contains("метрополитен", true) ||
                                service.contains("подземка", true) ||
                                service.contains("subway", true) ||
                                service.contains("u-bahn", true) ||
                                service.contains("ubahn", true) ||
                                service.contains("地下鉄", true) ||
                                service.contains("地铁", true) ||
                                service.contains("捷運", true) ||
                                service.contains("지하철", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_metro)

                                service.contains("ferry", true) ||
                                service.contains("boat", true) ||
                                service.contains("паром", true) ||
                                service.contains("フェリー", true) ||
                                service.contains("渡し船", true) ||
                                service.contains("나룻배", true) ||
                                service.contains("渡船", true) ||
                                service.contains("渡輪", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_ferry)

                                service.contains("lrt", true) ||
                                service.contains("light", true) ||
                                service.contains("light rail", true) ||
                                service.contains("lightrail", true) ||
                                service.contains("tram", true) ||
                                service.contains("tramway", true) ||
                                service.contains("トラム", true) ||
                                service.contains("トロリーカー", true) ||
                                service.contains("有轨电车", true) ||
                                service.contains("電車", true) ||
                                service.contains("трамвай", true) ||
                                service.contains("노면전차", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_light_rail)

                                service.contains("hst", true) ||
                                service.contains("speed", true) ||
                                service.contains("shinkansen", true) ||
                                service.contains("高速列车", true) ||
                                service.contains("высокоскоростной", true) ||
                                service.contains("고속", true) ||
                                service.contains("新幹線", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_hst_train)

                                service.contains("索道", true) ||
                                service.contains("케이블카", true) ||
                                service.contains("往复式地面缆车", true) ||
                                service.contains("Фуникулёр", true) ||
                                service.contains("ケーブルカー", true) ||
                                service.contains("funicular", true) ||
                                service.contains("cable rail", true) ||
                                service.contains("cable railway", true) ||
                                service.contains("cable", true) ||
                                service.contains("cable car", true)
                                    -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_cable_car)

                                else -> inflatedView.findViewById<ImageView>(R.id.item_line_icon).setImageResource(R.drawable.type_train)
                            }

                            //Destination
                            val destinationNames = getAllLanguagesAsList(response[i].destination)
                            if (destinationNames.isNotEmpty()) {
                                inflatedView.findViewById<FadingTextView>(R.id.item_line_destination)
                                inflatedView.findViewById<FadingTextView>(R.id.item_line_destination).setTexts(destinationNames.toTypedArray())
                                inflatedView.findViewById<FadingTextView>(R.id.item_line_destination).setTimeout(3.seconds)
                            } else {
                                inflatedView.findViewById<FadingTextView>(R.id.item_line_destination).text = requireContext().getString(R.string.no_destination)
                            }

                            //Route
                            val routeNames = getAllLanguagesAsList(response[i].route)
                            if (routeNames.isNotEmpty()) {
                                inflatedView.findViewById<TextView>(R.id.item_line_route)
                            } else {
                                inflatedView.findViewById<TextView>(R.id.item_line_route).visibility = View.GONE
                            }

                            //Platform
                            inflatedView.findViewById<TextView>(R.id.item_line_platform).text = getString(R.string.fragment_station_platform, response[i].platform)

                            //ETA
                            inflatedView.findViewById<TextView>(R.id.item_line_eta).text = convertMillisAndCheckTime(requireContext(), response[i].arrival)
                            departureListLayout.addView(inflatedView)
                        }
                    } else {
                        inflatedView = layoutInflater.inflate(R.layout.item_no_departures, null)
                        departureListLayout.gravity = Gravity.CENTER
                        departureListLayout.addView(inflatedView)
                    }

                    binding.lastUpdated.text = getString(R.string.fragment_station_last_updated, convertMillisToTime(System.currentTimeMillis(), true, true))
                    navigation.menu.forEach { it.isEnabled = true }
                    delay(30000)
                }
            }
        }

        navigation.post {
            navigation.menu.forEach { it.isChecked = false }
            navigation.menu.findItem(R.id.navigation_timetable).isChecked = true
        }
    }
}