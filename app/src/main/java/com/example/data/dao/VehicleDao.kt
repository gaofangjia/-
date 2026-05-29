package com.example.data.dao

import androidx.room.*
import com.example.data.entity.FuelRecord
import com.example.data.entity.MaintenanceRecord
import com.example.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    // --- Vehicle Operations ---
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: Int): Flow<Vehicle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)


    // --- Fuel Record Operations ---
    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY dateMs DESC")
    fun getFuelRecordsForVehicle(vehicleId: Int): Flow<List<FuelRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: FuelRecord): Long

    @Update
    suspend fun updateFuelRecord(record: FuelRecord)

    @Delete
    suspend fun deleteFuelRecord(record: FuelRecord)


    // --- Maintenance Record Operations ---
    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY dateMs DESC")
    fun getMaintenanceRecordsForVehicle(vehicleId: Int): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRecord(record: MaintenanceRecord): Long

    @Update
    suspend fun updateMaintenanceRecord(record: MaintenanceRecord)

    @Delete
    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord)
}
