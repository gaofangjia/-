package com.example.data.repository

import com.example.data.dao.VehicleDao
import com.example.data.entity.FuelRecord
import com.example.data.entity.MaintenanceRecord
import com.example.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun getFuelRecords(vehicleId: Int): Flow<List<FuelRecord>> {
        return vehicleDao.getFuelRecordsForVehicle(vehicleId)
    }

    fun getMaintenanceRecords(vehicleId: Int): Flow<List<MaintenanceRecord>> {
        return vehicleDao.getMaintenanceRecordsForVehicle(vehicleId)
    }

    suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun insertFuelRecord(record: FuelRecord): Long {
        return vehicleDao.insertFuelRecord(record)
    }

    suspend fun updateFuelRecord(record: FuelRecord) {
        vehicleDao.updateFuelRecord(record)
    }

    suspend fun deleteFuelRecord(record: FuelRecord) {
        vehicleDao.deleteFuelRecord(record)
    }

    suspend fun insertMaintenanceRecord(record: MaintenanceRecord): Long {
        return vehicleDao.insertMaintenanceRecord(record)
    }

    suspend fun updateMaintenanceRecord(record: MaintenanceRecord) {
        vehicleDao.updateMaintenanceRecord(record)
    }

    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord) {
        vehicleDao.deleteMaintenanceRecord(record)
    }
}
