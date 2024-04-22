package com.jcibravo.mtr_viewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.databinding.FragmentStationBinding
import com.jcibravo.mtr_viewer.ui.OnClickListener

class StationFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentStationBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val stations = GLOBAL!![DIMENSION].stations
        val sortedStations = stations.entries.sortedBy { it.value.name }.associate { it.key to it.value }
        val spinner = binding.stationListSpinner

        val stationNames = listOf<String>(getString(R.string.fragment_timetable_select_spinner)) + sortedStations.values.map { it.name.replace("|", " | ") }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stationNames)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    val selectedStationID = sortedStations.keys.toList()[position - 1]
                    onClick(selectedStationID)
                    spinner.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
}