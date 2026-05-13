package com.example.warehouse.ui.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.warehouse.viewmodel.AnalyticsViewModel

private val TRANSIT_LABELS = mapOf(
    "PLANNED" to "Запланировано",
    "IN_TRANSIT" to "В пути",
    "DELIVERED" to "Доставлено",
    "DELAYED" to "Задержка",
    "CANCELLED" to "Отменено"
)

private val SCHEDULE_LABELS = mapOf(
    "SCHEDULED" to "Запланировано",
    "IN_PROGRESS" to "В работе",
    "COMPLETED" to "Завершено",
    "CANCELLED" to "Отменено"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    token: String,
    vm: AnalyticsViewModel = viewModel()
) {
    val data by vm.data.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val days by vm.days.collectAsState()

    LaunchedEffect(Unit) { vm.load(token) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Аналитика", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { vm.load(token) }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (loading && data.kpi == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }

                // KPI row 1
                val kpi = data.kpi
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiTile(
                        title = "ВСЕГО ПОСТАВОК",
                        value = (kpi?.shipmentsTotal ?: 0).toString(),
                        sub = "доставлено: ${kpi?.shipmentsDelivered ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        title = "В ПУТИ",
                        value = (kpi?.shipmentsInTransit ?: 0).toString(),
                        sub = "задержано: ${kpi?.shipmentsDelayed ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiTile(
                        title = "СМЕН ВСЕГО",
                        value = (kpi?.schedulesTotal ?: 0).toString(),
                        sub = "сегодня: ${kpi?.schedulesToday ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        title = "АКТИВНЫХ СМЕН",
                        value = (kpi?.schedulesActive ?: 0).toString(),
                        sub = "завершено: ${kpi?.schedulesCompleted ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Period selector
                ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Движение поставок", fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f))
                            AssistChip(
                                onClick = { vm.setDays(7); vm.load(token) },
                                label = { Text("7д") },
                                colors = chipColors(days == 7)
                            )
                            Spacer(Modifier.width(4.dp))
                            AssistChip(
                                onClick = { vm.setDays(14); vm.load(token) },
                                label = { Text("14д") },
                                colors = chipColors(days == 14)
                            )
                            Spacer(Modifier.width(4.dp))
                            AssistChip(
                                onClick = { vm.setDays(30); vm.load(token) },
                                label = { Text("30д") },
                                colors = chipColors(days == 30)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        DailyFlowChart(
                            data = data.shipmentFlow,
                            seriesA = "Отправлено",
                            seriesB = "Прибыло"
                        )
                    }
                }

                AnalyticsSection(title = "Поставки по статусам") {
                    HorizontalCountBars(
                        entries = data.transitByStatus,
                        labelMapper = { TRANSIT_LABELS[it] ?: it }
                    )
                }

                AnalyticsSection(title = "Смены по статусам") {
                    HorizontalCountBars(
                        entries = data.schedulesByStatus,
                        labelMapper = { SCHEDULE_LABELS[it] ?: it }
                    )
                }

                AnalyticsSection(title = "Топ перевозчиков") {
                    HorizontalCountBars(entries = data.topCarriers)
                }

                AnalyticsSection(title = "Топ водителей") {
                    HorizontalCountBars(entries = data.topDrivers)
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun chipColors(active: Boolean) = AssistChipDefaults.assistChipColors(
    containerColor = if (active)
        MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface,
    labelColor = if (active)
        MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
)

@Composable
private fun AnalyticsSection(title: String, content: @Composable () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
