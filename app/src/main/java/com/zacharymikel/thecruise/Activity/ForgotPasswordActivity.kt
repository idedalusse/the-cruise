package com.zacharymikel.thecruise.Activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.EmailService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var sendResetEmailButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resetForm: ViewGroup

    private val TAG = "ForgotPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        EventBus.getDefault().register(this)
        setup()
    }

    private fun setup() {
        sendResetEmailButton = findViewById(R.id.button_send_email)
        sendResetEmailButton.setOnClickListener { sendResetEmail() }
        progressBar = findViewById(R.id.loading)
        resetForm = findViewById(R.id.reset_form)
    }

    private fun sendResetEmail() {

        val email = (findViewById<EditText>(R.id.email) as EditText).text.toString()

        if(!TextUtils.isEmpty(email)) {
            progressBar.visibility = View.VISIBLE
            resetForm.visibility = View.INVISIBLE

            EmailService.sendResetEmail(email)
        }

        else {
            Toast.makeText(this, getString(R.string.toast_reset_email_fail),
                    Toast.LENGTH_SHORT).show()
        }

    }

    @Subscribe
    fun handleResetEmailResult(event: Event) {

        if(event.type != EventType.PASSWORD_RESET) return

        if(event.success()) {
            Toast.makeText(this, getString(R.string.activity_password_reset_success),
                    Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Reset email sent!")
            nextActivity()
        }

        else {
            Toast.makeText(this, getString(R.string.activity_password_reset_fail),
                    Toast.LENGTH_SHORT).show()

            progressBar.visibility = View.INVISIBLE
            resetForm.visibility = View.VISIBLE
        }

    }

    private fun nextActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

}
