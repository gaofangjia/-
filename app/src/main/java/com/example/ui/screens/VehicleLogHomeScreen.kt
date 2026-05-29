package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.FuelRecord
import com.example.data.entity.MaintenanceRecord
import com.example.data.entity.Vehicle
import com.example.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VehicleLogHomeScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val selectedVehicle by viewModel.selectedVehicle.collectAsStateWithLifecycle()
    val fuelRecords by viewModel.fuelRecords.collectAsStateWithLifecycle()
    val maintenanceRecords by viewModel.maintenanceRecords.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: 加油/充电, 1: 保养记录

    // Dialog flags
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddMaintDialog by remember { mutableStateOf(false) }
    var showVehicleManagerDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "App Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "机动车记录本",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showVehicleManagerDialog = true },
                        modifier = Modifier.testTag("manage_vehicles_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "车辆管理"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedVehicle != null) {
                FloatingActionButton(
                    onClick = {
                        if (activeTab == 0) {
                            showAddFuelDialog = true
                        } else {
                            showAddMaintDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("add_record_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加记录",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (vehicles.isEmpty()) {
                // Onboarding screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "欢迎使用机动车记录本",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "这里可以独立、清晰地帮您维护不同机动车的加油、充电记录和保养维护历史。开始添加您的第一辆爱车吧！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { showAddVehicleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_add_vehicle_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加第一辆车辆", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Main content
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Quick Vehicle Info Panel & Statistics Dashboard
                    selectedVehicle?.let { vehicle ->
                        val isEv = vehicle.vehicleType in listOf("纯电动", "插电混动")
                        VehicleDashboard(
                            vehicle = vehicle,
                            stats = stats,
                            onSwitchVehicleClick = { showVehicleManagerDialog = true }
                        )

                        // High Density Action Buttons Row (Fuel/Charging & Maintenance Side-by-Side Card Buttons)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Fuel / Charging Button Card
                            Card(
                                onClick = { showAddFuelDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("density_fuel_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = if (isEv) "⚡" else "⛽", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isEv) "记录充电" else "记录加油",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Maintenance Button Card
                            Card(
                                onClick = { showAddMaintDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("density_maint_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = BorderStroke(1.dp, Color(0xFFCDDAF5))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "🔧", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "保养维护",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Record Tab Switcher
                        TabRow(
                            selectedTabIndex = activeTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                text = {
                                    Text(
                                        text = if (isEv) "加油/充电记录" else "加油明细",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.testTag("tab_fuel")
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                text = { Text(text = "保养记录", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tab_maintenance")
                            )
                        }

                        // Lists layout - Rounded High Density Card Container
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (activeTab == 0) {
                                if (fuelRecords.isEmpty()) {
                                    EmptyStateWidget(
                                        title = "暂无消费记录",
                                        description = if (isEv) "点击下方按钮记录您的第一次加油或充电明细吧！" else "点击下方按钮记录您的第一次加油明细吧！",
                                        icon = Icons.Default.LocalGasStation
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(bottom = 80.dp, top = 12.dp, start = 16.dp, end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(fuelRecords) { record ->
                                            FuelRecordItem(
                                                record = record,
                                                vehicleType = vehicle.vehicleType,
                                                onDelete = { viewModel.deleteFuelRecord(record) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                if (maintenanceRecords.isEmpty()) {
                                    EmptyStateWidget(
                                        title = "暂无保养记录",
                                        description = "定期维护车辆能让出行更安全，点击下方按钮建立您的维修/保养档案！",
                                        icon = Icons.Default.Build
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(bottom = 80.dp, top = 12.dp, start = 16.dp, end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(maintenanceRecords) { record ->
                                            MaintenanceRecordItem(
                                                record = record,
                                                onDelete = { viewModel.deleteMaintenanceRecord(record) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AddEditVehicleDialog(
            vehicleToEdit = null,
            onDismiss = { showAddVehicleDialog = false },
            onConfirm = { name, plate, type, initialOdo, notes ->
                viewModel.addVehicle(name, plate, type, initialOdo, notes)
                showAddVehicleDialog = false
            }
        )
    }

    // 2. Edit Vehicle Dialog
    if (vehicleToEdit != null) {
        AddEditVehicleDialog(
            vehicleToEdit = vehicleToEdit,
            onDismiss = { vehicleToEdit = null },
            onConfirm = { name, plate, type, initialOdo, notes ->
                vehicleToEdit?.let { old ->
                    viewModel.updateVehicle(
                        old.copy(
                            name = name,
                            plateNumber = plate,
                            vehicleType = type,
                            initialOdometer = initialOdo,
                            notes = notes
                        )
                    )
                }
                vehicleToEdit = null
            }
        )
    }

    // 3. Add Fuel Record Dialog
    if (showAddFuelDialog && selectedVehicle != null) {
        AddFuelRecordDialog(
            vehicle = selectedVehicle!!,
            onDismiss = { showAddFuelDialog = false },
            onConfirm = { date, odo, amount, price, total, isFull, notes ->
                viewModel.addFuelRecord(
                    vehicleId = selectedVehicle!!.id,
                    dateMs = date,
                    odometer = odo,
                    fuelAmount = amount,
                    pricePerUnit = price,
                    totalCost = total,
                    isFull = isFull,
                    notes = notes
                )
                showAddFuelDialog = false
            }
        )
    }

    // 4. Add Maintenance Record Dialog
    if (showAddMaintDialog && selectedVehicle != null) {
        AddMaintenanceRecordDialog(
            vehicle = selectedVehicle!!,
            onDismiss = { showAddMaintDialog = false },
            onConfirm = { date, odo, type, cost, provider, notes ->
                viewModel.addMaintenanceRecord(
                    vehicleId = selectedVehicle!!.id,
                    dateMs = date,
                    odometer = odo,
                    serviceType = type,
                    cost = cost,
                    provider = provider,
                    notes = notes
                )
                showAddMaintDialog = false
            }
        )
    }

    // 5. Vehicle Manager Dialog (Switching & Add/Edit/Delete Vehicles)
    if (showVehicleManagerDialog) {
        VehicleManagerDialog(
            vehicles = vehicles,
            selectedVehicleId = selectedVehicle?.id,
            onDismiss = { showVehicleManagerDialog = false },
            onSelect = { id ->
                viewModel.selectVehicle(id)
                showVehicleManagerDialog = false
            },
            onAddVehicleClick = {
                showAddVehicleDialog = true
                showVehicleManagerDialog = false
            },
            onEditVehicleClick = { vehicle ->
                vehicleToEdit = vehicle
                showVehicleManagerDialog = false
            },
            onDeleteVehicleClick = { vehicle ->
                viewModel.deleteVehicle(vehicle)
            }
        )
    }
}

// --- Dynamic Visual Dashboards & UI Cards ---

@Composable
fun VehicleDashboard(
    vehicle: Vehicle,
    stats: VehicleViewModel.VehicleStats,
    onSwitchVehicleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Vehicle Identity Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = vehicle.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Type Badge
                        TypeBadge(vehicle.vehicleType)
                    }

                    if (vehicle.plateNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LicensePlateWidget(
                            plateNumber = vehicle.plateNumber,
                            isNewEnergy = vehicle.vehicleType in listOf("纯电动", "插电混动")
                        )
                    }
                }

                // Switch Button
                OutlinedButton(
                    onClick = onSwitchVehicleClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("switch_vehicle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("切车辆", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            // Stats row 1: Large primary counters representing Vehicle Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Block 1: Total Spent
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyYuan,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "累计总花费",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f 元", stats.totalSpent),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Block 2: Mileage Run
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "累计记录里程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f km", stats.runDistance),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub Stats block containing refueling vs upkeep split
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val isEv = vehicle.vehicleType in listOf("纯电动", "插电混动")
                Column {
                    Text(
                        text = if (isEv) "充能消耗 (${stats.fuelCount}笔)" else "加油消耗 (${stats.fuelCount}笔)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f 元", stats.totalFuelSpent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "保养消耗 (${stats.maintenanceCount}笔)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f 元", stats.totalMaintenanceSpent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = if (isEv) "消耗电量" else "消耗油量",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f %s", stats.totalFuelVolume, if (isEv) "kWh" else "L"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Custom Licence Plate Graphic
@Composable
fun LicensePlateWidget(
    plateNumber: String,
    isNewEnergy: Boolean
) {
    val plateBg = if (isNewEnergy) {
        // High-fidelity Green-To-White gradient characteristic of Chinese New Energy Plates
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFE0F7FA), Color(0xFFC8E6C9), Color(0xFF81C784))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF0F52BA), Color(0xFF1E3F66))
        )
    }

    val textColor = if (isNewEnergy) Color(0xFF111111) else Color.White
    val borderColor = if (isNewEnergy) Color(0xFF388E3C) else Color(0xFF4A90E2)

    Box(
        modifier = Modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .background(brush = plateBg, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = plateNumber.uppercase(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TypeBadge(type: String) {
    val badgeColor = when (type) {
        "纯电动" -> Color(0xFF4CAF50)
        "插电混动" -> Color(0xFF009688)
        "汽油车" -> Color(0xFFE91E63)
        "柴油车" -> Color(0xFF9C27B0)
        "摩托车" -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = type,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Empty state illustrator
@Composable
fun EmptyStateWidget(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Record Card Items
@Composable
fun FuelRecordItem(
    record: FuelRecord,
    vehicleType: String,
    onDelete: () -> Unit
) {
    val isEv = vehicleType in listOf("纯电动", "插电混动")
    val volUnit = if (isEv) "度(kWh)" else "升(L)"
    val priceUnit = if (isEv) "元/度" else "元/升"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // High Density style Circle Avatar from Design Specs
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFFF2B8B5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEv) "⚡" else "⛽",
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f 元", record.totalCost),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (record.isFull && !isEv) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "加满",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Logistics split
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "里程: ${String.format(Locale.getDefault(), "%.1f", record.odometer)} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "数量: ${String.format(Locale.getDefault(), "%.2f", record.fuelAmount)} $volUnit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "单价: ${String.format(Locale.getDefault(), "%.2f", record.pricePerUnit)} $priceUnit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = record.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formatEpochDate(record.dateMs),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_fuel_record_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除记录",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MaintenanceRecordItem(
    record: MaintenanceRecord,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // High Density style Circle Avatar from Design Specs
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFFC2E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔧",
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = record.serviceType,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f 元", record.cost),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "里程: ${String.format(Locale.getDefault(), "%.1f", record.odometer)} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.provider.isNotBlank()) {
                        Text(
                            text = "服务商: ${record.provider}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = record.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formatEpochDate(record.dateMs),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_maint_record_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除记录",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// --- Forms & Popup Card Sheets ---

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleDialog(
    vehicleToEdit: Vehicle?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, plate: String, type: String, initialOdo: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(vehicleToEdit?.name ?: "") }
    var plateNumber by remember { mutableStateOf(vehicleToEdit?.plateNumber ?: "") }
    var vehicleType by remember { mutableStateOf(vehicleToEdit?.vehicleType ?: "汽油车") }
    var initialOdomText by remember { mutableStateOf(vehicleToEdit?.initialOdometer?.toString() ?: "") }
    var notes by remember { mutableStateOf(vehicleToEdit?.notes ?: "") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (vehicleToEdit == null) "添加新机动车" else "编辑机动车记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("车辆名称") },
                    placeholder = { Text("例如：卡罗拉、特斯拉等") },
                    singleLine = true,
                    isError = hasError && name.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_name_input")
                )
                if (hasError && name.isBlank()) {
                    Text(
                        text = "车辆名称不能为空",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Plate number
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = { Text("车牌号码 (选填)") },
                    placeholder = { Text("例如：沪A88888") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_plate_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Chips Field
                Text(
                    text = "车辆类型",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val typesList = listOf("汽油车", "纯电动", "插电混动", "柴油车", "摩托车")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    typesList.forEach { type ->
                        val selected = vehicleType == type
                        FilterChip(
                            selected = selected,
                            onClick = { vehicleType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Initial odometer
                OutlinedTextField(
                    value = initialOdomText,
                    onValueChange = { initialOdomText = it },
                    label = { Text("初始里程 (km)") },
                    placeholder = { Text("例如：0.0 或当前二手购入里程") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && initialOdomText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_odo_input")
                )
                if (hasError && initialOdomText.toDoubleOrNull() == null) {
                    Text(
                        text = "请输入有效的里程数值",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注记录 (选填)") },
                    placeholder = { Text("记录车架号、保险到期日等") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_notes_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val odo = initialOdomText.toDoubleOrNull()
                            if (name.isNotBlank() && odo != null) {
                                onConfirm(name, plateNumber, vehicleType, odo, notes)
                            } else {
                                hasError = true
                            }
                        },
                        modifier = Modifier.testTag("vehicle_dialog_confirm_btn")
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
fun AddFuelRecordDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (date: Long, odo: Double, amount: Double, price: Double, total: Double, isFull: Boolean, notes: String) -> Unit
) {
    val isEv = vehicle.vehicleType in listOf("纯电动", "插电混动")
    val labelAmount = if (isEv) "电量 (kWh)" else "油量 (L)"
    val labelPrice = if (isEv) "电价单价 (元/度)" else "油价单价 (元/L)"

    var odometerText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var totalCostText by remember { mutableStateOf("") }
    var isFull by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    // Auto-calculate logic: total cost auto calculation
    LaunchedEffect(amountText, priceText) {
        val qty = amountText.toDoubleOrNull()
        val cst = priceText.toDoubleOrNull()
        if (qty != null && cst != null) {
            totalCostText = String.format(Locale.getDefault(), "%.2f", qty * cst)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEv) "做一笔充电记录" else "做一笔加油记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Mileage
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it },
                    label = { Text("当前仪表里程 (km)") },
                    placeholder = { Text("例如：12580") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && odometerText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_odo_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Volume quantity
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(labelAmount) },
                    placeholder = { Text("例如：45") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && amountText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_qty_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price unit
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(labelPrice) },
                    placeholder = { Text("例如：7.81") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && priceText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_price_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Total Cost
                OutlinedTextField(
                    value = totalCostText,
                    onValueChange = { totalCostText = it },
                    label = { Text("交易总费用 (元)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && totalCostText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_total_input")
                )

                // Fill Tank selection (only make sense for fluid tank, disabled or omitted for EV)
                if (!isEv) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFull,
                            onCheckedChange = { isFull = it },
                            modifier = Modifier.testTag("fuel_isfull_chk")
                        )
                        Text(
                            text = "是否加满油箱",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("消费备注 (选填)") },
                    placeholder = { Text(if (isEv) "例如：特来电快充" else "例如：中石化95号汽油") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_notes_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val odo = odometerText.toDoubleOrNull()
                            val amt = amountText.toDoubleOrNull()
                            val prc = priceText.toDoubleOrNull()
                            val tot = totalCostText.toDoubleOrNull()
                            if (odo != null && amt != null && prc != null && tot != null) {
                                onConfirm(System.currentTimeMillis(), odo, amt, prc, tot, isFull, notes)
                            } else {
                                hasError = true
                            }
                        },
                        modifier = Modifier.testTag("fuel_dialog_confirm_btn")
                    ) {
                        Text("记录")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceRecordDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (date: Long, odo: Double, type: String, cost: Double, provider: String, notes: String) -> Unit
) {
    var odometerText by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("小保养") }
    var costText by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "做一笔维护保养记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Odometer
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it },
                    label = { Text("维护时车辆里程 (km)") },
                    placeholder = { Text("例如：15000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && odometerText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maint_odo_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Service type choices
                Text(
                    text = "维护保养类型",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val maintenanceTypes = listOf("小保养", "大保养", "更换耗材", "维修事故", "定期检测", "其它")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    maintenanceTypes.forEach { type ->
                        val selected = serviceType == type
                        FilterChip(
                            selected = selected,
                            onClick = { serviceType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cost
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("保养费用 (元)") },
                    placeholder = { Text("例如：350") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && costText.toDoubleOrNull() == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maint_cost_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Provider
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("服务门店 / 实施人") },
                    placeholder = { Text("例如：4S店、汽修一厂、自带自带") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maint_provider_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes detailing replacements
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("维护保养档案详情") },
                    placeholder = { Text("例如：更换原厂机油5W-30、更换机油滤清器、吹洗空调滤芯。") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maint_notes_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val odo = odometerText.toDoubleOrNull()
                            val cost = costText.toDoubleOrNull()
                            if (odo != null && cost != null && serviceType.isNotBlank()) {
                                onConfirm(System.currentTimeMillis(), odo, serviceType, cost, provider, notes)
                            } else {
                                hasError = true
                            }
                        },
                        modifier = Modifier.testTag("maint_dialog_confirm_btn")
                    ) {
                        Text("建立档案")
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleManagerDialog(
    vehicles: List<Vehicle>,
    selectedVehicleId: Int?,
    onDismiss: () -> Unit,
    onSelect: (id: Int) -> Unit,
    onAddVehicleClick: () -> Unit,
    onEditVehicleClick: (Vehicle) -> Unit,
    onDeleteVehicleClick: (Vehicle) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 500.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "车位库与车辆管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onAddVehicleClick,
                        modifier = Modifier.testTag("add_vehicle_from_manager")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "添加车辆",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vehicles) { vehicle ->
                        val isSelected = vehicle.id == selectedVehicleId
                        val borderMod = if (isSelected) {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else Modifier

                        Card(
                            onClick = { onSelect(vehicle.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }
                            ),
                            modifier = borderMod.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = vehicle.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        TypeBadge(vehicle.vehicleType)
                                    }
                                    if (vehicle.plateNumber.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "车牌号: ${vehicle.plateNumber}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditVehicleClick(vehicle) },
                                        modifier = Modifier.testTag("edit_vehicle_btn_${vehicle.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "修改车辆",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (vehicles.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteVehicleClick(vehicle) },
                                            modifier = Modifier.testTag("delete_vehicle_btn_${vehicle.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteForever,
                                                contentDescription = "注销删除车辆",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("完成")
                    }
                }
            }
        }
    }
}

// Utility to format timestamp to human date
fun formatEpochDate(ms: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(ms))
}
