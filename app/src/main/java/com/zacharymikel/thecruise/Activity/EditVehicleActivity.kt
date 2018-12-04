package com.zacharymikel.thecruise.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import com.zacharymikel.thecruise.Constants.*
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Event.VehicleInfoEvent
import com.zacharymikel.thecruise.Model.FormInput.FormInput
import com.zacharymikel.thecruise.Model.Validator
import com.zacharymikel.thecruise.Model.Vehicle
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.VehicleInfoService
import com.zacharymikel.thecruise.Service.Interface.IVehicleInfoService
import kotlinx.android.synthetic.main.activity_edit_vehicle.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class EditVehicleActivity : AppCompatActivity() {

    private val TAG = "EditVehicleActivity"
    private lateinit var yearInput: FormInput
    private lateinit var makeInput: FormInput
    private lateinit var modelInput: FormInput
    private lateinit var colorInput: FormInput
    private lateinit var engineInput: FormInput
    private lateinit var saveButton: Button
    private lateinit var editImageButton: ImageView
    private lateinit var image: ImageView

    private var year: String? = null
    private var make: String? = null
    private var model: String? = null
    private var engine: String? = null
    private var uuid: String? = null
    private var userId: String? = null
    private var imageUri: String? = null
    private var action: VehicleAction? = null

    private lateinit var vehicleInfoService: IVehicleInfoService

    override fun onCreate(savedInstanceState: Bundle?) {

        // app notifications can bring us here, so make sure the user is signed in
        // they may have signed out since the notification popped up
        checkAuthenticated()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_vehicle)
        setSupportActionBar(toolbar)
        EventBus.getDefault().register(this)

        val view = findViewById<View>(android.R.id.content)
        action = intent.getSerializableExtra("action") as VehicleAction
        val vehicle: Vehicle? = intent.getParcelableExtra("vehicle")

        setup(view, action, vehicle)

    }


    private fun checkAuthenticated() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if(user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    private fun setup(view: View, action: VehicleAction?, vehicle: Vehicle?) {

        vehicleInfoService = VehicleInfoService()
        vehicleInfoService.getYearRange()

        yearInput = FormInput().newInstance(
                view = view,
                layoutId = R.id.year_input_layout,
                inputId = R.id.year_input,
                validator = Validator(4, InputType.TYPE_CLASS_NUMBER),
                errorMessages = List<String>(1) { "Please enter a 4-digit year." },
                callback = this::yearChanged,
                options = ArrayList()
        )

        makeInput = FormInput().newInstance(
                view = view,
                layoutId = R.id.make_input_layout,
                inputId = R.id.make_input,
                validator = Validator(-1, InputType.TYPE_CLASS_TEXT),
                errorMessages = List<String>(1) { "Please enter a vehicle make." },
                callback = this::makeChanged
        )

        modelInput = FormInput().newInstance(
                view = view,
                layoutId = R.id.model_input_layout,
                inputId = R.id.model_input,
                validator = Validator(-1, InputType.TYPE_CLASS_TEXT),
                errorMessages = List<String>(1) { "Please enter a vehicle model." },
                callback = this::modelChanged
        )

        val colors = ArrayList<String>()
        colors.add("Blue")
        colors.add("Red")
        colors.add("Black")
        colors.add("Green")
        colors.add("Silver")
        colors.add("Yellow")
        colors.add("White")
        colors.add("Purple")
        colors.sortBy { x -> x }

        colorInput = FormInput().newInstance(
                view = view,
                layoutId = R.id.color_input_layout,
                inputId = R.id.color_input,
                options = colors,
                callback = this::colorChanged
        )

        engineInput = FormInput().newInstance(
                view = view,
                layoutId = R.id.engine_input_layout,
                inputId = R.id.engine_input,
                callback = this::engineChanged
        )

        editImageButton = findViewById(R.id.icon_edit_vehicle_image)
        editImageButton.setOnClickListener {
            editImage()
        }

        image = findViewById(R.id.vehicle_image)
        image.setOnClickListener {
            editImage()
        }

        saveButton = findViewById(R.id.save)
        saveButton.setOnClickListener {

            saveVehicle()

        }

        // Populate the fields on this form if the vehicle already exists
        if(action == VehicleAction.EDIT) {

            yearInput.setInput(vehicle!!.year)
            makeInput.setInput(vehicle.make)
            modelInput.setInput(vehicle.model)
            colorInput.setInput(vehicle.color)

            yearInput.setOptions(List<String>(1) { vehicle.year })
            makeInput.setOptions(List<String>(1) { vehicle.make })
            modelInput.setOptions(List<String>(1) { vehicle.model })

            if(vehicle.engine != null) {
                engineInput.setOptions(List<String>(1) { vehicle.engine!! })
            }


            year = vehicle.year
            make = vehicle.make
            model = vehicle.model
            engine = vehicle.engine
            userId = vehicle.userId
            uuid = vehicle.uuid
            imageUri = vehicle.imageFilePath

            if(vehicle.engine != null) {
                engineInput.setInput(vehicle.engine!!)
            }

            if(imageUri != null) {
                showVehicleImage(Uri.fromFile(File(imageUri)))
            }

        }

    }

    private fun saveVehicle() {

        if(validate()) {

            // pass the uuid back to the caller if they're expecting this vehicle
            // to be an existing one
            val v = Vehicle().newInstance(
                    yearInput.getInput(),
                    makeInput.getInput(),
                    modelInput.getInput(),
                    colorInput.getInput(),
                    imageUri,
                    engineInput.getInput(),
                    uuid = uuid,
                    userId = userId)

            val intent = Intent()
            intent.putExtra("vehicle", v)
            intent.putExtra("action", action)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }

    }

    private fun editImage() {
        permissionCheck()
    }

    @Subscribe
    fun onPermissionSuccess(event: Event) {

        if(event.type != EventType.PERMISSION_GRANTED) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.photo_picker_title))
        builder.setMessage(getString(R.string.photo_picker_description))
        builder.setPositiveButton(getString(R.string.photo_picker_camera)) {
            dialog, which -> takePicture()
        }
        builder.setNegativeButton(getString(R.string.photo_picker_gallery)) {
            dialog, which -> pickFromGallery()
        }
        builder.show()

    }

    @Subscribe
    fun onVehicleInfoLoaded(event: VehicleInfoEvent) {

        runOnUiThread {

            if(event.data != null) {

                when(event.type) {

                    VehicleInfoEventType.YEAR -> {
                        //yearInput.setFocusable()
                        yearInput.setOptions(event.data as ArrayList<String>)
                    }

                    VehicleInfoEventType.MAKE -> {
                        //makeInput.setFocusable()
                        makeInput.setOptions(event.data as ArrayList<String>)
                    }

                    VehicleInfoEventType.MODEL -> {
                        //modelInput.setFocusable()
                        modelInput.setOptions(event.data as ArrayList<String>)
                    }

                    VehicleInfoEventType.ENGINE -> {
                        //engineInput.setFocusable()
                        engineInput.setOptions(event.data as ArrayList<String>)
                    }

                }

            }

        }

    }


    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, RequestCode.TAKE_PICTURE)
        }
    }

    private fun pickFromGallery() {
        val pickGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI)
        if(pickGalleryIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(pickGalleryIntent, RequestCode.GALLERY_PICTURE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RequestCode.TAKE_PICTURE && resultCode == Activity.RESULT_OK && data != null) {

            // get the bitmap data from the camera
            val extras = data.extras
            val imageBitmap = extras.get("data") as Bitmap

            // create a file path for the image
            val path = Environment.getExternalStorageDirectory().absolutePath + "/cruise/photo_" + Date().time.toString() + ".jpg"
            imageUri = path

            // add the image to the gallery and display it
            addToGallery(path, imageBitmap)

        }


        else if(requestCode == RequestCode.GALLERY_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data

            // convert the uri to a filepath uri
            imageUri = getRealPathFromURI(uri).path

            // show the content uri path
            showVehicleImage(uri)
        }

    }

    // display the passed-in uri in the image view
    private fun showVehicleImage(uri: Uri) {

        Picasso.get()
                .load(uri)
                .into(image)

        image.visibility = View.VISIBLE
        editImageButton.visibility = View.INVISIBLE

    }

    // extract the file system path of the uri resource
    private fun getRealPathFromURI(uri: Uri?): Uri {

        val filePath = Array<String>(1) { MediaStore.Images.Media.DATA }
        val c = this.contentResolver.query(uri, filePath, null, null, null)
        c.moveToFirst()
        val columnIndex = c.getColumnIndex(filePath[0])
        val picturePath = c.getString(columnIndex)
        c.close()

        return Uri.parse(picturePath)
    }

    // add a new image to the device's gallery
    private fun addToGallery(path: String, bitmap: Bitmap) {

        Thread().run {

            try {
                var file = File(path)
                file.parentFile.mkdirs()

                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                showVehicleImage(Uri.fromFile(file))

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun validate(): Boolean {
        return yearInput.validate() && makeInput.validate() &&
                modelInput.validate() && engineInput.validate()
    }

    fun yearChanged() {
        val y = yearInput.getInput()
        vehicleInfoService.getMakes(y)
    }

    fun makeChanged() {
        Log.d(TAG, "Make changed!")
        val y = makeInput.getInput()
        vehicleInfoService.getModels(y)
    }

    fun modelChanged() {
        Log.d(TAG, "Model changed!")
        val y = modelInput.getInput()
        vehicleInfoService.getEngines(y)
    }

    fun engineChanged() {
        Log.d(TAG, "Engine changed!")
    }

    fun colorChanged() {
        Log.d(TAG, "Engine changed!")
    }

    private fun permissionCheck() {

        // if we don't have permission, ask for it
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
            EventBus.getDefault().post(Event(Status.SUCCESS, null, EventType.PERMISSION_GRANTED))
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RequestCode.REQUEST_STORAGE_PERMISSION -> {

                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    EventBus.getDefault().post(Event(Status.SUCCESS, null, EventType.PERMISSION_GRANTED))

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

}
