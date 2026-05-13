package com.example.warehouse.data.repository

import com.example.warehouse.data.api.RetrofitClient
import com.example.warehouse.data.model.*

/**
 * Maps warehouse-api DTOs to the picker-oriented domain model
 * used by the screens (PickOrder / PickTask).
 *
 * "Shipments to pick" == outgoing invoices that aren't cancelled.
 */
class WarehouseRepository {
    private val api = RetrofitClient.api

    suspend fun login(username: String, password: String): Result<LoginResponse> =
        runCatching {
            val resp = api.login(LoginRequest(username, password))
            val body = resp.body() ?: error(errorMessage(resp))
            LoginResponse(
                token = body.token,
                userId = body.user.id,
                fullName = body.user.fullName,
                role = body.user.role
            )
        }

    /** Outgoing invoices, transformed to picker orders. */
    suspend fun getPickOrders(token: String): Result<List<PickOrder>> = runCatching {
        val bearer = "Bearer $token"
        val invoicesResp = api.getInvoices(bearer)
        val invoices = invoicesResp.body() ?: error(errorMessage(invoicesResp))

        invoices
            .filter { it.type == "OUTGOING" && it.status != "CANCELLED" }
            .map { inv ->
                val items = api.getInvoiceItems(bearer, inv.id).body().orEmpty()
                val done = items.count { (it.actualQuantity ?: 0) >= it.quantity }
                PickOrder(
                    id = inv.id,
                    orderNumber = inv.invoiceNumber,
                    status = when {
                        inv.status == "CONFIRMED" -> "DONE"
                        done == items.size && items.isNotEmpty() -> "IN_PROGRESS"
                        done > 0 -> "IN_PROGRESS"
                        else -> "PENDING"
                    },
                    totalTasks = items.size,
                    doneTasks = done,
                    createdAt = inv.createdAt?.substringBefore('T').orEmpty()
                )
            }
    }

    suspend fun getTasks(token: String, orderId: String): Result<List<PickTask>> = runCatching {
        val bearer = "Bearer $token"
        val resp = api.getInvoiceItems(bearer, orderId)
        val items = resp.body() ?: error(errorMessage(resp))
        items.map { it.toTask() }
    }

    suspend fun confirmTask(token: String, taskId: String, qty: Int): Result<MessageResponse> =
        runCatching {
            val bearer = "Bearer $token"
            val resp = api.setActualQuantity(bearer, taskId, ActualQtyRequest(qty))
            resp.body() ?: error(errorMessage(resp))
            MessageResponse("OK")
        }

    suspend fun completeOrder(token: String, orderId: String): Result<MessageResponse> =
        runCatching {
            val bearer = "Bearer $token"
            val resp = api.confirmInvoice(bearer, orderId)
            resp.body() ?: error(errorMessage(resp))
            MessageResponse("OK")
        }

    private fun ApiInvoiceItem.toTask(): PickTask {
        val picked = actualQuantity ?: 0
        return PickTask(
            id = id,
            productName = productName,
            sku = sku,
            barcode = barcode.orEmpty(),
            cellCode = cellCode,
            zone = cellCode.substringBefore('-', ""),
            qtyRequired = quantity,
            qtyPicked = picked,
            status = when {
                picked >= quantity && quantity > 0 -> "PICKED"
                picked > 0 -> "IN_PROGRESS"
                else -> "PENDING"
            }
        )
    }

    private fun errorMessage(resp: retrofit2.Response<*>): String {
        if (resp.code() == 401) return "Неверный логин или пароль"
        val raw = resp.errorBody()?.string()
        return if (!raw.isNullOrBlank()) "HTTP ${resp.code()}: $raw"
        else "HTTP ${resp.code()}"
    }
}
