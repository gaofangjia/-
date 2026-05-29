package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val plateNumber: String = "",
    val vehicleType: String = "汽油车", // 汽油车, 柴油车, 纯电动, 插电混动, 摩托车
    val initialOdometer: Double = 0.0,
    val notes: String = ""
)
