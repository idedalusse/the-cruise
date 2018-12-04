package com.zacharymikel.thecruise.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.RequestCode
import com.zacharymikel.thecruise.Constants.Status
import com.zacharymikel.thecruise.Constants.VehicleAction
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Fragment.VehicleListFragment
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.UserService
import com.zacharymikel.thecruise.Service.Implementation.VehicleService
import com.zacharymikel.thecruise.Service.Interface.IVehicleService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class VehicleOverviewActivity : VehicleListFragment.OnFragmentInteractionListener, AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var addVehicleButton: FloatingActionButton
    private var vehicleListFragment: VehicleListFragment? = null
    private var userService: UserService = UserService
    private lateinit var vehicleService: IVehicleService
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_overview)
        setSupportActionBar(findViewById(R.id.toolbar))

        vehicleService = VehicleService()

        EventBus.getDefault().register(this)
        setup()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun setup() {
        addVehicleButton = findViewById(R.id.button_add_vehicle)
        addVehicleButton.setOnClickListener { addVehicle() }

        vehicleListFragment = VehicleListFragment.newInstance(ArrayList())
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.vehicle_list_fragment, vehicleListFragment)
        trans.commit()

        loading = findViewById(R.id.loading)

        refreshOnPermissionCheck()
        initVehicles()
    }

    private fun addVehicle() {
        val intent = Intent(this, EditVehicleActivity::class.java)
        intent.putExtra("action", VehicleAction.CREATE)
        startActivityForResult(intent, RequestCode.ADD_VEHICLE)
    }

    @Subscribe
    fun vehicleAdded(event: Event) {
        if(event.type != EventType.REFRESH_VEHICLES) return
    }

    @Subscribe
    fun vehiclesRefreshed(event: Event) {

        if(event.type != EventType.VEHICLES_REFRESHED) return
        val vehicles = event.data as ArrayList<Vehicle>

        loading.visibility = View.VISIBLE

        when {

            // fragment hasn't been setup yet, init it
            vehicleListFragment == null -> {
                // the fragment hasn't been set up yet
                vehicleListFragment = VehicleListFragment.newInstance(vehicles)
                val trans = supportFragmentManager.beginTransaction()
                trans.replace(R.id.vehicle_list_fragment, vehicleListFragment)
                trans.commit()
            }

            // the fragment is setup and there are no vehicles
            else -> {
                vehicleListFragment!!.dataRefreshed(vehicles)

                if(vehicles.size == 0) {
                    findViewById<TextView>(R.id.no_vehicles_warning).visibility = View.VISIBLE
                } else {
                    findViewById<TextView>(R.id.no_vehicles_warning).visibility = View.INVISIBLE
                }

            }

        }

        loading.visibility = View.INVISIBLE

    }


    private fun initVehicles() {
        vehicleService.refreshVehicles()
    }

    private fun refreshOnPermissionCheck() {

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Do we need to show the user an explanation of permissions?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                // Fire up the permission request dialog
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        RequestCode.REQUEST_STORAGE_PERMISSION)

            }

        } else {

            // permission has already been granted, emit refresh vehicle list event
            EventBus.getDefault().post(Event(Status.SUCCESS, null, EventType.REFRESH_VEHICLES))

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RequestCode.REQUEST_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    EventBus.getDefault().post(Event(Status.SUCCESS, null, EventType.REFRESH_VEHICLES))

                } else {

                    // TODO: Show a message that says permission is denied and there's nothing we can do

                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.

            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RequestCode.ADD_VEHICLE && resultCode == Activity.RESULT_OK) {

            val v = data!!.getParcelableExtra<Vehicle>("vehicle")
            val action = data.getSerializableExtra("action")

            when(action) {

                VehicleAction.CREATE -> {
                    v.userId = userService.getUserId()
                    vehicleService.createVehicle(v)

                    refreshOnPermissionCheck()
                }

                VehicleAction.EDIT -> {

                }

                VehicleAction.DELETE -> {
                    initVehicles()
                }

            }


        }

    }


    override fun vehicleSelected(v: Vehicle) {
        val intent = Intent(this, VehicleDetailActivity::class.java)
        intent.putExtra("vehicle", v)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.logout -> {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
