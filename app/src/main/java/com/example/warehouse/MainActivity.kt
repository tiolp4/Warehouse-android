package com.example.warehouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warehouse.data.model.PickOrder
import com.example.warehouse.ui.manager.ManagerHomeScreen
import com.example.warehouse.ui.scanner.BarcodeScannerScreen
import com.example.warehouse.ui.screens.LoginScreen
import com.example.warehouse.ui.screens.OrdersScreen
import com.example.warehouse.ui.screens.PickSheetScreen
import com.example.warehouse.ui.theme.WarehouseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarehouseTheme {
                WarehouseApp()
            }
        }
    }
}

@Composable
fun WarehouseApp() {
    val navController = rememberNavController()
    var token    by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role     by remember { mutableStateOf("") }
    var selectedOrder by remember { mutableStateOf<PickOrder?>(null) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }

    fun logout() {
        token = ""; fullName = ""; role = ""; selectedOrder = null
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { t, name, r ->
                token = t; fullName = name; role = r
                val home = if (r == "MANAGER") "manager_home" else "orders"
                navController.navigate(home) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("manager_home") {
            ManagerHomeScreen(
                token = token,
                fullName = fullName,
                onLogout = { logout() },
                onPickerOrderClick = { order ->
                    selectedOrder = order
                    navController.navigate("picksheet")
                }
            )
        }
        composable("orders") {
            OrdersScreen(
                token = token,
                fullName = fullName,
                onOrderClick = { order ->
                    selectedOrder = order
                    navController.navigate("picksheet")
                },
                onLogout = { logout() }
            )
        }
        composable("picksheet") {
            selectedOrder?.let { order ->
                PickSheetScreen(
                    token = token,
                    orderId = order.id,
                    orderNumber = order.orderNumber,
                    scannedBarcode = scannedBarcode,
                    onConsumeScan = { scannedBarcode = null },
                    onScanClick = { navController.navigate("scanner") },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("scanner") {
            BarcodeScannerScreen(
                onResult = { code ->
                    scannedBarcode = code
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
