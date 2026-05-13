package com.example.warehouse.ui.manager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.warehouse.data.model.*
import com.example.warehouse.viewmodel.ShipmentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    token: String,
    vm: ShipmentsViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val suppliers by vm.suppliers.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ApiTransitShipment?>(null) }

    LaunchedEffect(Unit) {
        vm.load(token)
        vm.loadSuppliers(token)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Поставки в пути", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { vm.load(token) }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Создать") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading && items.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null && items.isEmpty() -> Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                items.isEmpty() -> Text(
                    "Нет активных поставок",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { it ->
                        ShipmentCard(it) { selected = it }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateShipmentDialog(
            suppliers = suppliers,
            onDismiss = { showCreate = false },
            onCreate = { req ->
                vm.create(token, req) { showCreate = false }
            }
        )
    }

    selected?.let { sh ->
        ShipmentActionsSheet(
            shipment = sh,
            onDismiss = { selected = null },
            onSetStatus = { st -> vm.changeStatus(token, sh.id, st); selected = null },
            onMarkArrived = { vm.markArrived(token, sh.id); selected = null }
        )
    }
}

@Composable
private fun ShipmentCard(s: ApiTransitShipment, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(s.trackingNumber, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(s.carrier, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ShipmentStatusChip(s.status)
            }
            Spacer(Modifier.height(8.dp))
            Text("${s.origin} → ${s.destination}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Отпр.: ${s.departureDate} · ожид.: ${s.expectedArrival}" +
                    (s.actualArrival?.let { " · прибыло: $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!s.supplierName.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Поставщик: ${s.supplierName}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ShipmentStatusChip(status: String) {
    val (bg, fg, label) = when (status) {
        "DELIVERED"  -> Triple(MaterialTheme.colorScheme.secondaryContainer,
                               MaterialTheme.colorScheme.onSecondaryContainer, "Доставлено")
        "IN_TRANSIT" -> Triple(MaterialTheme.colorScheme.primaryContainer,
                               MaterialTheme.colorScheme.onPrimaryContainer, "В пути")
        "DELAYED"    -> Triple(MaterialTheme.colorScheme.tertiaryContainer,
                               MaterialTheme.colorScheme.onTertiary, "Задержка")
        "CANCELLED"  -> Triple(MaterialTheme.colorScheme.surfaceVariant,
                               MaterialTheme.colorScheme.onSurfaceVariant, "Отменено")
        else         -> Triple(MaterialTheme.colorScheme.surfaceVariant,
                               MaterialTheme.colorScheme.onSurfaceVariant, "Запланировано")
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(label, color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShipmentActionsSheet(
    shipment: ApiTransitShipment,
    onDismiss: () -> Unit,
    onSetStatus: (String) -> Unit,
    onMarkArrived: () -> Unit
) {
    val sheet = rememberModalBottomSheetState()
    val done = shipment.status == "DELIVERED" || shipment.status == "CANCELLED"
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(shipment.trackingNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(shipment.carrier, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            if (!done) {
                FilledTonalButton(
                    onClick = { onSetStatus("IN_TRANSIT") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocalShipping, null); Spacer(Modifier.width(8.dp))
                    Text("Отметить «В пути»")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onMarkArrived,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(8.dp))
                    Text("Доставлено")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onSetStatus("CANCELLED") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Close, null); Spacer(Modifier.width(8.dp))
                    Text("Отменить")
                }
            } else {
                Text("Поставка уже закрыта",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
