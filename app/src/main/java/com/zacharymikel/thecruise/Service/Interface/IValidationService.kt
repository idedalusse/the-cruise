package com.zacharymikel.thecruise.Service.Interface

/**
 * Created by zacharymikel on 4/14/18.
 */

interface IValidationService {
    fun simpleValidate(length: Int = -1, type: Int, text: String): Boolean
    fun minLength(length: Int = -1, text: String): Boolean
}