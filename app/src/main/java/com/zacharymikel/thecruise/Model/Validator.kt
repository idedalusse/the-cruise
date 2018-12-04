package com.zacharymikel.thecruise.Model

import android.text.InputType

/**
 * Created by zacharymikel on 4/15/18.
 */

open class Validator(
        private val length: Int = -1,
        private val inputType: Int
) {

    fun validate(text: String): Boolean {
        var result = "^"

        when {

            length < 0 -> return text.matches(Regex("^.+$"))

            inputType == InputType.TYPE_CLASS_TEXT -> result += "\\w"

            inputType == InputType.TYPE_CLASS_NUMBER -> result += "\\d"

        }

        result += "{" + length.toString() + "}$"
        return text.matches(Regex(result))
    }
}
