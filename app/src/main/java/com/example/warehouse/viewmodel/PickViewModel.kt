package com.example.warehouse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warehouse.data.model.PickOrder
import com.example.warehouse.data.model.PickTask
import com.example.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PickViewModel : ViewModel() {
    private val repo = WarehouseRepository()

    private val _orders  = MutableStateFlow<List<PickOrder>>(emptyList())
    val orders: StateFlow<List<PickOrder>> = _orders

    private val _tasks   = MutableStateFlow<List<PickTask>>(emptyList())
    val tasks: StateFlow<List<PickTask>> = _tasks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error   = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _confirmed = MutableStateFlow<String?>(null)
    val confirmed: StateFlow<String?> = _confirmed

    fun loadOrders(token: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getPickOrders(token).fold(
                onSuccess  = { _orders.value = it; _error.value = null },
                onFailure  = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun loadTasks(token: String, orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.getTasks(token, orderId).fold(
                onSuccess  = { _tasks.value = it; _error.value = null },
                onFailure  = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun confirmTask(token: String, taskId: String, qty: Int) {
        viewModelScope.launch {
            repo.confirmTask(token, taskId, qty).fold(
                onSuccess = { _confirmed.value = taskId },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearConfirmed() { _confirmed.value = null }
}
