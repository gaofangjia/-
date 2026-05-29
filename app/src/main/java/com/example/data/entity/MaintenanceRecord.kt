package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val dateMs: Long,
    val odometer: Double,
    val serviceType: String, // e.g. 小保养, 大保养, 更换配件, 维修, 检查
    val cost: Double,
    val provider: String = "", // e.g. 4S店, 途虎, 自备维护
    val notes: String = "" // detail notes
)
