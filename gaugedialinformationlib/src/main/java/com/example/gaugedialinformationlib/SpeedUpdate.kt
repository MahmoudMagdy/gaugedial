package com.example.gaugedialinformationlib

import android.os.Parcel
import android.os.Parcelable


data class SpeedUpdate(val type: SpeedUpdateType, val speed: Float? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        SpeedUpdateType.valueOf(parcel.readString() ?: SpeedUpdateType.SPEED_UNAVAILABLE.name),
        parcel.readValue(Float::class.java.classLoader) as? Float
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type.name)
        parcel.writeValue(speed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpeedUpdate> {
        override fun createFromParcel(parcel: Parcel): SpeedUpdate {
            return SpeedUpdate(parcel)
        }

        override fun newArray(size: Int): Array<SpeedUpdate?> {
            return arrayOfNulls(size)
        }
    }
}
