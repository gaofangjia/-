package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.VehicleRepository
import com.example.ui.screens.VehicleLogHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VehicleViewModel
import com.example.ui.viewmodel.VehicleViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, DAO and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VehicleRepository(database.vehicleDao())
        
        // Instantiate ViewModel
        val viewModel = ViewModelProvider(
            this,
            VehicleViewModelFactory(repository)
        )[VehicleViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VehicleLogHomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}

