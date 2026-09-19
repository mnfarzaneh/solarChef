//package com.mnfarzaneh.solalrchef.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.SavedStateHandle
//import app.cash.turbine.test
//import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
//import com.mnfarzaneh.solalrchef.data.remote.ParseRecipeRequest
//import com.mnfarzaneh.solalrchef.data.remote.ParsedRecipeResponse
//import com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
//import com.mnfarzaneh.solalrchef.util.NetworkMonitor
//import io.mockk.coEvery
//import io.mockk.mockk
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import java.net.SocketTimeoutException
//import java.net.UnknownHostException
//
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNull
//import org.junit.Assert.assertTrue
//import org.junit.Assert.assertFalse
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class AddRecipeViewModelTest {
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    private lateinit var userRepo: UserRecipeRepository
//    private lateinit var recipeParserApi: RecipeParserApi
//    private lateinit var networkMonitor: NetworkMonitor
//    private lateinit var application: Application
//    private lateinit var viewModel: AddRecipeViewModel
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//
//        userRepo = mockk(relaxed = true)
//        recipeParserApi = mockk()
//        networkMonitor = mockk()
//        application = mockk(relaxed = true)
//
//        viewModel = AddRecipeViewModel(
//            userRepo = userRepo,
//            recipeParserApi = recipeParserApi,
//            networkMonitor = networkMonitor,
//            application = application,
//            savedStateHandle = SavedStateHandle()
//        )
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    // ── سناریو ۱: بدون اینترنت ──────────────────────────
//    @Test
//    fun `parseRecipeFromText - no internet - shows internet error`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns false
//
//        viewModel.parseRecipeFromText("یک متن دستور پخت")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertTrue(state.parseError?.contains("اینترنت") == true)
//        assertFalse(state.isParsing)
//        assertFalse(state.parseSuccess)
//    }
//
//    // ── سناریو ۲: متن خالی — نباید اصلاً درخواست بره ──────
//    @Test
//    fun `parseRecipeFromText - blank text - does nothing`() = runTest {
//        viewModel.parseRecipeFromText("   ")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertFalse(state.isParsing)
//        assertNull(state.parseError)
//    }
//
//    // ── سناریو ۳: تایم‌اوت (احتمالاً نیاز به فیلترشکن) ────
//    @Test
//    fun `parseRecipeFromText - timeout - shows vpn hint`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns true
//        coEvery { recipeParserApi.parseRecipe(any()) } throws SocketTimeoutException()
//
//        viewModel.parseRecipeFromText("متن دستور پخت")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertTrue(state.parseError?.contains("فیلترشکن") == true)
//        assertFalse(state.isParsing)
//    }
//
//    // ── سناریو ۴: میزبان پیدا نشد (سرور در دسترس نیست) ────
//    @Test
//    fun `parseRecipeFromText - unknown host - shows connection error`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns true
//        coEvery { recipeParserApi.parseRecipe(any()) } throws UnknownHostException()
//
//        viewModel.parseRecipeFromText("متن دستور پخت")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertTrue(state.parseError != null)
//        assertFalse(state.isParsing)
//    }
//
//    // ── سناریو ۵: موفقیت کامل — فیلدها باید پر بشن ────────
//    @Test
//    fun `parseRecipeFromText - success - fills fields and sets parseSuccess`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns true
//        coEvery { recipeParserApi.parseRecipe(any()) } returns ParsedRecipeResponse(
//            title = "کیک شکلاتی",
//            description = "",
//            totalTime = "",
//            cookTime = "۳۰ دقیقه",
//            yield = "",
//            calories = "",
//            difficulty = "",
//            ingredients = emptyList(),
//            steps = listOf("مرحله اول"),
//            equipment = emptyList(),
//            error = null
//        )
//
//        viewModel.parseRecipeFromText("متن دستور پخت واقعی")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertEquals("کیک شکلاتی", state.title)
//        assertEquals("۳۰ دقیقه", state.cookTime)
//        assertEquals(1, state.steps.size)
//        assertTrue(state.parseSuccess)
//        assertFalse(state.isParsing)
//        assertNull(state.parseError)
//    }
//
//    // ── سناریو ۶: سرور خودش پیام خطا برگردونده (نه استثنا) ─
//    @Test
//    fun `parseRecipeFromText - server returns error field - shows that error`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns true
//        coEvery { recipeParserApi.parseRecipe(any()) } returns ParsedRecipeResponse(
//            error = "پاسخ مدل قابل تفسیر نبود"
//        )
//
//        viewModel.parseRecipeFromText("متن نامفهوم")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertEquals("پاسخ مدل قابل تفسیر نبود", state.parseError)
//        assertFalse(state.isParsing)
//    }
//
//    // ── سناریو ۷: متن نامرتبط اما بدون خطای سرور — فیلدهای
//    // خالی رو باید نادیده بگیره و مقدار قبلی رو نگه داره ────
//    @Test
//    fun `parseRecipeFromText - empty result fields - keeps previous values`() = runTest {
//        coEvery { networkMonitor.isConnected() } returns true
//        coEvery { recipeParserApi.parseRecipe(any()) } returns ParsedRecipeResponse(
//            title = "",  // خالی برگشته
//            error = null
//        )
//
//        viewModel.updateTitle("عنوان قبلی")
//        viewModel.parseRecipeFromText("سلام چطوری؟")
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        val state = viewModel.uiState.value
//        assertEquals("عنوان قبلی", state.title)  // نباید پاک بشه
//    }
//}