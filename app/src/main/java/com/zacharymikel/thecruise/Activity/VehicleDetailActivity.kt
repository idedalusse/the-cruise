package com.zacharymikel.thecruise.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.MaintenanceItemAction
import com.zacharymikel.thecruise.Constants.RequestCode
import com.zacharymikel.thecruise.Constants.VehicleAction
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Fragment.MaintenanceListFragment
import com.zacharymikel.thecruise.Model.MaintenanceItem
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.MaintenanceService
import com.zacharymikel.thecruise.Service.Implementation.VehicleService
import com.zacharymikel.thecruise.Service.Interface.IMaintenanceService
import com.zacharymikel.thecruise.Service.Interface.IVehicleService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import android.app.PendingIntent
import android.support.v4.app.NotificationManagerCompat





class VehicleDetailActivity : MaintenanceListFragment.OnFragmentInteractionListener, AppCompatActivity() {

    private lateinit var maintenanceListFragment: MaintenanceListFragment
    private lateinit var vehicle: Vehicle
    private lateinit var vehicleService: IVehicleService
    private lateinit var yearMakeModelView: TextView
    private lateinit var maintenanceService: IMaintenanceService
    private lateinit var maintenanceList: ArrayList<MaintenanceItem>

    private val TAG = "VehicleDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_detail)
        EventBus.getDefault().register(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        vehicle = intent.getParcelableExtra("vehicle")

        maintenanceService = MaintenanceService()
        vehicleService = VehicleService()

        refreshMaintenanceItems()

        maintenanceList = ArrayList()

        // set vehicle model for this detail view and year/make/model title
        vehicle = intent.getParcelableExtra("vehicle")
        setVehicleInformation()

        val trans = supportFragmentManager.beginTransaction()
        maintenanceListFragment = MaintenanceListFragment.newInstance(maintenanceList)
        trans.replace(R.id.maintenance_fragment, maintenanceListFragment)
        trans.commit()

    }




    private fun setVehicleInformation() {
        yearMakeModelView = findViewById(R.id.year_make_model)
        yearMakeModelView.text = getString(R.string.year_make_model, vehicle.year,
                vehicle.make, vehicle.model)
        yearMakeModelView.setOnClickListener {
            editVehicle()
        }

        (findViewById<ImageView>(R.id.button_edit) as ImageView).setOnClickListener {
            editVehicle()
        }

        // set values in the details table
        (findViewById<TextView>(R.id.engine) as TextView).text = vehicle.engine
        (findViewById<TextView>(R.id.color) as TextView).text = vehicle.color
    }




    private fun editVehicle() {
        val intent = Intent(this, EditVehicleActivity::class.java)
        intent.putExtra("vehicle", vehicle)
        intent.putExtra("action", VehicleAction.EDIT)
        startActivityForResult(intent, RequestCode.EDIT_VEHICLE)
    }




    @Subscribe
    fun maintenanceRefresh(event: Event) {
        if(event.type != EventType.MAINTENANCE_REFRESHED || !maintenanceListFragment.isAdded) return
        maintenanceList = event.data as ArrayList<MaintenanceItem>
        maintenanceListFragment.dataRefreshed(maintenanceList)

        maintenanceList.forEach { x ->

            val cal1 = Calendar.getInstance()
            cal1.time = x.dueDate
            val y = cal1.get(Calendar.YEAR)
            val m = cal1.get(Calendar.MONTH)
            val d = cal1.get(Calendar.DAY_OF_MONTH)

            val cal2 = Calendar.getInstance()

            if(cal2.get(Calendar.YEAR) == y && cal2.get(Calendar.MONTH) == m && cal2.get(Calendar.DAY_OF_MONTH) == d) {
                createMaintenanceNotification(x)
            }

        }
    }

    private fun createMaintenanceNotification(x: MaintenanceItem) {

        val intent = Intent(this, VehicleDetailActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("vehicle", vehicle)
        intent.putExtra("action", VehicleAction.MAINTENANCE_NOTIFICATION)

        val bundle = Bundle()
        bundle.putParcelable("vehicle", vehicle)

        val pendingIntent = PendingIntent.getActivity(this, RequestCode.MAINTENANCE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Maintenance Due")
                .setContentText(x.title)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(x.description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(this)

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(RequestCode.MAINTENANCE_NOTIFICATION, mBuilder.build())
    }

    override fun refreshMaintenanceItems() {
        maintenanceService.getItemsForVehicle(vehicle.uuid)
    }

    override fun addMaintenanceItem() {
        val intent = Intent(this, AddMaintenanceActivity::class.java)
        intent.putExtra("action", MaintenanceItemAction.CREATE)
        intent.putExtra("vehicleId", vehicle.uuid)
        startActivityForResult(intent, RequestCode.ADD_MAINTENANCE_ITEM)
    }

    override fun editMaintenanceItem(m: MaintenanceItem) {

        if(m.default) {
            addMaintenanceItem()
        }

        else {

            val intent = Intent(this, AddMaintenanceActivity::class.java)
            intent.putExtra("maintenanceItem", m)
            intent.putExtra("action", MaintenanceItemAction.EDIT)
            startActivityForResult(intent, RequestCode.ADD_MAINTENANCE_ITEM)

        }
    }

    override fun completeMaintenanceItem() {
        val intent = Intent(this, AddMaintenanceActivity::class.java)
        intent.putExtra("action", MaintenanceItemAction.COMPLETE)
        startActivity(intent)
    }

    override fun removeMaintenanceItem(m: MaintenanceItem) {
        maintenanceService.removeMaintenanceItem(m)
        maintenanceListFragment.removeMaintenanceItem(m)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // On maintenance activity finish
        if(requestCode == RequestCode.ADD_MAINTENANCE_ITEM && data != null) {

            val m = data.getParcelableExtra<MaintenanceItem>("maintenanceItem")
            val action = data.getSerializableExtra("action")
            if(m != null) {

                when(action) {

                    MaintenanceItemAction.CREATE -> {
                        maintenanceListFragment.addMaintenanceItem(m)
                        maintenanceService.createMaintenanceItem(m)
                    }

                    MaintenanceItemAction.EDIT -> {
                        maintenanceListFragment.updateMaintenanceItem(m)
                        maintenanceService.updateMaintenanceItem(m)
                    }

                    MaintenanceItemAction.DELETE -> {
                        removeMaintenanceItem(m)
                    }

                }

            }

        }

        // On Edit Vehicle activity finish
        else if(requestCode == RequestCode.EDIT_VEHICLE && data != null) {

            val v = data.getParcelableExtra<Vehicle>("vehicle")
            val action = data.getSerializableExtra("action")

            if(v != null) {

                when(action) {

                    VehicleAction.EDIT -> {
                        this.vehicle = v
                        setVehicleInformation()
                        vehicleService.updateVehicle(v)
                    }

                }

            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_vehicle_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.d(TAG, item.toString())

        when (item.itemId) {

            R.id.remove -> {

                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.remove_vehicle_title))
                builder.setMessage(getString(R.string.remove_vehicle_description))
                builder.setPositiveButton(getString(R.string.remove_vehicle_confirm)) { dialog, which ->
                    removeVehicle()
                }
                builder.setNegativeButton(getString(R.string.remove_vehicle_deny)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
            }

            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
            }

            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    private fun removeVehicle() {
        vehicleService.removeVehicle(vehicle)
        val intent = Intent()
        intent.putExtra("vehicle", vehicle)
        intent.putExtra("action", VehicleAction.DELETE)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
