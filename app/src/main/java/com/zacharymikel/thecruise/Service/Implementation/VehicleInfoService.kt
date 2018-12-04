package com.zacharymikel.thecruise.Service.Implementation

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zacharymikel.thecruise.Constants.Status
import com.zacharymikel.thecruise.Constants.VehicleInfoEventType
import com.zacharymikel.thecruise.Event.VehicleInfoEvent
import com.zacharymikel.thecruise.Model.AutozoneItem
import com.zacharymikel.thecruise.Service.Interface.IVehicleInfoService
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException


class VehicleInfoService: IVehicleInfoService {

    private val url = "https://www.autozone.com/rest/bean/autozone/diy/commerce/vehicle/VehicleServices/getVehicleList?atg-rest-depth=2"
    private var years: ArrayList<AutozoneItem>? = null
    private var makes: ArrayList<AutozoneItem>? = null
    private var models: ArrayList<AutozoneItem>? = null
    private var engines: ArrayList<AutozoneItem>? = null

    override fun getYearRange() {

        val params = HashMap<String, String>()
        params.put("id", "0")
        params.put("itemType", "year")
        params.put("arg1", "0")
        params.put("arg2", "make")

        val json = JSONObject(params)
        post(json.toString(), VehicleInfoEventType.YEAR)

    }

    override fun getMakes(year: String) {

        if(years == null) return
        val id = years!!.find { x -> x.label == year }!!.id

        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("itemType", "make")
        params.put("arg1", id)
        params.put("arg2", "make")

        val json = JSONObject(params)
        post(json.toString(), VehicleInfoEventType.MAKE)

    }

    override fun getModels(make: String) {

        if(makes == null) return
        val id = makes!!.find { x -> x.label == make }!!.id

        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("itemType", "model")
        params.put("arg1", id)
        params.put("arg2", "model")

        val json = JSONObject(params)


        post(json.toString(), VehicleInfoEventType.MODEL)

    }

    override fun getEngines(model: String) {

        if(models == null) return
        val id = models!!.find { x -> x.label == model }!!.id

        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("itemType", "model")
        params.put("arg1", id)
        params.put("arg2", "model")

        val json = JSONObject(params)
        post(json.toString(), VehicleInfoEventType.ENGINE)

    }

    override fun getColors(): ArrayList<String> {
        val colors = ArrayList<String>()
        colors.add("Blue")
        return colors
    }

    private fun post(json: String, type: VehicleInfoEventType) {

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val client = OkHttpClient()

        val body = RequestBody.create(JSON, json)
        val req = Request.Builder()
                .url(url)
                .post(body)
                .build()

        client.newCall(req).enqueue(object: Callback {

            override fun onFailure(call: Call?, e: IOException?) {}

            override fun onResponse(call: Call?, response: Response?) {
                if(response!!.isSuccessful) {
                    successCallback(response.body()!!.string(), type)
                }
            }

        })

    }

    private fun successCallback(response: String, type: VehicleInfoEventType) {
        // parse response
        val jsonObject = JSONObject(response)
        val itemList = jsonObject.getJSONArray("items").toString()

        val listType = object : TypeToken<ArrayList<AutozoneItem>>() {}.type
        val list: ArrayList<AutozoneItem> = Gson().fromJson(itemList, listType)
        val labelList = list.map { x -> x.label }
        EventBus.getDefault().post(VehicleInfoEvent(Status.SUCCESS, null, type, labelList))

        when(type) {

            VehicleInfoEventType.YEAR -> {
                this.years = list
            }

            VehicleInfoEventType.MAKE -> {
                this.makes = list
            }

            VehicleInfoEventType.MODEL -> {
                this.models = list
            }

            VehicleInfoEventType.ENGINE -> {
                this.engines = list
            }

        }
    }
}