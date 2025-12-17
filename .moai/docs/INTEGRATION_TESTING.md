# Integration Testing Guide - SPEC-ANDROID-INTEGRATION-003

**Latest Update**: December 17, 2025
**Status**: ✅ Completed
**Implementation Phase**: SPEC-ANDROID-INTEGRATION-003 (앱 통합 및 전체 네비게이션 구성)

---

## Table of Contents

1. [Testing Overview](#testing-overview)
2. [Navigation Testing](#navigation-testing)
3. [State Management Testing](#state-management-testing)
4. [Event Flow Testing](#event-flow-testing)
5. [Integration Test Examples](#integration-test-examples)
6. [Mock Setup](#mock-setup)
7. [Best Practices](#best-practices)

---

## Testing Overview

### Integration Testing Scope

```
┌────────────────────────────────────────────────────────┐
│              INTEGRATION TEST COVERAGE                 │
├────────────────────────────────────────────────────────┤
│  1. Navigation Graph                                   │
│     └─ Route transitions, parameters, back stack       │
│                                                        │
│  2. ViewModel Integration                             │
│     └─ Event submission, state updates, side effects  │
│                                                        │
│  3. Screen Composition                                │
│     └─ UI rendering, event handlers, callbacks        │
│                                                        │
│  4. End-to-End Flows                                  │
│     └─ Complete user journeys from UI to repository   │
│                                                        │
│  5. Error Handling                                    │
│     └─ Error states, recovery, user feedback          │
│                                                        │
│  6. State Persistence                                 │
│     └─ SavedStateHandle, process death recovery       │
└────────────────────────────────────────────────────────┘
```

### Testing Pyramid

```
              ▲
             /│\
            / │ \
           /  │  \     E2E Tests
          /   │   \    (UI Automation)
         /    │    \
        /─────┼─────\
       /      │      \
      /   Integration \  Integration Tests
     /       Tests      \  (Navigation, ViewModels)
    /        │           \
   /─────────┼───────────\
  /          │            \
 / Unit Tests (Repos, Use Cases)
└────────────────────────────────────────────┘

Integration tests verify: How components work together
```

### Test Dependencies

```kotlin
// File: core/data/build.gradle.kts

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.compose.ui.test)
}
```

---

## Navigation Testing

### Navigate Home to ProductDetail

```kotlin
@RunWith(RobolectricTestRunner::class)
class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testNavigateFromHomeToProductDetail() {
        // Setup
        composeTestRule.setContent {
            DaitsoNavHost(navController = navController)
        }

        // Verify HOME is initial screen
        assert(navController.currentDestination?.route == NavRoutes.HOME)

        // User clicks product
        composeTestRule.onNodeWithText("Product 1").performClick()

        // Verify navigation to ProductDetail
        assertEquals(NavRoutes.PRODUCT_DETAIL, navController.currentDestination?.route)
    }

    @Test
    fun testProductDetailReceivesProductId() {
        // Setup
        composeTestRule.setContent {
            DaitsoNavHost(navController = navController)
        }

        // Navigate to ProductDetail with productId
        navController.navigate(NavRoutes.productDetail("P123"))

        // Verify route and parameter
        val route = navController.currentDestination?.route
        assertEquals(NavRoutes.PRODUCT_DETAIL, route)

        // Verify productId parameter is extracted
        val savedStateHandle = navController.backStack.last().savedStateHandle
        assertEquals("P123", savedStateHandle.get<String>("productId"))
    }

    @Test
    fun testNavigateFromProductDetailToCart() {
        // Setup
        composeTestRule.setContent {
            DaitsoNavHost(navController = navController)
        }

        // Navigate to ProductDetail
        navController.navigate(NavRoutes.productDetail("P123"))

        // User clicks "View Cart"
        composeTestRule.onNodeWithText("장바구니 보기").performClick()

        // Verify navigation to Cart
        assertEquals(NavRoutes.CART, navController.currentDestination?.route)
    }

    @Test
    fun testBackNavigationFromProductDetail() {
        // Setup
        composeTestRule.setContent {
            DaitsoNavHost(navController = navController)
        }

        // Navigate: HOME → ProductDetail
        navController.navigate(NavRoutes.productDetail("P123"))
        assertEquals(NavRoutes.PRODUCT_DETAIL, navController.currentDestination?.route)

        // Simulate back press
        navController.popBackStack()

        // Verify we're back on HOME
        assertEquals(NavRoutes.HOME, navController.currentDestination?.route)
    }

    @Test
    fun testBackStackManagement() {
        // Setup
        composeTestRule.setContent {
            DaitsoNavHost(navController = navController)
        }

        // Build back stack: HOME → ProductDetail → Cart
        navController.navigate(NavRoutes.productDetail("P123"))
        navController.navigate(NavRoutes.CART)

        // Verify back stack structure
        val backStack = navController.backStack
        assertEquals(3, backStack.size)  // HOME, ProductDetail, Cart

        // Pop once
        navController.popBackStack()
        assertEquals(2, navController.backStack.size)  // HOME, ProductDetail

        // Pop twice
        navController.popBackStack()
        assertEquals(1, navController.backStack.size)  // HOME
    }
}
```

---

## State Management Testing

### ViewModel State Updates

```kotlin
@RunWith(RobolectricTestRunner::class)
class HomeViewModelIntegrationTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: FakeHomeRepository

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = FakeHomeRepository()
        viewModel = HomeViewModel(repository, SavedStateHandle())
    }

    @Test
    fun testRefreshProductsUpdatesState() = runTest {
        // Setup
        val testProducts = listOf(
            Product(id = "P1", name = "Product 1"),
            Product(id = "P2", name = "Product 2")
        )
        repository.setProducts(testProducts)

        // Trigger state collection
        val states = mutableListOf<HomeState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }

        // Trigger event
        viewModel.submitEvent(HomeEvent.RefreshProducts)
        advanceUntilIdle()

        // Verify state transitions
        assert(states[0] is HomeState.Initial)
        assert(states[1] is HomeState.Loading)
        assert(states[2] is HomeState.Success)

        val successState = states[2] as HomeState.Success
        assertEquals(2, successState.products.size)
        assertEquals("Product 1", successState.products[0].name)

        job.cancel()
    }

    @Test
    fun testErrorStateOnRepositoryFailure() = runTest {
        // Setup
        repository.setError("Network error")

        // Trigger state collection
        val states = mutableListOf<HomeState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }

        // Trigger event
        viewModel.submitEvent(HomeEvent.RefreshProducts)
        advanceUntilIdle()

        // Verify error state
        assert(states.last() is HomeState.Error)
        val errorState = states.last() as HomeState.Error
        assertEquals("Network error", errorState.message)

        job.cancel()
    }

    @Test
    fun testSideEffectEmission() = runTest {
        // Trigger side effect collection
        val sideEffects = mutableListOf<HomeSideEffect>()
        val job = launch {
            viewModel.sideEffect.collect { sideEffects.add(it) }
        }

        // Trigger navigation event
        viewModel.submitEvent(HomeEvent.OnProductClick("P123"))
        advanceUntilIdle()

        // Verify side effect
        assert(sideEffects.last() is HomeSideEffect.NavigateToProductDetail)
        val navEffect = sideEffects.last() as HomeSideEffect.NavigateToProductDetail
        assertEquals("P123", navEffect.productId)

        job.cancel()
    }
}
```

---

## Event Flow Testing

### Complete Event Processing

```kotlin
@RunWith(RobolectricTestRunner::class)
class EventFlowIntegrationTest {

    private lateinit var viewModel: ProductDetailViewModel
    private lateinit var repository: FakeProductRepository

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = FakeProductRepository()
        viewModel = ProductDetailViewModel(repository, SavedStateHandle())
    }

    @Test
    fun testCompleteEventFlow_LoadProduct() = runTest {
        // Setup test data
        val testProduct = Product(
            id = "P123",
            name = "Test Product",
            price = 29.99f
        )
        repository.setProduct("P123", testProduct)

        // Track state and side effects
        val states = mutableListOf<ProductDetailState>()
        val sideEffects = mutableListOf<ProductDetailSideEffect>()

        val stateJob = launch {
            viewModel.uiState.collect { states.add(it) }
        }

        val effectJob = launch {
            viewModel.sideEffect.collect { sideEffects.add(it) }
        }

        // STEP 1: Initial state
        assert(states[0] is ProductDetailState.Initial)

        // STEP 2: Submit LoadProduct event
        viewModel.submitEvent(ProductDetailIntent.LoadProduct("P123"))
        advanceUntilIdle()

        // STEP 3: Verify state transitions
        assert(states[1] is ProductDetailState.Loading)
        assert(states[2] is ProductDetailState.Success)

        // STEP 4: Verify product data
        val successState = states[2] as ProductDetailState.Success
        assertEquals("P123", successState.product.id)
        assertEquals("Test Product", successState.product.name)

        // STEP 5: Submit AddToCart event
        viewModel.submitEvent(ProductDetailIntent.AddToCart(testProduct))
        advanceUntilIdle()

        // STEP 6: Verify side effect
        assert(sideEffects.isNotEmpty())

        stateJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun testEventErrorHandling() = runTest {
        // Setup error
        repository.setError("Product not found")

        // Track states
        val states = mutableListOf<ProductDetailState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }

        // Trigger event
        viewModel.submitEvent(ProductDetailIntent.LoadProduct("INVALID"))
        advanceUntilIdle()

        // Verify error state
        val lastState = states.last()
        assert(lastState is ProductDetailState.Error)

        job.cancel()
    }
}
```

---

## Integration Test Examples

### Example 1: Complete User Journey Test

```kotlin
@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
class UserJourneyIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCompleteUserJourney_BrowseAndAddToCart() {
        // Setup: Navigate to home and verify products displayed
        composeTestRule.setContent {
            DaitsoNavHost()
        }

        // Step 1: HOME - Product listing visible
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
        composeTestRule.onNodeWithText("Product 1").assertIsDisplayed()

        // Step 2: USER CLICKS PRODUCT
        composeTestRule.onNodeWithText("Product 1").performClick()

        // Step 3: PRODUCT_DETAIL - Product details visible
        composeTestRule.onNodeWithText("Product 1 Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("$29.99").assertIsDisplayed()

        // Step 4: USER ADDS TO CART
        composeTestRule.onNodeWithText("장바구니에 추가").performClick()

        // Step 5: USER NAVIGATES TO CART
        composeTestRule.onNodeWithText("장바구니 보기").performClick()

        // Step 6: CART - Item visible
        composeTestRule.onNodeWithText("Product 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("수량: 1").assertIsDisplayed()

        // Step 7: USER UPDATES QUANTITY
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("수량: 2").assertIsDisplayed()

        // Step 8: USER GOES BACK
        composeTestRule.onNodeWithContentDescription("Navigate up").performClick()

        // Step 9: PRODUCT_DETAIL - Restored
        composeTestRule.onNodeWithText("Product 1 Details").assertIsDisplayed()
    }
}
```

### Example 2: Error Handling Test

```kotlin
@RunWith(RobolectricTestRunner::class)
class ErrorHandlingIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        // Configure repository to return errors
        MockRepositoryProvider.setNetworkError(true)
    }

    @After
    fun tearDown() {
        MockRepositoryProvider.reset()
    }

    @Test
    fun testNetworkErrorHandling() {
        composeTestRule.setContent {
            DaitsoNavHost()
        }

        // Error should be displayed
        composeTestRule.onNodeWithText("오류 발생").assertIsDisplayed()
        composeTestRule.onNodeWithText("네트워크 오류").assertIsDisplayed()

        // Retry button should be visible
        composeTestRule.onNodeWithText("재시도").assertIsDisplayed()

        // Click retry
        composeTestRule.onNodeWithText("재시도").performClick()

        // Wait for retry and verify recovery
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Product").fetchSemanticsNodes().isNotEmpty()
        }

        // Products should now be displayed
        composeTestRule.onNodeWithText("Product 1").assertIsDisplayed()
    }
}
```

---

## Mock Setup

### Fake Repository Implementation

```kotlin
class FakeHomeRepository : HomeRepository {

    private var products = listOf<Product>()
    private var error: Exception? = null

    fun setProducts(products: List<Product>) {
        this.products = products
    }

    fun setError(message: String) {
        this.error = Exception(message)
    }

    override suspend fun getProducts(): List<Product> {
        if (error != null) {
            throw error!!
        }
        return products
    }

    fun reset() {
        products = emptyList()
        error = null
    }
}
```

### Mock NavController Setup

```kotlin
@Before
fun setupNavController() {
    navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    navController.navigatorProvider.addNavigator(
        ComposeNavigator()
    )
    navController.navigatorProvider.addNavigator(
        DialogNavigator()
    )
}
```

### Hilt Test Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Singleton
    @Provides
    fun provideHomeRepository(): HomeRepository {
        return FakeHomeRepository()
    }

    @Singleton
    @Provides
    fun provideProductRepository(): ProductRepository {
        return FakeProductRepository()
    }

    @Singleton
    @Provides
    fun provideCartRepository(): CartRepository {
        return FakeCartRepository()
    }
}
```

---

## Best Practices

### 1. Use TestDispatchers for Coroutine Control

```kotlin
// ✅ CORRECT: Control execution timing
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun testWithControlledDispatchers() = runTest {
    viewModel.submitEvent(event)
    advanceUntilIdle()  // Wait for all coroutines
    // Verify state
}

// ❌ WRONG: Non-deterministic timing
@Test
fun testWithoutDispatcherControl() {
    viewModel.submitEvent(event)
    Thread.sleep(1000)  // Flaky test
    // Verify state
}
```

### 2. Use Turbine for Flow Testing

```kotlin
// ✅ CORRECT: Deterministic flow testing
@Test
fun testStateFlow() = runTest {
    viewModel.uiState.test {
        assertEquals(Initial, awaitItem())
        viewModel.submitEvent(event)
        assertEquals(Loading, awaitItem())
        assertEquals(Success, awaitItem())
        cancelAndConsumeRemainingEvents()
    }
}
```

### 3. Isolate Tests with Fresh ViewModels

```kotlin
// ✅ CORRECT: Each test starts fresh
@Before
fun setup() {
    viewModel = HomeViewModel(fakeRepository, SavedStateHandle())
}

// ❌ WRONG: Shared state between tests
private val viewModel = HomeViewModel(...)

@Test
fun test1() { /* modifies viewModel state */ }

@Test
fun test2() { /* test1 state affects this */ }
```

### 4. Test Navigation with NavController

```kotlin
// ✅ CORRECT: Verify navigation programmatically
@Test
fun testNavigation() {
    navController.navigate(NavRoutes.productDetail("P123"))

    // Verify route
    assertEquals(NavRoutes.PRODUCT_DETAIL, navController.currentDestination?.route)

    // Verify parameters
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    assertEquals("P123", savedStateHandle?.get<String>("productId"))
}
```

### 5. Use Compose Test Rule Finders

```kotlin
// ✅ CORRECT: Find by meaningful attributes
composeTestRule.onNodeWithText("Product 1").performClick()
composeTestRule.onNodeWithContentDescription("Add button").performClick()

// ❌ WRONG: Find by internal structure
composeTestRule.onNode(hasTestTag("item_0")).performClick()
```

---

## Summary

The **Integration Testing Guide** covers:

1. ✅ **Navigation Testing** - Route transitions, parameters, back stack
2. ✅ **State Management** - ViewModel state updates and verification
3. ✅ **Event Flow** - Complete event processing paths
4. ✅ **Integration Examples** - Real-world test scenarios
5. ✅ **Mock Setup** - Fake repositories and test doubles
6. ✅ **Best Practices** - Proven testing patterns

These tests ensure:
- 🎯 **End-to-End Validation** - Complete user flows work correctly
- 🐛 **Regression Prevention** - Changes don't break existing functionality
- 📱 **Quality Assurance** - Integration points verified
- 🧪 **Developer Confidence** - Tests catch integration issues early

---

**Related Documentation**:
- [APP_INTEGRATION_GUIDE.md](./APP_INTEGRATION_GUIDE.md) - Integration overview
- [EVENT_HANDLING_GUIDE.md](./EVENT_HANDLING_GUIDE.md) - Event patterns
- [NAVIGATION_ARCHITECTURE.md](./NAVIGATION_ARCHITECTURE.md) - Navigation design

**Generated by**: Doc-Syncer Agent
**SPEC Reference**: SPEC-ANDROID-INTEGRATION-003
**TAG Coverage**: TAG-INT-006 (Integration Testing)
