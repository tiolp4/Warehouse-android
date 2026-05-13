package com.example.warehouse.data.api

import com.example.warehouse.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiLoginResponse>

    // ── Picker (invoices) ─────────────────────────────────────
    @GET("api/v1/invoices")
    suspend fun getInvoices(
        @Header("Authorization") token: String
    ): Response<List<ApiInvoice>>

    @GET("api/v1/invoices/{id}/items")
    suspend fun getInvoiceItems(
        @Header("Authorization") token: String,
        @Path("id") invoiceId: String
    ): Response<List<ApiInvoiceItem>>

    @PATCH("api/v1/invoices/items/{itemId}/actual")
    suspend fun setActualQuantity(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Body request: ActualQtyRequest
    ): Response<ApiInvoiceItem>

    @POST("api/v1/invoices/{id}/confirm")
    suspend fun confirmInvoice(
        @Header("Authorization") token: String,
        @Path("id") invoiceId: String
    ): Response<ApiInvoice>

    // ── Logistics: transit shipments ──────────────────────────
    @GET("api/v1/shipments")
    suspend fun getShipments(
        @Header("Authorization") token: String
    ): Response<List<ApiTransitShipment>>

    @POST("api/v1/shipments")
    suspend fun createShipment(
        @Header("Authorization") token: String,
        @Body req: TransitCreateRequest
    ): Response<ApiTransitShipment>

    @PATCH("api/v1/shipments/{id}/status")
    suspend fun updateShipmentStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body req: StatusUpdateRequest
    ): Response<ApiTransitShipment>

    @PATCH("api/v1/shipments/{id}/arrived")
    suspend fun markShipmentArrived(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body req: MarkArrivedRequest
    ): Response<ApiTransitShipment>

    @DELETE("api/v1/shipments/{id}")
    suspend fun deleteShipment(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    // ── Logistics: schedules ──────────────────────────────────
    @GET("api/v1/schedules")
    suspend fun getSchedules(
        @Header("Authorization") token: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<ApiSchedule>>

    @POST("api/v1/schedules")
    suspend fun createSchedule(
        @Header("Authorization") token: String,
        @Body req: ScheduleCreateRequest
    ): Response<ApiSchedule>

    @PATCH("api/v1/schedules/{id}/status")
    suspend fun updateScheduleStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body req: StatusUpdateRequest
    ): Response<ApiSchedule>

    @DELETE("api/v1/schedules/{id}")
    suspend fun deleteSchedule(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    // ── Catalog ───────────────────────────────────────────────
    @GET("api/v1/suppliers")
    suspend fun getSuppliers(
        @Header("Authorization") token: String
    ): Response<List<ApiSupplier>>

    // ── Analytics ─────────────────────────────────────────────
    @GET("api/v1/analytics/kpi")
    suspend fun getKpi(@Header("Authorization") token: String): Response<ApiKpi>

    @GET("api/v1/analytics/shipment-flow")
    suspend fun getShipmentFlow(
        @Header("Authorization") token: String,
        @Query("days") days: Int = 14
    ): Response<List<ApiDailyFlow>>

    @GET("api/v1/analytics/transit-status")
    suspend fun getTransitStatus(
        @Header("Authorization") token: String
    ): Response<List<ApiCountEntry>>

    @GET("api/v1/analytics/schedule-status")
    suspend fun getScheduleStatus(
        @Header("Authorization") token: String
    ): Response<List<ApiCountEntry>>

    @GET("api/v1/analytics/top-carriers")
    suspend fun getTopCarriers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 5
    ): Response<List<ApiCountEntry>>

    @GET("api/v1/analytics/top-drivers")
    suspend fun getTopDrivers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 5
    ): Response<List<ApiCountEntry>>
}
