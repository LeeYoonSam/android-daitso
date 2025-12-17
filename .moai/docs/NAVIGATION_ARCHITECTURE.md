# Navigation Architecture - SPEC-ANDROID-INTEGRATION-003

**Latest Update**: December 17, 2025
**Status**: ✅ Completed
**Implementation Phase**: SPEC-ANDROID-INTEGRATION-003 (앱 통합 및 전체 네비게이션 구성)

---

## Table of Contents

1. [Overview](#overview)
2. [Navigation System Design](#navigation-system-design)
3. [Route Architecture](#route-architecture)
4. [Navigation Graph Structure](#navigation-graph-structure)
5. [Screen Transitions](#screen-transitions)
6. [Back Stack Management](#back-stack-management)
7. [Deep Linking (Future)](#deep-linking-future)
8. [Performance Optimization](#performance-optimization)
9. [Testing Navigation](#testing-navigation)

---

## Overview

The **Navigation Architecture** provides a unified routing system for the Daitso app, connecting three main screens (Home, Product Detail, Cart) through a hierarchical navigation graph.

### Design Principles

- ✅ **Declarative Routing** - Routes defined as constants
- ✅ **Type Safety** - Route parameters validated at compile-time
- ✅ **Back Stack Management** - Automatic back navigation
- ✅ **Screen Isolation** - Each screen manages its own state
- ✅ **Navigation Predictability** - Consistent routing patterns across app

### Navigation Stack

```
┌─────────────────────────────────────────────────────────┐
│                    Back Stack                           │
├─────────────────────────────────────────────────────────┤
│  [Home] ─→ [ProductDetail] ─→ [Cart] ─→ [ProductDetail]│
│   (Current)                          (Most Recent)      │
└─────────────────────────────────────────────────────────┘
```

---

## Navigation System Design

### Three-Route Architecture

```
HOME (Root)
├─ Route: "home"
├─ Screen: HomeScreen (Product listing)
├─ Arguments: None
└─ Navigation: To ProductDetail(productId)

PRODUCT_DETAIL
├─ Route: "product_detail/{productId}"
├─ Screen: ProductDetailScreen
├─ Arguments: productId (String)
└─ Navigation: To Cart or back to Home

CART
├─ Route: "cart"
├─ Screen: CartScreen
├─ Arguments: None
└─ Navigation: Back to ProductDetail
```

### Navigation Flow Graph

```
                    ┌─────────────┐
                    │   Home      │
                    │  Screen     │
                    └──────┬──────┘
                           │ Product Click
                           │ productId = "P123"
                           ▼
                    ┌─────────────────────┐
                    │ Product Detail      │
                    │ Screen              │
                    │ (productId: String) │
                    └──────┬──────────────┘
                           │ "Add to Cart"
                           │ Navigate to Cart
                           ▼
                    ┌─────────────┐
                    │   Cart      │
                    │  Screen     │
                    └─────────────┘
                           │
                           │ Back / Pop
                           ▼
                    ┌─────────────┐
                    │   Previous  │
                    │  Screen     │
                    └─────────────┘
```

---

## Route Architecture

### Route Constants Definition

**File**: `app/src/main/kotlin/com/bup/ys/daitso/navigation/NavigationHost.kt`

```kotlin
/**
 * Navigation route constants
 *
 * Provides type-safe route definitions for all screens.
 * Routes follow the format: screen_name[/{param}]
 */
object NavRoutes {

    /**
     * Home screen - Product listing
     * No parameters required
     * Destination: HomeScreen
     */
    const val HOME = "home"

    /**
     * Product detail screen
     * Parameter: productId (String)
     * Destination: ProductDetailScreen
     *
     * Usage: NavRoutes.productDetail("product-123")
     */
    const val PRODUCT_DETAIL = "product_detail/{productId}"

    /**
     * Shopping cart screen
     * No parameters required
     * Destination: CartScreen
     */
    const val CART = "cart"

    /**
     * Route builder for product detail navigation
     *
     * @param productId Product identifier
     * @return Complete route string with productId parameter
     *
     * Usage:
     * ```
     * navController.navigate(NavRoutes.productDetail("P123"))
     * ```
     */
    fun productDetail(productId: String): String {
        return "product_detail/$productId"
    }
}
```

### Route Parameter Types

| Route | Parameters | Type | Required |
|-------|-----------|------|----------|
| `home` | None | - | - |
| `product_detail/{productId}` | productId | String | Yes |
| `cart` | None | - | - |

### Parameter Validation

All route parameters are validated using `NavArgument`:

```kotlin
composable(
    route = NavRoutes.PRODUCT_DETAIL,
    arguments = listOf(
        navArgument("productId") {
            type = NavType.StringType  // Enforced at compile-time
            nullable = false           // Required parameter
        }
    )
) { backStackEntry ->
    val productId = backStackEntry.arguments?.getString("productId") ?: ""
    // Use productId safely
}
```

---

## Navigation Graph Structure

### Complete Navigation Graph

```kotlin
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        // HOME ROUTE
        composable(route = NavRoutes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onProductClick = { productId ->
                    navController.navigate(NavRoutes.productDetail(productId))
                },
                onNavigateToDetail = { productId ->
                    navController.navigate(NavRoutes.productDetail(productId))
                }
            )
        }

        // PRODUCT_DETAIL ROUTE
        composable(
            route = NavRoutes.PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val viewModel: ProductDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(productId) {
                viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
            }

            ProductDetailScreen(
                state = uiState,
                onIntentSubmitted = { intent ->
                    coroutineScope.launch {
                        viewModel.submitEvent(intent)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(NavRoutes.CART)
                }
            )
        }

        // CART ROUTE
        composable(route = NavRoutes.CART) {
            val viewModel: CartViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                viewModel.submitEvent(CartIntent.LoadCartItems)
            }

            CartScreen(
                state = uiState,
                onLoadCart = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.LoadCartItems)
                    }
                },
                onUpdateQuantity = { productId, newQuantity ->
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.UpdateQuantity(productId, newQuantity))
                    }
                },
                onRemoveItem = { productId ->
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.RemoveItem(productId))
                    }
                },
                onClearCart = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.ClearCart)
                    }
                },
                onDismissError = {
                    coroutineScope.launch {
                        viewModel.submitEvent(CartIntent.DismissError)
                    }
                }
            )
        }
    }
}
```

### Graph Entry Points

Each route has specific entry conditions:

```
HOME (startDestination)
├─ Triggered: App cold start
├─ Data: None
├─ ViewModel: HomeViewModel
└─ Initial State: Initial (empty) → Loading → Success/Error

PRODUCT_DETAIL
├─ Triggered: User clicks product on Home
├─ Data: productId parameter
├─ ViewModel: ProductDetailViewModel
└─ Initial State: Initial → Loading → Success/Error

CART
├─ Triggered: User clicks "View Cart" on ProductDetail
├─ Data: None (uses Repository)
├─ ViewModel: CartViewModel
└─ Initial State: Initial → Loading → Success/Error
```

---

## Screen Transitions

### Transition Diagrams

#### 1. Home → ProductDetail Transition

```
USER ACTION: Clicks product card
     │
     ▼
HomeScreen.onProductClick(productId)
     │
     ├─→ navController.navigate(NavRoutes.productDetail(productId))
     │
     ▼
Route Change: "home" → "product_detail/P123"
     │
     ├─→ Back stack: [home] → [home, product_detail/P123]
     │
     ▼
ProductDetailScreen composed
     │
     ├─→ LaunchedEffect(productId) triggered
     ├─→ viewModel.submitEvent(LoadProduct("P123"))
     │
     ▼
ProductDetailViewModel
     │
     ├─→ Intent: LoadProduct(productId)
     ├─→ Action: Fetch product from repository
     ├─→ State: Initial → Loading → Success(product)
     │
     ▼
UI RENDERED: Product detail page displays
```

#### 2. ProductDetail → Cart Transition

```
USER ACTION: Clicks "View Cart"
     │
     ▼
ProductDetailScreen.onNavigateToCart()
     │
     ├─→ navController.navigate(NavRoutes.CART)
     │
     ▼
Route Change: "product_detail/P123" → "cart"
     │
     ├─→ Back stack: [home, product_detail/P123] → [home, product_detail/P123, cart]
     │
     ▼
CartScreen composed
     │
     ├─→ LaunchedEffect(Unit) triggered
     ├─→ viewModel.submitEvent(LoadCartItems)
     │
     ▼
CartViewModel
     │
     ├─→ Intent: LoadCartItems
     ├─→ Action: Fetch cart from repository
     ├─→ State: Initial → Loading → Success(cartItems)
     │
     ▼
UI RENDERED: Cart screen displays all items
```

#### 3. ProductDetail → Home Transition (Back)

```
USER ACTION: Clicks back button
     │
     ▼
ProductDetailScreen.onNavigateBack()
     │
     ├─→ navController.popBackStack()
     │
     ▼
Route Change: "product_detail/P123" → "home"
     │
     ├─→ Back stack: [home, product_detail/P123] → [home]
     │
     ▼
HomeScreen recomposed (restored from state)
     │
     ├─→ LaunchedEffect NOT triggered (same key)
     ├─→ UI restored to previous state
     │
     ▼
UI RENDERED: Home screen restored
```

---

## Back Stack Management

### Back Stack Operations

#### Pop Back Stack (Back Navigation)

```kotlin
// Pop current screen, return to previous
navController.popBackStack()

// Result:
// Before: [Home] → [ProductDetail] → [Cart]
// After:  [Home] → [ProductDetail]
```

#### Navigate with Back Stack

```kotlin
// Navigate and add to back stack
navController.navigate(NavRoutes.CART)

// Result:
// Before: [Home] → [ProductDetail]
// After:  [Home] → [ProductDetail] → [Cart]
```

#### Clear Back Stack

```kotlin
// Navigate and clear back stack (rarely used)
navController.navigate(NavRoutes.HOME) {
    popUpTo(NavRoutes.HOME) { inclusive = true }
}

// Result: [Home]  (all previous entries removed)
```

### Back Stack State

```
┌────────────────────────────────────────────────┐
│           Back Stack (Lifo Stack)              │
├────────────────────────────────────────────────┤
│  Index 0: HOME (root)                          │
│  Index 1: PRODUCT_DETAIL (productId=P123)      │
│  Index 2: PRODUCT_DETAIL (productId=P456)      │
│  Index 3: CART ← Current (top of stack)        │
└────────────────────────────────────────────────┘

Back Button Press:
Index 3: CART (pop)
Index 2: PRODUCT_DETAIL (productId=P456) ← New current
```

### Handling Process Death

When the app is recreated after process death:

1. ✅ Navigation stack is restored from `savedStateHandle`
2. ✅ Each ViewModel's state is reconstructed
3. ✅ Current screen is displayed
4. ✅ User can continue from where they left off

```kotlin
// ViewModel handles state restoration
class ProductDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    // State is automatically restored from savedStateHandle
    fun getRestoredState(): ProductDetailState? {
        return savedStateHandle.get<ProductDetailState>("saved_state")
    }
}
```

---

## Deep Linking (Future)

### Deep Link Configuration

For future implementation, deep links would be configured as:

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data android:scheme="https" android:host="daitso.com" />
    </intent-filter>
</activity>
```

### Deep Link Routes

```kotlin
// Future implementation
val navigationGraph = navGraphBuilder {
    composable(
        route = "home",
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://daitso.com/" }
        )
    ) { HomeScreen() }

    composable(
        route = "product_detail/{productId}",
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://daitso.com/product/{productId}" }
        )
    ) { ProductDetailScreen() }
}
```

---

## Performance Optimization

### Lazy Composition

Screens are only composed when navigated to:

```kotlin
// ✅ EFFICIENT: Screens composed on demand
composable(route = NavRoutes.CART) {
    CartScreen()  // Only composed when user navigates to Cart
}

// Before navigation: CartScreen() NOT composed
// During navigation: CartScreen() composed
// After pop: CartScreen() disposed
```

### State Retention

States are retained when returning to a screen:

```kotlin
// ✅ EFFICIENT: State retained in back stack
Home (State: Loading → Success)
  ↓ Navigate to ProductDetail
ProductDetail (State: Loading → Success)
  ↓ Navigate to Cart
Cart (State: Loading → Success)
  ↓ Back
ProductDetail (State: RESTORED from saved state)
```

### Memory Management

```kotlin
// ✅ Back stack limits prevent memory leaks
NavHost(
    navController = navController,
    startDestination = NavRoutes.HOME
)

// NavController automatically manages:
// - Screen lifecycle
// - ViewModel lifecycle
// - Resource cleanup when screens removed from stack
```

---

## Testing Navigation

### Navigation Testing with Espresso

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testNavigationFromHomeToProductDetail() {
    composeTestRule.setContent {
        DaitsoNavHost()
    }

    // Find and click product
    composeTestRule.onNodeWithText("Product 1").performClick()

    // Verify navigation
    composeTestRule.onNodeWithText("Product Details").assertIsDisplayed()
}

@Test
fun testBackNavigationFromProductDetail() {
    composeTestRule.setContent {
        DaitsoNavHost()
    }

    // Navigate to ProductDetail
    composeTestRule.onNodeWithText("Product 1").performClick()

    // Click back
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()

    // Verify we're back on Home
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
}
```

### Navigation Testing with Robolectric

```kotlin
@RunWith(RobolectricTestRunner::class)
class NavigationTest {

    @Test
    fun testNavigationStack() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        navController.setGraph(R.navigation.nav_graph)
        navController.navigate(NavRoutes.HOME)

        assertEquals(NavRoutes.HOME, navController.currentDestination?.route)
    }
}
```

---

## Summary

The **Navigation Architecture** provides:

1. ✅ **Declarative Routes** - Type-safe route constants (NavRoutes)
2. ✅ **Hierarchical Graph** - Three main destinations with clear transitions
3. ✅ **Back Stack Management** - Automatic stack handling
4. ✅ **State Retention** - Screens restored when returning
5. ✅ **Performance Optimization** - Lazy composition and memory management
6. ✅ **Testability** - Navigation flows easily tested

This ensures:
- 🎯 **Predictable Navigation** - Users always know how to navigate
- 🚀 **Smooth Transitions** - Efficient screen composition
- 🛡️ **Robustness** - Proper back stack and state management
- 📱 **Scalability** - Easy to add new routes and destinations

---

**Related Documentation**:
- [APP_INTEGRATION_GUIDE.md](./APP_INTEGRATION_GUIDE.md) - Integration overview
- [APP_API_REFERENCE.md](./APP_API_REFERENCE.md) - Component APIs
- [NAVIGATION_FLOW_DIAGRAMS.md](./NAVIGATION_FLOW_DIAGRAMS.md) - Visual diagrams
- [EVENT_HANDLING_GUIDE.md](./EVENT_HANDLING_GUIDE.md) - Event patterns

**Generated by**: Doc-Syncer Agent
**SPEC Reference**: SPEC-ANDROID-INTEGRATION-003
**TAG Coverage**: TAG-INT-003, TAG-INT-004
