package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.FuelRecord
import com.example.data.entity.MaintenanceRecord
import com.example.data.entity.Vehicle
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId.asStateFlow()

    // Active vehicle
    val selectedVehicle: StateFlow<Vehicle?> = combine(vehicles, _selectedVehicleId) { vehicleList, id ->
        if (id != null) {
            vehicleList.find { it.id == id }
        } else {
            vehicleList.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Automatically synchronize selectedVehicleId when vehicles list loads if it's null
    init {
        viewModelScope.launch {
            vehicles.collect { list ->
                if (_selectedVehicleId.value == null && list.isNotEmpty()) {
                    _selectedVehicleId.value = list.first().id
                }
            }
        }
    }

    fun selectVehicle(id: Int) {
        _selectedVehicleId.value = id
    }

    // Fuel/Electric charging records for active vehicle
    @OptIn(ExperimentalCoroutinesApi::class)
    val fuelRecords: StateFlow<List<FuelRecord>> = selectedVehicle
        .flatMapLatest { vehicle ->
            if (vehicle != null) {
                repository.getFuelRecords(vehicle.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Maintenance Records for active vehicle
    @OptIn(ExperimentalCoroutinesApi::class)
    val maintenanceRecords: StateFlow<List<MaintenanceRecord>> = selectedVehicle
        .flatMapLatest { vehicle ->
            if (vehicle != null) {
                repository.getMaintenanceRecords(vehicle.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics structure
    data class VehicleStats(
        val totalSpent: Double = 0.0,
        val totalFuelSpent: Double = 0.0,
        val totalMaintenanceSpent: Double = 0.0,
        val totalFuelVolume: Double = 0.0,
        val runDistance: Double = 0.0,
        val avgFuelPrice: Double = 0.0,
        val maintenanceCount: Int = 0,
        val fuelCount: Int = 0
    )

    // Combined Statistics for active vehicle
    val stats: StateFlow<VehicleStats> = combine(
        selectedVehicle,
        fuelRecords,
        maintenanceRecords
    ) { vehicle, fuels, maintenances ->
        if (vehicle == null) return@combine VehicleStats()

        val totalFuelSpent = fuels.sumOf { it.totalCost }
        val totalMaintenanceSpent = maintenances.sumOf { it.cost }
        val totalSpent = totalFuelSpent + totalMaintenanceSpent
        val totalFuelVolume = fuels.sumOf { it.fuelAmount }
        
        // Mileage calculation based on max entry odometer compared with initial odometer
        val initialOdo = vehicle.initialOdometer
        val maxFuelOdo = fuels.maxOfOrNull { it.odometer } ?: 0.0
        val maxMaintOdo = maintenances.maxOfOrNull { it.odometer } ?: 0.0
        val currentMaxOdo = maxOf(initialOdo, maxFuelOdo, maxMaintOdo)
        val runDistance = maxOf(0.0, currentMaxOdo - initialOdo)

        val avgFuelPrice = if (fuels.isNotEmpty()) {
            fuels.sumOf { it.pricePerUnit } / fuels.size
        } else {
            0.0
        }

        VehicleStats(
            totalSpent = totalSpent,
            totalFuelSpent = totalFuelSpent,
            totalMaintenanceSpent = totalMaintenanceSpent,
            totalFuelVolume = totalFuelVolume,
            runDistance = runDistance,
            avgFuelPrice = avgFuelPrice,
            maintenanceCount = maintenances.size,
            fuelCount = fuels.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleStats())

    // --- Actions ---

    fun addVehicle(name: String, plateNumber: String, vehicleType: String, initialOdometer: Double, notes: String) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                name = name,
                plateNumber = plateNumber,
                vehicleType = vehicleType,
                initialOdometer = initialOdometer,
                notes = notes
            )
            val insertedId = repository.insertVehicle(vehicle)
            _selectedVehicleId.value = insertedId.toInt()
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            if (_selectedVehicleId.value == vehicle.id) {
                _selectedVehicleId.value = null
            }
        }
    }

    fun addFuelRecord(
        vehicleId: Int,
        dateMs: Long,
        odometer: Double,
        fuelAmount: Double,
        pricePerUnit: Double,
        totalCost: Double,
        isFull: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val record = FuelRecord(
                vehicleId = vehicleId,
                dateMs = dateMs,
                odometer = odometer,
                fuelAmount = fuelAmount,
                pricePerUnit = pricePerUnit,
                totalCost = totalCost,
                isFull = isFull,
                notes = notes
            )
            repository.insertFuelRecord(record)
        }
    }

    fun deleteFuelRecord(record: FuelRecord) {
        viewModelScope.launch {
            repository.deleteFuelRecord(record)
        }
    }

    fun addMaintenanceRecord(
        vehicleId: Int,
        dateMs: Long,
        odometer: Double,
        serviceType: String,
        cost: Double,
        provider: String,
        notes: String
    ) {
        viewModelScope.launch {
            val record = MaintenanceRecord(
                vehicleId = vehicleId,
                dateMs = dateMs,
                odometer = odometer,
                serviceType = serviceType,
                cost = cost,
                provider = provider,
                notes = notes
            )
            repository.insertMaintenanceRecord(record)
        }
    }

    fun deleteMaintenanceRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            repository.deleteMaintenanceRecord(record)
        }
    }
}

class VehicleViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
