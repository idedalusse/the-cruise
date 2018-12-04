package com.zacharymikel.thecruise.Event

import com.zacharymikel.thecruise.Constants.Status
import com.zacharymikel.thecruise.Constants.VehicleInfoEventType

/**
 * Created by zacharymikel on 4/22/18.
 */
class VehicleInfoEvent(
        var status: Status,
        var message: String? = null,
        var type: VehicleInfoEventType,
        var data: Any? = null
)