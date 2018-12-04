package com.zacharymikel.thecruise.Model

import android.os.Parcel
import android.os.Parcelable

class Vehicle() : Parcelable, Cloneable {

    lateinit var year: String
    lateinit var make: String
    lateinit var model: String
    lateinit var color: String
    // lateinit var driveType: String
    // var mileage: Int? = null
    var imageFilePath: String? = null
    var engine: String? = null
    var maintenanceItems: List<MaintenanceItem>? = null
    var uuid: String? = null
    var userId: String? = null
    var deleted: Boolean = false
    var user: User? = null

    constructor(parcel: Parcel) : this() {
        year = parcel.readString()
        make = parcel.readString()
        model = parcel.readString()
        color = parcel.readString()
        imageFilePath = parcel.readString()
        engine = parcel.readString()
        uuid = parcel.readString()
        userId = parcel.readString()
        deleted = parcel.readByte() == 1.toByte()
    }

    fun newInstance(
            year: String,
            make: String,
            model: String,
            color: String,
//            driveType: String,
//            mileage: Int,
            imageUri: String? = null,
            engine: String? = null,
            maintenanceItems: List<MaintenanceItem>? = null,
            user: User? = null,
            uuid: String? = null,
            userId: String? = null,
            deleted: Boolean = false

    ): Vehicle {
        this.year = year
        this.make = make
        this.model = model
        this.color = color
//        this.driveType = driveType
//        this.mileage = mileage
        this.imageFilePath = imageUri
        this.engine = engine
        this.maintenanceItems = maintenanceItems
        this.user = user
        this.uuid = uuid
        this.userId = userId
        this.deleted = deleted

        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(year)
        parcel.writeString(make)
        parcel.writeString(model)
        parcel.writeString(color)
//        parcel.writeString(driveType)
//        parcel.writeValue(mileage)
        parcel.writeString(imageFilePath)
        parcel.writeString(engine)
        parcel.writeString(uuid)
        parcel.writeString(userId)

        // this is gross, but the only foreseeable way to write a boolean value to parcel
        val d = { if(deleted) 1 else 0 }().toByte()
        parcel.writeByte(d)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }

}
