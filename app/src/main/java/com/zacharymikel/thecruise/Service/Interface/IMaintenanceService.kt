package com.zacharymikel.thecruise.Service.Interface

import com.zacharymikel.thecruise.Model.MaintenanceItem

/**
 * Created by zacharymikel on 4/20/18.
 */
interface IMaintenanceService {

    fun createMaintenanceItem(m: MaintenanceItem): MaintenanceItem
    fun updateMaintenanceItem(m: MaintenanceItem): MaintenanceItem
    fun removeMaintenanceItem(m: MaintenanceItem)
    fun completeMaintenanceItem(m: MaintenanceItem)
    fun getItemsForVehicle(uuid: String?)

}