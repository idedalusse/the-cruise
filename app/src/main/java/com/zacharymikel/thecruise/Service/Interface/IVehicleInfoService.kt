package com.zacharymikel.thecruise.Service.Interface

interface IVehicleInfoService {
    fun getYearRange()
    fun getMakes(year: String)
    fun getModels(make: String)
    fun getEngines(model: String)
    fun getColors(): ArrayList<String>
}