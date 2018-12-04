package com.zacharymikel.thecruise.Service.Interface

import com.zacharymikel.thecruise.Model.User


interface IUserService {
    fun signUp(user: User)
    fun update(user: User)
    fun login(email: String, password: String)
}
