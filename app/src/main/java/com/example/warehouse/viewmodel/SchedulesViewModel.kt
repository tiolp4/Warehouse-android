package com.example.warehouse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warehouse.data.model.*
import com.example.warehouse.data.repository.ManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class SchedulesViewModel : ViewModel() {
    private val repo = ManagerRepository()

    private val _items = MutableStateFlow<List<ApiSchedule>>(emptyList())
    val items: StateFlow<List<ApiSchedule>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _from = MutableStateFlow(LocalDate.now().minusDays(7))
    val from: StateFlow<LocalDate> = _from

    private val _to = MutableStateFlow(LocalDate.now().plusDays(30))
    val to: StateFlow<LocalDate> = _to

    fun setRange(from: LocalDate, to: LocalDate) {
        _from.value = from
        _to.value = to
    }

    fun load(token: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.listSchedules(token, _from.value.toString(), _to.value.toString()).fold(
                onSuccess = { _items.value = it; _error.value = null },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun create(token: String, req: ScheduleCreateRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.createSchedule(token, req).fold(
                onSuccess = { load(token); onDone() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun changeStatus(token: String, id: String, status: String) {
        viewModelScope.launch {
            repo.updateScheduleStatus(token, id, status).fold(
                onSuccess = { load(token) },
                onFailure = { _error.value = it.message }
            )
        }
    }
}
