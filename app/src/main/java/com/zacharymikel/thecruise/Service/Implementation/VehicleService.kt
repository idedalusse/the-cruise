package com.zacharymikel.thecruise.Service.Implementation;

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.Status
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.Service.Interface.IVehicleService
import org.greenrobot.eventbus.EventBus

class VehicleService: IVehicleService {
    private val db = FirebaseDatabase.getInstance().reference.child("Vehicles")

    private val userService = UserService

    override fun removeVehicle(v: Vehicle) {
        v.deleted = true
        db.child(v.uuid).setValue(v)
    }

    override fun createVehicle(v: Vehicle): Vehicle {
        val key = db.push().key
        v.uuid = key
        v.userId = userService.getUserId()
        db.child(key).setValue(v)

        return v
    }

    override fun updateVehicle(v: Vehicle): Vehicle {
        db.child(v.uuid).setValue(v)
        return v
    }

    override fun refreshVehicles() {

        val listener = object : ValueEventListener {

            override fun onDataChange(dataSnapShot: DataSnapshot?) {

                var vehicleList = ArrayList<Vehicle>()

                // map the results to the maintenance list
                dataSnapShot!!.children.mapNotNullTo(vehicleList) {
                    val vehicle = it.getValue<Vehicle>(Vehicle::class.java)
                    if(vehicle == null || vehicle.deleted) {
                        null
                    } else {
                        vehicle
                    }
                }

                // notify subscribers that the data has changed
                EventBus.getDefault().post(Event(status = Status.SUCCESS,
                        type = EventType.VEHICLES_REFRESHED, data = vehicleList))

            }

            override fun onCancelled(p0: DatabaseError?) {}
        }

        val userId = userService.getUserId()
        db.orderByChild("userId").equalTo(userId).addValueEventListener(listener)

    }

}
