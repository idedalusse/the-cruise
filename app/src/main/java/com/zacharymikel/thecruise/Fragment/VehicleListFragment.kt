package com.zacharymikel.thecruise.Fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.View.VehicleRecyclerViewAdapter

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VehicleListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [VehicleListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VehicleListFragment : Fragment() {

    private val TAG = "VehicleListFragment"
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: VehicleRecyclerViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var vehicles: ArrayList<Vehicle>? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            vehicles = arguments.getParcelableArrayList<Vehicle>(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_vehicle_list, container, false)

        viewManager = LinearLayoutManager(activity)
        viewAdapter = VehicleRecyclerViewAdapter(
                vehicles,
                this::onVehicleSelected
        )
        recyclerView = view.findViewById<RecyclerView>(R.id.vehicle_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        return view

    }

    private fun onVehicleSelected(v: Vehicle) {
        mListener!!.vehicleSelected(v)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun dataRefreshed(vehicles: ArrayList<Vehicle>) {
        viewAdapter.updateData(vehicles)
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun vehicleSelected(v: Vehicle)
    }

    companion object {

        private val ARG_PARAM1 = "vehicles"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment VehicleListFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: ArrayList<Vehicle>): VehicleListFragment {
            val fragment = VehicleListFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }
}
