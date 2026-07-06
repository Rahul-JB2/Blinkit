package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.repository.GroceryRepository
import com.example.ui.GroceryApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GroceryViewModel
import com.example.viewmodel.GroceryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room database and repository
        val database = AppDatabase.getDatabase(this)
        val repository = GroceryRepository(database)
        
        // Setup ViewModel
        val factory = GroceryViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[GroceryViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GroceryApp(viewModel = viewModel)
            }
        }
    }
}
