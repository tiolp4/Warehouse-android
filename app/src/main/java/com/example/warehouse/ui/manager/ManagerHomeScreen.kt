package com.example.warehouse.ui.manager

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.warehouse.ui.screens.OrdersScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerHomeScreen(
    token: String,
    fullName: String,
    onLogout: () -> Unit,
    onPickerOrderClick: (com.example.warehouse.data.model.PickOrder) -> Unit
) {
    var tab by remember { mutableStateOf(ManagerTab.SHIPMENTS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ManagerTab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.title) },
                        label = { Text(t.title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                ManagerTab.SHIPMENTS -> ShipmentsScreen(token = token)
                ManagerTab.SCHEDULES -> SchedulesScreen(token = token)
                ManagerTab.ANALYTICS -> AnalyticsScreen(token = token)
                ManagerTab.PICKER    -> OrdersScreen(
                    token = token,
                    fullName = fullName,
                    onOrderClick = onPickerOrderClick,
                    onLogout = onLogout
                )
            }
        }
    }
}

enum class ManagerTab(val title: String, val icon: ImageVector) {
    SHIPMENTS("Поставки",  Icons.Default.LocalShipping),
    SCHEDULES("Графики",   Icons.Default.CalendarMonth),
    ANALYTICS("Аналитика", Icons.Default.Analytics),
    PICKER   ("Сборка",    Icons.AutoMirrored.Filled.ListAlt)
}
