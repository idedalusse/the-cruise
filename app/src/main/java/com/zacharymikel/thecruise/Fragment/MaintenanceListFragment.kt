package com.zacharymikel.thecruise.Fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import com.zacharymikel.thecruise.Model.MaintenanceItem
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.View.MaintenanceRecyclerViewAdapter

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MaintenanceListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MaintenanceListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaintenanceListFragment : Fragment() {

    private val TAG = "MaintenanceListFragment"

    private var maintenanceItems: ArrayList<MaintenanceItem>? = null
    private var mListener: OnFragmentInteractionListener? = null

    private lateinit var addMaintenanceButton: ImageView
    private lateinit var refreshButton: ImageView
    private lateinit var loading: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            maintenanceItems = arguments.getParcelableArrayList<MaintenanceItem>(MaintenanceListFragment.ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_maintenance_list, container, false)

        loading = view.findViewById(R.id.loading)

        addMaintenanceButton = view.findViewById(R.id.button_add_maintenance_item)
        addMaintenanceButton.setOnClickListener {
            mListener!!.addMaintenanceItem()
        }

        refreshButton = view.findViewById(R.id.button_refresh)
        refreshButton.setOnClickListener {
            val rotation: Animation = AnimationUtils.loadAnimation(view.context, R.anim.rotate_360)
            rotation.repeatCount = Animation.INFINITE
            refreshButton.startAnimation(rotation)
            mListener!!.refreshMaintenanceItems()
        }

        viewManager = LinearLayoutManager(activity)
        viewAdapter = MaintenanceRecyclerViewAdapter(
                maintenanceItems,
                this::onMaintenanceItemSelected,
                this::onMaintenanceItemLongPressed
        )
        recyclerView = view.findViewById<RecyclerView>(R.id.maintenance_recycler_view).apply {

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        if(maintenanceItems!!.size == 0) {
            generateDefault()
        }

        showLoading()

        return view
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

    private fun generateDefault() {

        val maintenanceItem = MaintenanceItem()
        maintenanceItem.title = getString(R.string.default_maintenance_title)
        maintenanceItem.default = true
        maintenanceItems!!.add(maintenanceItem)
        viewAdapter.notifyDataSetChanged()
        viewAdapter.notifyItemInserted(0)

    }

    private fun showLoading() {
        loading.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
    }

    private fun showItems() {
        loading.visibility = View.INVISIBLE
        recyclerView.visibility = View.VISIBLE
    }

    private fun onMaintenanceItemSelected(m: MaintenanceItem) {
        mListener!!.editMaintenanceItem(m)
    }

    private fun onMaintenanceItemLongPressed(m: MaintenanceItem) {

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.remove_maintenance_title))
        builder.setMessage(getString(R.string.remove_maintenance_description))
        builder.setPositiveButton(getString(R.string.remove_maintenance_confirm)) { dialog, which ->
            mListener!!.removeMaintenanceItem(m)
        }
        builder.setNegativeButton(getString(R.string.remove_maintenance_deny)) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()

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
        fun addMaintenanceItem()
        fun editMaintenanceItem(m: MaintenanceItem)
        fun completeMaintenanceItem()
        fun removeMaintenanceItem(m: MaintenanceItem)
        fun refreshMaintenanceItems()
    }

    fun addMaintenanceItem(m: MaintenanceItem) {
        maintenanceItems!!.add(m)
        maintenanceItems!!.filter { it.default }.forEach { maintenanceItems!!.remove(it) }
        viewAdapter.notifyDataSetChanged()
    }

    fun updateMaintenanceItem(updateMaintenanceItem: MaintenanceItem) {
        val index = maintenanceItems!!.indexOfFirst { x -> x.uuid == updateMaintenanceItem.uuid }
        maintenanceItems!![index] = updateMaintenanceItem
        viewAdapter.notifyItemChanged(index)
    }

    fun removeMaintenanceItem(m: MaintenanceItem) {
        val index = findIndex(m)
        maintenanceItems!!.removeAt(index)
        viewAdapter.notifyItemRemoved(index)

        if(maintenanceItems!!.size == 0) generateDefault()
    }

    private fun findIndex(m: MaintenanceItem): Int {
        return maintenanceItems!!.indexOfFirst { x -> x.uuid == m.uuid }
    }

    /**
     * This is called soon after the parent activity instantiates this fragment.
     * Firebase is called for a list of maintenance items for the vehicle, and
     * the data is then loaded into this fragment's recycler view.
     */
    fun dataRefreshed(data: ArrayList<MaintenanceItem>) {

        this.maintenanceItems = data
        if(this.maintenanceItems!!.size == 0) generateDefault()

        viewAdapter = MaintenanceRecyclerViewAdapter(
                this.maintenanceItems,
                this::onMaintenanceItemSelected,
                this::onMaintenanceItemLongPressed
        )

        recyclerView.adapter = viewAdapter
        recyclerView.invalidate()

        refreshButton.clearAnimation()
        showItems()
    }

    companion object {

        val ARG_PARAM1 = "maintenanceItems"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MaintenanceListFragment.
         */
        fun newInstance(maintenanceItems: ArrayList<MaintenanceItem>): MaintenanceListFragment {
            val fragment = MaintenanceListFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_PARAM1, maintenanceItems)
            fragment.arguments = args
            return fragment
        }
    }

}
