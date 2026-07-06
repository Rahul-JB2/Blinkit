package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.viewmodel.GroceryViewModel

const val ROUTE_HOME = "home"
const val ROUTE_CART = "cart"
const val ROUTE_PAYMENT = "payment"
const val ROUTE_TRACKING = "tracking"
const val ROUTE_ORDERS = "orders"
const val ROUTE_ADMIN = "admin"
const val ROUTE_CHAT = "chat"

@Composable
fun GroceryApp(
    viewModel: GroceryViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
        modifier = modifier.fillMaxSize()
    ) {
        composable(ROUTE_HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCart = { navController.navigate(ROUTE_CART) },
                onNavigateToTracking = { navController.navigate(ROUTE_TRACKING) },
                onNavigateToOrders = { navController.navigate(ROUTE_ORDERS) },
                onNavigateToAdmin = { navController.navigate(ROUTE_ADMIN) },
                onNavigateToChat = { navController.navigate(ROUTE_CHAT) }
            )
        }

        composable(ROUTE_CART) {
            CartScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = { navController.navigate(ROUTE_PAYMENT) }
            )
        }

        composable(ROUTE_PAYMENT) {
            PaymentScreen(
                viewModel = viewModel,
                onNavigateToTracking = {
                    navController.navigate(ROUTE_TRACKING) {
                        popUpTo(ROUTE_HOME) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_TRACKING) {
            TrackingScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    viewModel.clearActiveTracking()
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_ORDERS) {
            OrderHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTracking = {
                    navController.navigate(ROUTE_TRACKING) {
                        popUpTo(ROUTE_HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(ROUTE_ADMIN) {
            AdminDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ROUTE_CHAT) {
            AIChatbotScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
