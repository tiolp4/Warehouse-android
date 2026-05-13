package com.example.warehouse.data.repository

import com.example.warehouse.data.api.RetrofitClient
import com.example.warehouse.data.model.*

/**
 * Logistics + analytics for the MANAGER section.
 * All methods take an unbearer-ised JWT token; the repo adds "Bearer ".
 */
class ManagerRepository {
    private val api = RetrofitClient.api

    private fun bearer(token: String) = "Bearer $token"

    // ── Shipments ─────────────────────────────────────────────

    suspend fun listShipments(token: String): Result<List<ApiTransitShipment>> = runCatching {
        val r = api.getShipments(bearer(token))
        r.body() ?: error(errorMessage(r))
    }

    suspend fun createShipment(token: String, req: TransitCreateRequest): Result<ApiTransitShipment> =
        runCatching {
            val r = api.createShipment(bearer(token), req)
            r.body() ?: error(errorMessage(r))
        }

    suspend fun updateShipmentStatus(token: String, id: String, status: String): Result<Unit> =
        runCatching {
            val r = api.updateShipmentStatus(bearer(token), id, StatusUpdateRequest(status))
            if (!r.isSuccessful) error(errorMessage(r))
        }

    suspend fun markShipmentArrived(token: String, id: String, date: String): Result<Unit> =
        runCatching {
            val r = api.markShipmentArrived(bearer(token), id, MarkArrivedRequest(date))
            if (!r.isSuccessful) error(errorMessage(r))
        }

    // ── Schedules ─────────────────────────────────────────────

    suspend fun listSchedules(
        token: String, from: String? = null, to: String? = null
    ): Result<List<ApiSchedule>> = runCatching {
        val r = api.getSchedules(bearer(token), from, to)
        r.body() ?: error(errorMessage(r))
    }

    suspend fun createSchedule(token: String, req: ScheduleCreateRequest): Result<ApiSchedule> =
        runCatching {
            val r = api.createSchedule(bearer(token), req)
            r.body() ?: error(errorMessage(r))
        }

    suspend fun updateScheduleStatus(token: String, id: String, status: String): Result<Unit> =
        runCatching {
            val r = api.updateScheduleStatus(bearer(token), id, StatusUpdateRequest(status))
            if (!r.isSuccessful) error(errorMessage(r))
        }

    // ── Catalog ───────────────────────────────────────────────

    suspend fun listSuppliers(token: String): Result<List<ApiSupplier>> = runCatching {
        val r = api.getSuppliers(bearer(token))
        r.body() ?: error(errorMessage(r))
    }

    // ── Analytics ─────────────────────────────────────────────

    suspend fun kpi(token: String): Result<ApiKpi> = runCatching {
        val r = api.getKpi(bearer(token))
        r.body() ?: error(errorMessage(r))
    }

    suspend fun shipmentFlow(token: String, days: Int = 14): Result<List<ApiDailyFlow>> = runCatching {
        val r = api.getShipmentFlow(bearer(token), days)
        r.body() ?: error(errorMessage(r))
    }

    suspend fun transitStatus(token: String): Result<List<ApiCountEntry>> = runCatching {
        val r = api.getTransitStatus(bearer(token))
        r.body() ?: error(errorMessage(r))
    }

    suspend fun scheduleStatus(token: String): Result<List<ApiCountEntry>> = runCatching {
        val r = api.getScheduleStatus(bearer(token))
        r.body() ?: error(errorMessage(r))
    }

    suspend fun topCarriers(token: String, limit: Int = 5): Result<List<ApiCountEntry>> = runCatching {
        val r = api.getTopCarriers(bearer(token), limit)
        r.body() ?: error(errorMessage(r))
    }

    suspend fun topDrivers(token: String, limit: Int = 5): Result<List<ApiCountEntry>> = runCatching {
        val r = api.getTopDrivers(bearer(token), limit)
        r.body() ?: error(errorMessage(r))
    }

    private fun errorMessage(resp: retrofit2.Response<*>): String {
        if (resp.code() == 401) return "Сессия истекла, войдите снова"
        if (resp.code() == 403) return "Нет доступа"
        val raw = resp.errorBody()?.string()
        return if (!raw.isNullOrBlank()) "HTTP ${resp.code()}: $raw"
        else "HTTP ${resp.code()}"
    }
}
