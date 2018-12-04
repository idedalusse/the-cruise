package com.zacharymikel.thecruise.Service.Implementation

import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.text.TextUtils.isEmpty
import com.zacharymikel.thecruise.Model.User
import com.zacharymikel.thecruise.Service.Interface.IValidationService

object ValidationService : IValidationService {


    override fun minLength(length: Int, text: String): Boolean {
        return text.length > length
    }


    override fun simpleValidate(length: Int, type: Int, text: String): Boolean {

        var result = "^"

        when {

            length < 0 -> return text.matches(Regex("^.+$"))

            type == InputType.TYPE_CLASS_TEXT -> result += "\\w"

            type == InputType.TYPE_CLASS_NUMBER -> result += "\\d"

        }

        result += "{" + length.toString() + "}$"
        return text.matches(Regex(result))

    }


    fun validateFieldsForUser (name: TextInputLayout,
                               email: TextInputLayout,
                               password: TextInputLayout): User? {

        if(!validateEmptyField(name) || !validateEmptyField(email) || !validateEmptyField(password)) {
            return null
        }

        else {
            return User(
                    null,
                    name.editText!!.text.toString(),
                    email.editText!!.text.toString(),
                    password.editText!!.text.toString()
            )
        }

    }

    private fun validateEmptyField(t: TextInputLayout): Boolean {
        if(isEmpty(t.editText!!.text.toString())) {
            t.error = "Required"
            return false
        }
        return true
    }

}