package com.zacharymikel.thecruise.Event

import com.google.android.gms.tasks.Task
import com.zacharymikel.thecruise.Constants.EventType
import com.zacharymikel.thecruise.Constants.Status
import org.greenrobot.eventbus.EventBus

object EventHandler {

    fun handleResult(task: Task<*>, eventType: EventType) {

        val event = if(task.isSuccessful) {
            Event(Status.SUCCESS, null, eventType)
        } else {
            Event(Status.FAIL, task.exception.toString(), eventType)
        }

        EventBus.getDefault().post(event)
    }


}