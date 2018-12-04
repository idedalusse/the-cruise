package com.zacharymikel.thecruise.Activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Model.User
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.EmailService
import com.zacharymikel.thecruise.Service.Implementation.UserService
import com.zacharymikel.thecruise.Service.Implementation.ValidationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var createAccountButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var user: User

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        initialize()
    }

    private fun initialize() {
        progressBar = findViewById(R.id.loading)
        createAccountButton = findViewById(R.id.button_sign_up)
        createAccountButton.setOnClickListener { createNewAccount() }
    }

    private fun createNewAccount() {

        val name = findViewById<TextInputLayout>(R.id.text_input_first_name_layout)
        val email = findViewById<TextInputLayout>(R.id.text_input_email_layout)
        val password = findViewById<TextInputLayout>(R.id.text_input_password_layout)

        user = ValidationService.validateFieldsForUser(name, email, password) ?: return

        progressBar.visibility = View.VISIBLE
        UserService.signUp(user)

    }


    @Subscribe
    fun handleSignUpEvent(event: Event) {

        if(event.type != EventType.SIGN_UP) return

        if(event.success()) {

            val auth = FirebaseAuth.getInstance()

            // get the user's id
            val userId = auth.currentUser!!.uid
            user.userId = userId
            Log.d("CreateAccountActivity", "User signed in: " + userId)

            // verify email
            EmailService.verificationEmail(user)
            UserService.update(user)

            Toast.makeText(this, getString(R.string.activity_signup_success), Toast.LENGTH_SHORT).show()
            nextActivity()
        }

        else {
            Toast.makeText(this, getString(R.string.toast_signup_fail, event.message),
                    Toast.LENGTH_SHORT).show()
        }

    }


    private fun handleSignUpResult(success: Boolean) {

        if(success) {

            val auth = FirebaseAuth.getInstance()

            // get the user's id
            val userId = auth.currentUser!!.uid
            user.userId = userId
            Log.d("CreateAccountActivity", "User signed in: " + userId)

            // Verify Email
            EmailService.verificationEmail(user)
            UserService.update(user)

            nextActivity()
        }

        else {
            Toast.makeText(this, getString(R.string.toast_signup_fail), Toast.LENGTH_SHORT).show()
        }

    }

    private fun nextActivity() {
        //start next activity
        val intent = Intent(this, VehicleOverviewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun verifyEmail(success: Boolean) {

        if(success) {
            Toast.makeText(this, String.format(getString(R.string.toast_verification_sent), user.email),
                    Toast.LENGTH_SHORT).show()
        }

        else {
            Toast.makeText(this, "Failed to send verification email.",
                    Toast.LENGTH_SHORT).show()
        }

    }

}
