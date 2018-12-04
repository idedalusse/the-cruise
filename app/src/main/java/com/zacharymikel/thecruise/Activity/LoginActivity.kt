package com.zacharymikel.thecruise.Activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zacharymikel.thecruise.Event.Event
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.R
import com.zacharymikel.thecruise.Service.Implementation.UserService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginForm: ViewGroup
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        EventBus.getDefault().register(this)

        checkAuthenticated()
        setup()
    }

    private fun checkAuthenticated() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            mainActivity()
        }
    }

    private fun setup() {
        signUpButton = findViewById(R.id.button_sign_up)
        signUpButton.setOnClickListener { signUpActivity() }
        loginButton = findViewById(R.id.button_login)
        loginButton.setOnClickListener { login() }
        progressBar = findViewById(R.id.loading)
        loginForm = findViewById(R.id.login_form)
        forgotPassword = findViewById(R.id.forgot_password)
        forgotPassword.setOnClickListener { forgotPasswordActivity() }
    }

    private fun login() {

        val email = (findViewById<EditText>(R.id.email) as EditText).text.toString()
        val password = (findViewById<EditText>(R.id.password) as EditText).text.toString()

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.visibility = View.VISIBLE
            loginForm.visibility = View.INVISIBLE

            UserService.login(email, password)
        }

        else {

        }


    }

    @Subscribe
    fun handleLoginResult(event: Event) {

        if(event.type != EventType.LOGIN) return

        if(event.success()) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            Log.d("LoginActivity", "User signed in: " + uid)
            mainActivity()
        }

        else {
            Toast.makeText(this, getString(R.string.toast_login_fail),
            Toast.LENGTH_SHORT).show()

            progressBar.visibility = View.INVISIBLE
            loginForm.visibility = View.VISIBLE
        }

    }

    private fun mainActivity() {
        val intent = Intent(this, VehicleOverviewActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun forgotPasswordActivity() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun signUpActivity() {
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }


}
