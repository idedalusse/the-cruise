package com.zacharymikel.thecruise.Service.Implementation

import com.google.firebase.auth.FirebaseAuth
import com.zacharymikel.thecruise.Event.EventHandler
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Model.User
import com.zacharymikel.thecruise.Service.Interface.IEmailService

object EmailService : IEmailService {
    private var auth = FirebaseAuth.getInstance()

    override fun verificationEmail(user: User) {
        val mUser = auth.currentUser
        mUser!!.sendEmailVerification()
                .addOnCompleteListener {
                    task -> EventHandler.handleResult(task, EventType.VERIFICATION_EMAIL)
                }
    }

    override fun sendResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    task -> EventHandler.handleResult(task, EventType.PASSWORD_RESET)
                }
    }

}
