package com.example.warehouse.ui.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.warehouse.data.model.ApiSupplier
import com.example.warehouse.data.model.TransitCreateRequest
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShipmentDialog(
    suppliers: List<ApiSupplier>,
    onDismiss: () -> Unit,
    onCreate: (TransitCreateRequest) -> Unit
) {
    var tracking by remember { mutableStateOf("") }
    var carrier  by remember { mutableStateOf("") }
    var origin   by remember { mutableStateOf("") }
    var dest     by remember { mutableStateOf("Склад") }
    var depDate  by remember { mutableStateOf(LocalDate.now().toString()) }
    var expDate  by remember { mutableStateOf(LocalDate.now().plusDays(3).toString()) }
    var notes    by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf<ApiSupplier?>(null) }
    var showSuppliers by remember { mutableStateOf(false) }

    val canSave = tracking.isNotBlank() && carrier.isNotBlank() && origin.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Новая поставка", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = tracking, onValueChange = { tracking = it },
                    label = { Text("Трек-номер") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = carrier, onValueChange = { carrier = it },
                    label = { Text("Перевозчик") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showSuppliers,
                    onExpandedChange = { showSuppliers = it }
                ) {
                    OutlinedTextField(
                        value = supplier?.name ?: "— не выбран —",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Поставщик") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showSuppliers) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showSuppliers,
                        onDismissRequest = { showSuppliers = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("— не выбран —") },
                            onClick = { supplier = null; showSuppliers = false }
                        )
                        suppliers.forEach { sup ->
                            DropdownMenuItem(
                                text = { Text(sup.name) },
                                onClick = { supplier = sup; showSuppliers = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = origin, onValueChange = { origin = it },
                    label = { Text("Откуда") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dest, onValueChange = { dest = it },
                    label = { Text("Куда") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = depDate, onValueChange = { depDate = it },
                        label = { Text("Отпр. (YYYY-MM-DD)") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expDate, onValueChange = { expDate = it },
                        label = { Text("Ожид.") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Примечание") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreate(
                                TransitCreateRequest(
                                    trackingNumber = tracking.trim(),
                                    carrier = carrier.trim(),
                                    supplierId = supplier?.id,
                                    origin = origin.trim(),
                                    destination = dest.trim(),
                                    departureDate = depDate.trim(),
                                    expectedArrival = expDate.trim(),
                                    notes = notes.ifBlank { null }
                                )
                            )
                        },
                        enabled = canSave
                    ) { Text("Создать") }
                }
            }
        }
    }
}
