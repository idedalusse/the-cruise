package com.zacharymikel.thecruise.Service.Interface

import com.zacharymikel.thecruise.Model.Vehicle

/**
 * Created by zacharymikel on 4/21/18.
 */
interface IVehicleService {
    fun createVehicle(v: Vehicle): Vehicle
    fun removeVehicle(v: Vehicle)
    fun updateVehicle(v: Vehicle): Vehicle
    fun refreshVehicles()
}