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
import com.example.warehouse.data.model.ApiSchedule
import com.example.warehouse.viewmodel.SchedulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    token: String,
    vm: SchedulesViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val from by vm.from.collectAsState()
    val to by vm.to.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ApiSchedule?>(null) }

    LaunchedEffect(Unit) { vm.load(token) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Графики работы", fontWeight = FontWeight.SemiBold) },
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
        Column(Modifier.padding(padding).fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Период: $from — $to",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loading && items.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    error != null && items.isEmpty() -> Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    items.isEmpty() -> Text("Нет назначенных смен",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items, key = { it.id }) { s ->
                            ScheduleCard(s) { selected = s }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateScheduleDialog(
            onDismiss = { showCreate = false },
            onCreate = { req -> vm.create(token, req) { showCreate = false } }
        )
    }

    selected?.let { s ->
        ScheduleActionsSheet(
            schedule = s,
            onDismiss = { selected = null },
            onSetStatus = { st -> vm.changeStatus(token, s.id, st); selected = null }
        )
    }
}

@Composable
private fun ScheduleCard(s: ApiSchedule, onClick: () -> Unit) {
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
                    Text(s.driverName, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("${s.vehicle} · ${s.route}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ScheduleStatusChip(s.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${s.workDate} · ${s.shiftStart.take(5)}–${s.shiftEnd.take(5)}",
                style = MaterialTheme.typography.bodyMedium
            )
            s.shipmentTracking?.let {
                Spacer(Modifier.height(4.dp))
                Text("Поставка: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ScheduleStatusChip(status: String) {
    val (bg, fg, label) = when (status) {
        "COMPLETED"  -> Triple(MaterialTheme.colorScheme.secondaryContainer,
                               MaterialTheme.colorScheme.onSecondaryContainer, "Завершено")
        "IN_PROGRESS"-> Triple(MaterialTheme.colorScheme.primaryContainer,
                               MaterialTheme.colorScheme.onPrimaryContainer, "В работе")
        "CANCELLED"  -> Triple(MaterialTheme.colorScheme.surfaceVariant,
                               MaterialTheme.colorScheme.onSurfaceVariant, "Отменено")
        else         -> Triple(MaterialTheme.colorScheme.tertiaryContainer,
                               MaterialTheme.colorScheme.onTertiary, "Запланировано")
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
private fun ScheduleActionsSheet(
    schedule: ApiSchedule,
    onDismiss: () -> Unit,
    onSetStatus: (String) -> Unit
) {
    val sheet = rememberModalBottomSheetState()
    val done = schedule.status == "COMPLETED" || schedule.status == "CANCELLED"
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(schedule.driverName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text(schedule.route, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            if (!done) {
                FilledTonalButton(
                    onClick = { onSetStatus("IN_PROGRESS") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                    Text("Запустить смену")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onSetStatus("COMPLETED") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(8.dp))
                    Text("Завершить")
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
                Text("Смена уже закрыта",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
