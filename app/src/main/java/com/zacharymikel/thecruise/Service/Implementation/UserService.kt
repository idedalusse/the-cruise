package com.zacharymikel.thecruise.Service.Implementation

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.zacharymikel.thecruise.Event.EventHandler
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Model.User
import com.zacharymikel.thecruise.Service.Interface.IUserService

object UserService : IUserService {

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance().reference.child("Users")
    private val TAG = "UserService"

    override fun signUp(user: User) {
        val auth: FirebaseAuth? = FirebaseAuth.getInstance()

        auth!!.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { task ->
                    EventHandler.handleResult(task, EventType.SIGN_UP)
                }

    }

    override fun update(user: User) {
        val currentUser = db.child(user.userId)
        currentUser.child("name").setValue(user.name)
    }

    override fun login(email: String, password: String) {

        val auth = FirebaseAuth.getInstance()

        auth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    EventHandler.handleResult(task, EventType.LOGIN)
                }

    }

    fun getUserId(): String? {

        try {
            return auth.currentUser!!.uid
        }

        catch(e: Exception) {
            Log.e(TAG, "No user is currently logged in.")
        }

        return null
    }

}
