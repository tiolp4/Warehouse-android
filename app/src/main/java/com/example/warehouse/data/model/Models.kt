package com.example.warehouse.data.model

// ── API DTOs (mirror warehouse-api responses) ──────────────────────

data class LoginRequest(val username: String, val password: String)

data class ApiUser(
    val id: String,
    val username: String,
    val fullName: String,
    val role: String
)

data class ApiLoginResponse(
    val token: String,
    val expiresAt: Long,
    val user: ApiUser
)

data class ApiInvoice(
    val id: String,
    val invoiceNumber: String,
    val type: String,          // INCOMING | OUTGOING
    val status: String,        // DRAFT | CONFIRMED | CANCELLED
    val supplierId: String?,
    val supplierName: String?,
    val createdBy: String?,
    val createdByName: String?,
    val createdAt: String?,
    val confirmedAt: String?
)

data class ApiInvoiceItem(
    val id: String,
    val invoiceId: String,
    val productId: String,
    val productName: String,
    val sku: String,
    val barcode: String?,
    val cellId: String,
    val cellCode: String,
    val quantity: Int,
    val actualQuantity: Int?
)

data class ActualQtyRequest(val actualQuantity: Int)
data class MessageResponse(val message: String? = null)

// ── Client-side LoginResponse used by ViewModel/screens ────────────

data class LoginResponse(
    val token: String,
    val userId: String,
    val fullName: String,
    val role: String
)

// ── Domain models for picker screens ───────────────────────────────

data class PickOrder(
    val id: String,
    val orderNumber: String,
    val status: String,           // PENDING | IN_PROGRESS | DONE
    val totalTasks: Int,
    val doneTasks: Int,
    val createdAt: String
)

data class PickTask(
    val id: String,
    val productName: String,
    val sku: String,
    val barcode: String,
    val cellCode: String,
    val zone: String,
    val qtyRequired: Int,
    val qtyPicked: Int,
    val status: String            // PENDING | IN_PROGRESS | PICKED
)

data class ConfirmRequest(val qtyPicked: Int)

// ── Logistics: transit shipments ───────────────────────────────────

data class ApiTransitShipment(
    val id: String,
    val trackingNumber: String,
    val carrier: String,
    val supplierId: String?,
    val supplierName: String?,
    val origin: String,
    val destination: String,
    val departureDate: String,        // ISO yyyy-MM-dd
    val expectedArrival: String,
    val actualArrival: String?,
    val status: String,               // PLANNED | IN_TRANSIT | DELIVERED | DELAYED | CANCELLED
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class TransitCreateRequest(
    val trackingNumber: String,
    val carrier: String,
    val supplierId: String?,
    val origin: String,
    val destination: String,
    val departureDate: String,
    val expectedArrival: String,
    val notes: String?
)

data class StatusUpdateRequest(val status: String)
data class MarkArrivedRequest(val actualArrival: String)

// ── Logistics: schedules ───────────────────────────────────────────

data class ApiSchedule(
    val id: String,
    val driverName: String,
    val vehicle: String,
    val route: String,
    val workDate: String,
    val shiftStart: String,           // HH:mm:ss
    val shiftEnd: String,
    val status: String,               // SCHEDULED | IN_PROGRESS | COMPLETED | CANCELLED
    val shipmentId: String?,
    val shipmentTracking: String?,
    val notes: String?
)

data class ScheduleCreateRequest(
    val driverName: String,
    val vehicle: String,
    val route: String,
    val workDate: String,
    val shiftStart: String,
    val shiftEnd: String,
    val shipmentId: String?,
    val notes: String?
)

// ── Catalog (Suppliers) ────────────────────────────────────────────

data class ApiSupplier(
    val id: String,
    val name: String,
    val contact: String?,
    val phone: String?,
    val email: String?
)

// ── Analytics ──────────────────────────────────────────────────────

data class ApiKpi(
    val shipmentsTotal: Long,
    val shipmentsInTransit: Long,
    val shipmentsDelivered: Long,
    val shipmentsDelayed: Long,
    val schedulesTotal: Long,
    val schedulesToday: Long,
    val schedulesCompleted: Long,
    val schedulesActive: Long,
    val invoicesTotal: Long,
    val invoicesConfirmed: Long,
    val qtyIncoming: Long,
    val qtyOutgoing: Long
)

data class ApiDailyFlow(val date: String, val incoming: Long, val outgoing: Long)
data class ApiCountEntry(val key: String, val count: Long)
