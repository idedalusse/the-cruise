package com.zacharymikel.thecruise.View

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zacharymikel.thecruise.Model.MaintenanceItem
import com.zacharymikel.thecruise.R

/**
 * This is a view adapter, which is responsible for converting
 * the data for the user's pictures into list items.
 */
class MaintenanceRecyclerViewAdapter(

        private val myDataset: ArrayList<MaintenanceItem>?,
        private val onItemPressed: (m: MaintenanceItem) -> Unit,
        private val onItemLongPressed: (m: MaintenanceItem) -> Unit

) : RecyclerView.Adapter<MaintenanceRecyclerViewAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(
            val rowView: View,
            val title: TextView,
            val dueDate: TextView

    ) : RecyclerView.ViewHolder(rowView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceRecyclerViewAdapter.ViewHolder {

        // create a new view
        val rowView: View = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_maintenance,
                parent,
                false) as View

        val titleView: TextView = rowView.findViewById(R.id.title)
        val dueDateView: TextView = rowView.findViewById(R.id.due_date)

        return ViewHolder(rowView, titleView, dueDateView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val maintenanceItem = myDataset!![position]

        holder.title.text = maintenanceItem.title
        holder.dueDate.text = maintenanceItem.dueDateStr()

        holder.rowView.setOnClickListener {
            onItemPressed(maintenanceItem)
        }

        holder.rowView.setOnLongClickListener {
            onItemLongPressed(maintenanceItem)
            true
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset!!.size

}