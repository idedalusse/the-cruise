package com.zacharymikel.thecruise.Service.Implementation

import android.support.v4.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.Status
import com.zacharymikel.thecruise.Model.MaintenanceItem
import com.zacharymikel.thecruise.Service.Interface.IMaintenanceService
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by zacharymikel on 4/20/18.
 */

class MaintenanceService: IMaintenanceService {

    val db = FirebaseDatabase.getInstance().reference.child("MaintenanceItems")

    override fun createMaintenanceItem(m: MaintenanceItem): MaintenanceItem {
        val key = db.push().key
        m.uuid = key
        db.child(key).setValue(m)

        return m
    }

    override fun updateMaintenanceItem(m: MaintenanceItem): MaintenanceItem {
        db.child(m.uuid).setValue(m)
        return m
    }

    override fun removeMaintenanceItem(m: MaintenanceItem) {
        db.child(m.uuid).removeValue()
    }

    override fun completeMaintenanceItem(m: MaintenanceItem) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemsForVehicle(vehicleId: String?) {

        val listener = object : ValueEventListener {

            private var maintenanceList = ArrayList<MaintenanceItem>()

            override fun onDataChange(dataSnapShot: DataSnapshot?) {

                // map the results to the maintenance list
                dataSnapShot!!.children.mapNotNullTo(maintenanceList) {
                    it.getValue<MaintenanceItem>(MaintenanceItem::class.java)
                }

                // notify subscribers that the data has changed
                EventBus.getDefault().post(Event(status = Status.SUCCESS,
                        type = EventType.MAINTENANCE_REFRESHED, data = maintenanceList))

            }

            override fun onCancelled(p0: DatabaseError?) {}
        }

        db.orderByChild("vehicleId").equalTo(vehicleId).addListenerForSingleValueEvent(listener)
    }


}