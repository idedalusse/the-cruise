package com.zacharymikel.thecruise.View

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.R
import java.io.File

/**
 * This is a view adapter, which is responsible for converting
 * the data for the user's pictures into list items.
 */
class VehicleRecyclerViewAdapter(

        private var myDataset: ArrayList<Vehicle>?,
        private val onItemPressed: (v: Vehicle) -> Unit

) : RecyclerView.Adapter<VehicleRecyclerViewAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(
            val rowView: View,
            val yearMakeModelView: TextView,
            val pictureThumbnail: ImageView

    ) : RecyclerView.ViewHolder(rowView) {}

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleRecyclerViewAdapter.ViewHolder {

        // create a new view
        val rowView: View = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_vehicle,
                parent,
                false) as View

        val yearMakeModelView: TextView = rowView.findViewById(R.id.year_make_model)
        val pictureThumbnail: ImageView = rowView.findViewById(R.id.vehicle_image)

        return ViewHolder(rowView, yearMakeModelView, pictureThumbnail)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val context = holder.rowView.context

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val vehicle = myDataset!![position]

        if(vehicle.imageFilePath != null && vehicle.imageFilePath != "null") {

            Picasso.get()
                    .load(Uri.fromFile(File(vehicle.imageFilePath)))
                    .fit()
                    // .resize(holder.pictureThumbnail.measuredWidth, holder.pictureThumbnail.measuredHeight)
                    .centerCrop()
                    .into(holder.pictureThumbnail)

        }

        val yearMakeModelStr = context.getString(R.string.year_make_model, vehicle.year, vehicle.make, vehicle.model)
        holder.yearMakeModelView.text = yearMakeModelStr

        holder.rowView.setOnClickListener {
            onItemPressed(vehicle)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset!!.size

    fun updateData(v: ArrayList<Vehicle>) {
        this.myDataset!!.clear()
        this.myDataset!!.addAll(v)
        notifyDataSetChanged()
    }

}