package com.zacharymikel.thecruise.Model.FormInput

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.TextView
import com.zacharymikel.thecruise.Model.Validator


open class FormInput2 {

    private lateinit var layout: TextInputLayout
    open lateinit var input: TextView
    private lateinit var validator: Validator
    private lateinit var errorMessages: List<String>
    private var options: List<String>? = null
    private var model: Any? = null


    fun create(view: View, layoutId: Int, inputId: Int,
               validator: Validator, errorMessages: List<String>,
               options: List<String>? = null): FormInput2 {

        this.layout = view.findViewById(layoutId)
        this.input = view.findViewById(inputId)
        this.input.setOnFocusChangeListener { _, hasFocus -> if(!hasFocus) validate() }
        this.errorMessages = errorMessages
        this.options = options
        this.validator = validator

        return this
    }

    /**
     * Use this class's Validator to determine whether the input from the
     * user is valid. If it's valid, set the internal model to the value
     * that was entered.
     *
     */
    open fun validate(): Boolean {

        val input = getInput()

        val result = validator.validate(input)
        if(!result) {
            setError(errorMessages[0])

        } else {
            removeError()
            model = input
        }
        return result
    }

    private fun getInput(): String {
        return input.text.toString()
    }

    fun setInput(s: String) {
        this.input.text = s
    }

    fun setError(s: String) {
        this.layout.error = s
    }

    private fun removeError() {
        this.layout.error = null
    }


}