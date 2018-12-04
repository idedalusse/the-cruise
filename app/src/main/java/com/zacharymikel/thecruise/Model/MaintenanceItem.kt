package com.zacharymikel.thecruise.Model

import android.os.Parcel
import android.os.Parcelable
import java.text.DateFormat
import java.util.*

/**
 * Created by zacharymikel on 4/14/18.
 */
class MaintenanceItem() : Parcelable {

    var uuid: String? = null
    var vehicleId: String? = null
    var createdDate: Date? = null
    var dueDate: Date? = null
    var description: String? = null
    var recurring: Boolean = false
    var completed: Boolean = false
    var cost: Double? = null
    var title: String? = null
    var default: Boolean = false

    constructor(parcel: Parcel) : this() {
        uuid = parcel.readString()
        vehicleId = parcel.readString()
        createdDate = Date(parcel.readLong())
        dueDate = Date(parcel.readLong())
        description = parcel.readString()
        cost = parcel.readDouble()
        title = parcel.readString()
        recurring = parcel.readByte() == 1.toByte()
        completed = parcel.readByte() == 1.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

        // if this was added by the fragment as a placeholder, don't write it!
        if(this.default) return

        parcel.writeString(uuid)
        parcel.writeString(vehicleId)
        parcel.writeLong(createdDate!!.time)
        parcel.writeLong(dueDate!!.time)
        parcel.writeString(description)
        parcel.writeDouble(cost!!)
        parcel.writeString(title)
        parcel.writeByte({ if(recurring) 1 else 0 }().toByte())
        parcel.writeByte({ if(completed) 1 else 0 }().toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MaintenanceItem> {
        override fun createFromParcel(parcel: Parcel): MaintenanceItem {
            return MaintenanceItem(parcel)
        }

        override fun newArray(size: Int): Array<MaintenanceItem?> {
            return arrayOfNulls(size)
        }
    }

    fun createdDateStr(): String {
        return if(this.createdDate != null) formatDate(this.createdDate!!) else ""
    }

    fun dueDateStr(): String {
        return if(this.createdDate != null) formatDate(this.createdDate!!) else ""
    }

    private fun formatDate(d: Date): String {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(d)
    }

}