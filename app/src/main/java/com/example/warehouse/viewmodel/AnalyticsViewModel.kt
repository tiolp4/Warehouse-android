package com.example.warehouse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warehouse.data.model.*
import com.example.warehouse.data.repository.ManagerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnalyticsBundle(
    val kpi: ApiKpi? = null,
    val shipmentFlow: List<ApiDailyFlow> = emptyList(),
    val transitByStatus: List<ApiCountEntry> = emptyList(),
    val schedulesByStatus: List<ApiCountEntry> = emptyList(),
    val topCarriers: List<ApiCountEntry> = emptyList(),
    val topDrivers: List<ApiCountEntry> = emptyList()
)

class AnalyticsViewModel : ViewModel() {
    private val repo = ManagerRepository()

    private val _data = MutableStateFlow(AnalyticsBundle())
    val data: StateFlow<AnalyticsBundle> = _data

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _days = MutableStateFlow(14)
    val days: StateFlow<Int> = _days

    fun setDays(d: Int) { _days.value = d.coerceIn(7, 90) }

    fun load(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val kpi      = async { repo.kpi(token) }
            val flow     = async { repo.shipmentFlow(token, _days.value) }
            val transit  = async { repo.transitStatus(token) }
            val sched    = async { repo.scheduleStatus(token) }
            val carriers = async { repo.topCarriers(token, 5) }
            val drivers  = async { repo.topDrivers(token, 5) }

            val kpiRes      = kpi.await()
            val flowRes     = flow.await()
            val transitRes  = transit.await()
            val schedRes    = sched.await()
            val carriersRes = carriers.await()
            val driversRes  = drivers.await()

            _data.value = AnalyticsBundle(
                kpi = kpiRes.getOrNull(),
                shipmentFlow = flowRes.getOrNull().orEmpty(),
                transitByStatus = transitRes.getOrNull().orEmpty(),
                schedulesByStatus = schedRes.getOrNull().orEmpty(),
                topCarriers = carriersRes.getOrNull().orEmpty(),
                topDrivers = driversRes.getOrNull().orEmpty()
            )
            listOf(kpiRes, flowRes, transitRes, schedRes, carriersRes, driversRes)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()?.let { _error.value = it.message }

            _loading.value = false
        }
    }
}
