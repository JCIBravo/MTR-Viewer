package com.jcibravo.mtr_viewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.databinding.FragmentRouteListBinding
import com.jcibravo.mtr_viewer.ui.OnClickListener
import com.jcibravo.mtr_viewer.ui.RouteAdapter
import com.jcibravo.mtr_viewer.ui.RouteInformationFragment
import kotlinx.coroutines.launch

class RouteListFragment : Fragment(), OnClickListener {
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var linearLayoutManager: RecyclerView.LayoutManager
    private lateinit var binding: FragmentRouteListBinding

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

        lifecycleScope.launchWhenCreated {
            generateList()
        }

        binding.incrementBtn.setOnClickListener {
            incrementDimension(requireContext())
            lifecycleScope.launch { generateList() }
        }

        binding.decrementBtn.setOnClickListener {
            decrementDimension(requireContext())
            lifecycleScope.launch { generateList() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRouteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun generateList() {
        binding.progressBar.visibility = View.VISIBLE
        routeAdapter = RouteAdapter(GLOBAL!![DIMENSION].routes.sortedBy { it.name }.filter { it.stationIDs.size >= 2 }, this@RouteListFragment)
        binding.progressBar.visibility = View.GONE
        linearLayoutManager = LinearLayoutManager(context)

        binding.recyclerViewRoutes.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = routeAdapter
        }

        binding.dimensionNumber.text = getString(R.string.fragment_route_list_dimension, DIMENSION)
    }
}