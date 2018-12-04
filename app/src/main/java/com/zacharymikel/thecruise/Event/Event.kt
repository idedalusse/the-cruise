package com.zacharymikel.thecruise.Event

import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.Status

open class Event(
        var status: Status,
        var message: String? = null,
        var type: EventType,
        var data: Any? = null

) {

    fun success(): Boolean {
        return this.status == Status.SUCCESS
    }

}