package com.zacharymikel.thecruise.Model.FormInput

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.zacharymikel.thecruise.Model.Validator
import com.zacharymikel.thecruise.R


class FormInput {

    private lateinit var layout: TextInputLayout
    private lateinit var input: TextView
    private var errorMessages: List<String>? = null
    private var options: List<String>? = null
    private var model: String? = null
    private var inputEnteredCallback: (() -> Unit)? = null
    private var validator: Validator? = null

    private lateinit var defaultInput: String
    private var acTextView: AutoCompleteTextView? = null


    fun newInstance(
            view: View,
            layoutId: Int,
            inputId: Int,
            validator: Validator? = null,
            errorMessages: List<String>? = null,
            options: List<String>? = null,
            callback: (() -> Unit)? = null,
            defaultInput: String = "",
            isAcTextView: Boolean = false): FormInput {

        this.layout = view.findViewById(layoutId)
        this.input = view.findViewById(inputId)
        this.input.setOnFocusChangeListener { _, hasFocus -> if(!hasFocus) validate() }
        this.errorMessages = errorMessages
        this.options = options
        this.validator = validator
        this.inputEnteredCallback = callback
        this.defaultInput = defaultInput

        // set the text on the input
        if(!this.defaultInput.isBlank()) {
            this.input.setText(this.defaultInput)
        }

        // if options are passed in, this is an auto-complete text view
        // we should populate the options list
        if(options != null) {

            // set the options list for the dropdown
            val adapter = ArrayAdapter<String>(input.context, R.layout.simple_list_item_1, this.options)
            acTextView = input as AutoCompleteTextView
            acTextView!!.setAdapter(adapter)

            // if the input is blank and the field has focus, show the dropdown
            acTextView!!.setOnFocusChangeListener { view, hasFocus ->
                if(!hasFocus && validate()) {
                    shouldFireCallback()
                }
            }

        }

        return this
    }

    /**
     * Determine whether the input's after-text callback listener should be fired.
     * This helps to notify the controller when the input has changed in case
     * other option values need to be modified on-the-fly.
     */
    private fun shouldFireCallback() {
        if(inputEnteredCallback != null && this.options != null &&
                this.options!!.isNotEmpty()) inputEnteredCallback!!.invoke()
    }

    /**
     * Use this class's Validator to determine whether the input from the
     * user is valid. If it's valid, set the internal model to the value
     * that was entered.
     *
     */
    fun validate(): Boolean {

        if(validator == null) return true

        val input = getInput()

        var result = validator!!.validate(input)

        // if we have a list of options, make sure the user entered something
        // from the options list
        if(options != null && options!!.isNotEmpty()) {
            result = result && options!!.contains(input)
        }

        if(!result) {
            setError(errorMessages!![0])

        } else {
            removeError()
            model = input
        }
        return result
    }

    fun getInput(): String {
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

    fun setOptions(options: List<String>) {
        this.options = options

        if(acTextView == null) {
            acTextView = input as AutoCompleteTextView
            acTextView!!.setOnFocusChangeListener { view, hasFocus ->
                if(!hasFocus && validate()) {
                    shouldFireCallback()
                }
            }
        }

        if(acTextView!!.adapter == null) {
            acTextView!!.setAdapter(ArrayAdapter<String>(acTextView!!.context, R.layout.simple_list_item_1, options))
        } else {
            val adapter = acTextView!!.adapter as ArrayAdapter<String>
            adapter.clear()
            adapter.addAll(options)
            adapter.notifyDataSetChanged()
        }

    }


}