package com.zacharymikel.thecruise.Activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.support.v4.app.NavUtils
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import com.zacharymikel.thecruise.Constants.MaintenanceItemAction
import com.zacharymikel.thecruise.Model.FormInput.FormInput
import com.zacharymikel.thecruise.Model.MaintenanceItem
import com.zacharymikel.thecruise.Model.Validator
import com.zacharymikel.thecruise.R
import ru.kolotnev.formattedittext.CurrencyEditText
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*



class AddMaintenanceActivity : AppCompatActivity() {

    private lateinit var titleView: FormInput
    private lateinit var dueDateView: TextInputEditText
    private lateinit var costView: CurrencyEditText
    private lateinit var descriptionView: FormInput
    private lateinit var submit: Button
    private lateinit var cancel: Button
    private lateinit var action: MaintenanceItemAction


    private lateinit var maintenanceItem: MaintenanceItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_maintenance)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val existingItem = intent.getParcelableExtra<MaintenanceItem>("maintenanceItem")
        action = intent.getSerializableExtra("action") as MaintenanceItemAction

        val view = this.findViewById<View>(android.R.id.content)
        setup(view)


        /*
        If we're editing a maintenance item, all of the fields in the form should be
        populated with the old information. The maintenance item model is passed in from the
        calling activity, so we set the model to that old item here. If any of the fields are
        changed, the model values will be updated in the validate() method.
        */

        if(action == MaintenanceItemAction.EDIT && existingItem != null) {

            titleView.setInput(existingItem.title!!)
            descriptionView.setInput(existingItem.description!!)
            dueDateView.setText(DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(existingItem.dueDate))
            costView.setText(NumberFormat.getCurrencyInstance().format(existingItem.cost))

            maintenanceItem = existingItem

        }

        /*
        If we're creating a new maintenance item, all of the fields should be blank, and we will
        start up a new maintenance item for our model. The vehicle id hasn't been set yet, so do that
        here. We don't set this in the caller activity because we want to start with a clean slate
        so that this activity is responsible for all of the model info that the caller needs.
         */
        else {

            val vehicleId = intent.getStringExtra("vehicleId")
            maintenanceItem = MaintenanceItem()
            maintenanceItem.vehicleId = vehicleId
        }


    }

    private fun setup(view: View) {

        titleView = FormInput().newInstance(
                view,
                R.id.title_input_layout,
                R.id.title,
                Validator(inputType = InputType.TYPE_CLASS_TEXT),
                List(1) { "Please include a title." }
        )

        descriptionView = FormInput().newInstance(
                view,
                R.id.description_input_layout,
                R.id.description
        )

        dueDateView = findViewById(R.id.due_date)
        dueDateView.setOnFocusChangeListener { view, hasFocus ->
            if(!hasFocus) {
                // validateDueDate()
            }
        }

        dueDateView.setOnTouchListener { view, motionEvent ->
            if(motionEvent!!.action == MotionEvent.ACTION_UP) {
                showDatePickerDialog()
            }
            false
        }

        costView = findViewById(R.id.cost)
        costView.setOnFocusChangeListener { view, hasFocus ->
            if(!hasFocus) {
                // validatecostView()
            }
        }

        submit = findViewById(R.id.submit)

        submit.setOnClickListener {

            val completingItem = (action == MaintenanceItemAction.COMPLETE)

            if(validate(completingItem)) {
                val intent = Intent()
                intent.putExtra("maintenanceItem", maintenanceItem)
                intent.putExtra("action", action)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }
    
    private fun validate(completingItem: Boolean): Boolean {

        if(maintenanceItem.dueDate == null) {
            dueDateView.error = "Please enter a due date."
        }

        if(!titleView.validate()) {
            return false
        }

        if(!descriptionView.validate()) {
            return false
        }

        maintenanceItem.createdDate = Date()
        maintenanceItem.cost = costView.value.toDouble()
        maintenanceItem.completed = completingItem
        maintenanceItem.description = descriptionView.getInput()
        maintenanceItem.default = false
        maintenanceItem.recurring = false
        maintenanceItem.title = titleView.getInput()

        return true
    }

    fun showDatePickerDialog(): Boolean {
        val df = DatePickerFragment()
        df.setDateCallback { d ->
            dueDateView.setText(DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(d))
            maintenanceItem.dueDate = d
        }
        df.show(supportFragmentManager, "timePicker")
        return false
    }


    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        lateinit var dateFunc: (d: Date) -> Unit

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(activity, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            // Do something with the date chosen by the user
            val c = GregorianCalendar()
            c.set(year, month, day)
            dateFunc.invoke(c.time)
        }

        fun setDateCallback(df: (d: Date) -> Unit) {
            this.dateFunc = df
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if(this.action == MaintenanceItemAction.EDIT) {
            menuInflater.inflate(R.menu.menu_edit_maintenance, menu)
            val item = menu!!.findItem(R.id.remove)
            val icon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_forever_black_24px, null)
            icon!!.setColorFilter(ResourcesCompat.getColor(resources, R.color.white, null), PorterDuff.Mode.SRC_IN)
            item.icon = icon
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.remove -> {
            removeItem()
            true
        }

        android.R.id.home -> {
            NavUtils.navigateUpFromSameTask(this)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun removeItem() {

        if(action == MaintenanceItemAction.EDIT) {
            val intent = Intent()
            intent.putExtra("maintenanceItem", maintenanceItem)
            intent.putExtra("action", MaintenanceItemAction.DELETE)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }



}
