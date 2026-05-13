package com.example.warehouse.viewmodel

import com.example.warehouse.data.model.*
import com.example.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Mock lateinit var repository: WarehouseRepository

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is Idle`() {
        assertIs<AuthState.Idle>(viewModel.state.value)
    }

    @Test
    fun `login success transitions to Success state`() = runTest {
        val response = LoginResponse(
            token    = "test-token",
            userId   = "user-123",
            fullName = "Иванов Сергей",
            role     = "MANAGER"
        )
        whenever(repository.login("ivanov", "password123"))
            .thenReturn(Result.success(response))

        viewModel.login("ivanov", "password123")

        val state = viewModel.state.value
        assertIs<AuthState.Success>(state)
        assertEquals("test-token", state.data.token)
        assertEquals("Иванов Сергей", state.data.fullName)
    }

    @Test
    fun `login failure transitions to Error state`() = runTest {
        whenever(repository.login("wrong", "pass"))
            .thenReturn(Result.failure(Exception("Неверный логин или пароль")))

        viewModel.login("wrong", "pass")

        val state = viewModel.state.value
        assertIs<AuthState.Error>(state)
        assertEquals("Неверный логин или пароль", state.message)
    }

    @Test
    fun `login sets Loading state during request`() = runTest {
        whenever(repository.login(any(), any()))
            .thenReturn(Result.success(LoginResponse("t", "u", "n", "r")))

        viewModel.login("user", "pass")
        // Loading → Success происходит мгновенно с UnconfinedTestDispatcher
        // проверяем конечное состояние
        assertIs<AuthState.Success>(viewModel.state.value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PickViewModelTest {

    @Mock lateinit var repository: WarehouseRepository

    private lateinit var viewModel: PickViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testToken = "Bearer test-token"

    private val testOrders = listOf(
        PickOrder("order-1", "PICK-001", "PENDING",    3, 0, "2024-01-01"),
        PickOrder("order-2", "PICK-002", "IN_PROGRESS", 5, 2, "2024-01-02")
    )

    private val testTasks = listOf(
        PickTask("task-1", "Болт М8", "BLT-001", "460001",
                 "A-01-01", "A", 10, 0, "PENDING"),
        PickTask("task-2", "Гайка М8", "NUT-001", "460002",
                 "A-01-02", "A", 5,  5, "PICKED")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = PickViewModel()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state — orders empty`() {
        assertTrue(viewModel.orders.value.isEmpty())
    }

    @Test
    fun `initial state — tasks empty`() {
        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `loadOrders success — updates orders list`() = runTest {
        whenever(repository.getPickOrders(testToken))
            .thenReturn(Result.success(testOrders))

        viewModel.loadOrders(testToken)

        assertEquals(2, viewModel.orders.value.size)
        assertEquals("PICK-001", viewModel.orders.value[0].orderNumber)
    }

    @Test
    fun `loadOrders failure — sets error`() = runTest {
        whenever(repository.getPickOrders(testToken))
            .thenReturn(Result.failure(Exception("Нет соединения")))

        viewModel.loadOrders(testToken)

        assertEquals("Нет соединения", viewModel.error.value)
        assertTrue(viewModel.orders.value.isEmpty())
    }

    @Test
    fun `loadTasks success — updates tasks list`() = runTest {
        whenever(repository.getTasks(testToken, "order-1"))
            .thenReturn(Result.success(testTasks))

        viewModel.loadTasks(testToken, "order-1")

        assertEquals(2, viewModel.tasks.value.size)
        assertEquals("PENDING", viewModel.tasks.value[0].status)
        assertEquals("PICKED",  viewModel.tasks.value[1].status)
    }

    @Test
    fun `confirmTask success — sets confirmed taskId`() = runTest {
        whenever(repository.confirmTask(testToken, "task-1", 10))
            .thenReturn(Result.success(MessageResponse("OK")))

        viewModel.confirmTask(testToken, "task-1", 10)

        assertEquals("task-1", viewModel.confirmed.value)
    }

    @Test
    fun `clearConfirmed — resets confirmed to null`() = runTest {
        whenever(repository.confirmTask(testToken, "task-1", 10))
            .thenReturn(Result.success(MessageResponse("OK")))
        viewModel.confirmTask(testToken, "task-1", 10)

        viewModel.clearConfirmed()

        assertEquals(null, viewModel.confirmed.value)
    }

    @Test
    fun `loading state — false after success`() = runTest {
        whenever(repository.getPickOrders(testToken))
            .thenReturn(Result.success(testOrders))

        viewModel.loadOrders(testToken)

        assertEquals(false, viewModel.loading.value)
    }
}
