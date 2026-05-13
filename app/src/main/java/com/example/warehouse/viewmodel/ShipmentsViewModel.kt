package com.example.warehouse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.warehouse.data.model.*
import com.example.warehouse.data.repository.ManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ShipmentsViewModel : ViewModel() {
    private val repo = ManagerRepository()

    private val _items   = MutableStateFlow<List<ApiTransitShipment>>(emptyList())
    val items: StateFlow<List<ApiTransitShipment>> = _items

    private val _suppliers = MutableStateFlow<List<ApiSupplier>>(emptyList())
    val suppliers: StateFlow<List<ApiSupplier>> = _suppliers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load(token: String) {
        viewModelScope.launch {
            _loading.value = true
            repo.listShipments(token).fold(
                onSuccess = { _items.value = it; _error.value = null },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun loadSuppliers(token: String) {
        viewModelScope.launch {
            repo.listSuppliers(token).fold(
                onSuccess = { _suppliers.value = it },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun create(token: String, req: TransitCreateRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.createShipment(token, req).fold(
                onSuccess = { load(token); onDone() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun changeStatus(token: String, id: String, status: String) {
        viewModelScope.launch {
            repo.updateShipmentStatus(token, id, status).fold(
                onSuccess = { load(token) },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun markArrived(token: String, id: String) {
        viewModelScope.launch {
            repo.markShipmentArrived(token, id, LocalDate.now().toString()).fold(
                onSuccess = { load(token) },
                onFailure = { _error.value = it.message }
            )
        }
    }
}
