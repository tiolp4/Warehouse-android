package com.example.warehouse.ui.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.warehouse.data.model.ScheduleCreateRequest
import java.time.LocalDate

@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreate: (ScheduleCreateRequest) -> Unit
) {
    var driver  by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var route   by remember { mutableStateOf("") }
    var date    by remember { mutableStateOf(LocalDate.now().toString()) }
    var start   by remember { mutableStateOf("08:00") }
    var end     by remember { mutableStateOf("17:00") }
    var notes   by remember { mutableStateOf("") }

    val canSave = driver.isNotBlank() && vehicle.isNotBlank() && route.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Новая смена", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(driver,  { driver = it },
                    label = { Text("Водитель") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(vehicle, { vehicle = it },
                    label = { Text("Транспорт") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(route,   { route = it },
                    label = { Text("Маршрут") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(date, { date = it },
                    label = { Text("Дата (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(start, { start = it },
                        label = { Text("Начало") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(end,   { end = it },
                        label = { Text("Конец") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it },
                    label = { Text("Примечание") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = canSave,
                        onClick = {
                            onCreate(
                                ScheduleCreateRequest(
                                    driverName = driver.trim(),
                                    vehicle    = vehicle.trim(),
                                    route      = route.trim(),
                                    workDate   = date.trim(),
                                    shiftStart = start.trim(),
                                    shiftEnd   = end.trim(),
                                    shipmentId = null,
                                    notes      = notes.ifBlank { null }
                                )
                            )
                        }
                    ) { Text("Создать") }
                }
            }
        }
    }
}
