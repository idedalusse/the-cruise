package com.zacharymikel.thecruise.Service.Interface

import com.zacharymikel.thecruise.Model.User

/**
 * Created by z.mikel on 3/30/2018.
 */

interface IEmailService {

    fun verificationEmail(user: User)
    fun sendResetEmail(email: String)

}