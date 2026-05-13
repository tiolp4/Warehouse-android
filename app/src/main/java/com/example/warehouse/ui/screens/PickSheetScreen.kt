package com.example.warehouse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.warehouse.data.model.PickTask
import com.example.warehouse.viewmodel.PickViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickSheetScreen(
    token: String,
    orderId: String,
    orderNumber: String,
    scannedBarcode: String?,
    onConsumeScan: () -> Unit,
    onScanClick: () -> Unit,
    onBack: () -> Unit,
    vm: PickViewModel = viewModel()
) {
    val tasks     by vm.tasks.collectAsState()
    val loading   by vm.loading.collectAsState()
    val error     by vm.error.collectAsState()
    val confirmed by vm.confirmed.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var openTaskId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadTasks(token, orderId) }

    LaunchedEffect(confirmed) {
        if (confirmed != null) {
            vm.loadTasks(token, orderId)
            vm.clearConfirmed()
            scope.launch { snackbar.showSnackbar("Позиция отмечена") }
        }
    }

    // Auto-jump to task whose barcode matches the scanned value
    LaunchedEffect(scannedBarcode, tasks) {
        val bc = scannedBarcode ?: return@LaunchedEffect
        if (tasks.isEmpty()) return@LaunchedEffect
        val idx = tasks.indexOfFirst { it.barcode.isNotBlank() && it.barcode == bc }
        if (idx >= 0) {
            listState.animateScrollToItem(idx)
            openTaskId = tasks[idx].id
        } else {
            snackbar.showSnackbar("Штрих-код не найден в задании: $bc")
        }
        onConsumeScan()
    }

    val done = tasks.count { it.status == "PICKED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(orderNumber, fontWeight = FontWeight.SemiBold)
                        if (tasks.isNotEmpty()) {
                            Text(
                                "$done из ${tasks.size} собрано",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScanClick,
                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                text = { Text("Сканер") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading && tasks.isEmpty() -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center)
                )
                error != null -> Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                tasks.isEmpty() -> Text(
                    "Нет позиций в задании",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            highlighted = task.id == openTaskId,
                            openConfirmDialog = task.id == openTaskId,
                            onDismissDialog = { openTaskId = null },
                            onOpen = { openTaskId = task.id },
                            onConfirm = { qty ->
                                openTaskId = null
                                vm.confirmTask(token, task.id, qty)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: PickTask,
    highlighted: Boolean,
    openConfirmDialog: Boolean,
    onDismissDialog: () -> Unit,
    onOpen: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val isPicked = task.status == "PICKED"
    val container = when {
        isPicked     -> MaterialTheme.colorScheme.secondaryContainer
        highlighted  -> MaterialTheme.colorScheme.primaryContainer
        else         -> MaterialTheme.colorScheme.surface
    }
    val onContainer = when {
        isPicked     -> MaterialTheme.colorScheme.onSecondaryContainer
        highlighted  -> MaterialTheme.colorScheme.onPrimaryContainer
        else         -> MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = container, contentColor = onContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(
                            if (isPicked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPicked) Icons.Default.CheckCircle
                                      else Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (isPicked) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(task.productName, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("SKU: ${task.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill("Ячейка", task.cellCode)
                if (task.zone.isNotBlank()) InfoPill("Зона", task.zone)
                if (task.barcode.isNotBlank()) InfoPill("Код", task.barcode)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pickedText = if (task.qtyPicked > 0) " (взято: ${task.qtyPicked})" else ""
                Text("Взять: ${task.qtyRequired}$pickedText",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
                if (!isPicked) {
                    FilledTonalButton(onClick = onOpen) { Text("Отметить") }
                }
            }
        }
    }

    if (openConfirmDialog) {
        var qtyInput by remember { mutableStateOf(task.qtyRequired.toString()) }
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Подтвердить взятие") },
            text = {
                Column {
                    Text(task.productName, fontWeight = FontWeight.SemiBold)
                    Text("Ячейка: ${task.cellCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it.filter(Char::isDigit) },
                        label = { Text("Фактическое кол-во") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(qtyInput.toIntOrNull() ?: task.qtyRequired)
                }) { Text("Подтвердить") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
