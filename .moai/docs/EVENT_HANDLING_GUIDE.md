# Event Handling Guide - SPEC-ANDROID-INTEGRATION-003

**Latest Update**: December 17, 2025
**Status**: ✅ Completed
**Implementation Phase**: SPEC-ANDROID-INTEGRATION-003 (앱 통합 및 전체 네비게이션 구성)

---

## Table of Contents

1. [Overview](#overview)
2. [MVI Pattern Foundation](#mvi-pattern-foundation)
3. [Event Flow Pipeline](#event-flow-pipeline)
4. [Screen Event Handlers](#screen-event-handlers)
5. [ViewModel Event Processing](#viewmodel-event-processing)
6. [Navigation Events](#navigation-events)
7. [Side Effects](#side-effects)
8. [Error Handling](#error-handling)
9. [Best Practices](#best-practices)

---

## Overview

The **Event Handling** system manages all user interactions and state changes through a unified MVI (Model-View-Intent) pattern across all screens.

### Core Principles

- ✅ **Unidirectional Data Flow** - Events flow one direction through the system
- ✅ **Intent-Driven** - User actions are expressed as intents
- ✅ **Immutable State** - State changes create new state objects
- ✅ **Predictable** - Event processing is deterministic
- ✅ **Testable** - All events can be unit tested

### Event Types

```
┌──────────────────────────────────────────────────────┐
│                   Event Categories                   │
├──────────────────────────────────────────────────────┤
│  1. User Actions (Clicks, inputs, gestures)          │
│  2. Lifecycle Events (onCreate, onResume, etc.)      │
│  3. Navigation Events (Navigation requests)          │
│  4. Data Events (API responses, DB updates)          │
│  5. System Events (Permission granted, etc.)         │
└──────────────────────────────────────────────────────┘
```

---

## MVI Pattern Foundation

### Model-View-Intent Architecture

```
┌────────────────┐
│   USER INPUT   │
│   (Click, Tap) │
└────────┬───────┘
         │
         ▼
┌────────────────────────────────────────────┐
│  VIEW (Composable)                         │
│  ├─ Renders UI state                       │
│  └─ Captures user input                    │
└────────┬───────────────────────────────────┘
         │
         │ sendEvent(Intent)
         │
         ▼
┌────────────────────────────────────────────┐
│  INTENT (User Action)                      │
│  ├─ OnProductClick(productId)              │
│  ├─ LoadProduct(productId)                 │
│  ├─ AddToCart(product)                     │
│  └─ UpdateQuantity(productId, quantity)    │
└────────┬───────────────────────────────────┘
         │
         │ submitEvent()
         │
         ▼
┌────────────────────────────────────────────┐
│  VIEWMODEL (Intent Processor)              │
│  ├─ Validate intent                        │
│  ├─ Process via reducer                    │
│  ├─ Update state                           │
│  └─ Emit side effects                      │
└────────┬───────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────┐
│  MODEL (State)                             │
│  ├─ products: List<Product>                │
│  ├─ isLoading: Boolean                     │
│  ├─ error: Exception?                      │
│  └─ cartItems: List<CartItem>              │
└────────┬───────────────────────────────────┘
         │
         │ State emission
         │ (StateFlow)
         │
         ▼
┌────────────────────────────────────────────┐
│  VIEW (Recomposition)                      │
│  └─ UI rendered with new state             │
└────────────────────────────────────────────┘
```

### Intent Hierarchy

```
HomeIntent (sealed class)
├─ RefreshProducts : HomeIntent
├─ RetryLoad : HomeIntent
├─ OnProductClick(productId: String) : HomeIntent

ProductDetailIntent (sealed class)
├─ LoadProduct(productId: String) : ProductDetailIntent
├─ AddToCart(product: Product) : ProductDetailIntent

CartIntent (sealed class)
├─ LoadCartItems : CartIntent
├─ UpdateQuantity(productId: String, newQuantity: Int) : CartIntent
├─ RemoveItem(productId: String) : CartIntent
├─ ClearCart : CartIntent
└─ DismissError : CartIntent
```

---

## Event Flow Pipeline

### Complete Event Processing Journey

#### Step 1: User Interacts with UI

```kotlin
// In Composable
Button(onClick = { onProductClick(productId) })
```

#### Step 2: Callback Invoked

```kotlin
// In HomeScreen
onProductClick = { productId ->
    coroutineScope.launch {
        viewModel.submitEvent(HomeContract.HomeEvent.OnProductClick(productId))
    }
}
```

#### Step 3: Event Submitted to ViewModel

```kotlin
// In HomeViewModel
fun submitEvent(event: HomeContract.HomeEvent) {
    viewModelScope.launch {
        processEvent(event)
    }
}

private suspend fun processEvent(event: HomeContract.HomeEvent) {
    when (event) {
        is HomeContract.HomeEvent.OnProductClick -> {
            // Emit side effect for navigation
            _sideEffect.emit(
                HomeSideEffect.NavigateToProductDetail(event.productId)
            )
        }
        // ... other events
    }
}
```

#### Step 4: State Reduced and Emitted

```kotlin
// ViewModel updates state
_uiState.value = currentState.copy(
    products = newProducts,
    isLoading = false
)
```

#### Step 5: UI Recomposed

```kotlin
// In Composable
val uiState by viewModel.uiState.collectAsState()

// LazyColumn renders with new state
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    items(uiState.products) { product ->
        ProductCard(product = product, onClick = { onProductClick(product.id) })
    }
}
```

---

## Screen Event Handlers

### HomeScreen Event Handlers

**File**: `feature/home/src/main/kotlin/.../HomeScreen.kt`

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val sideEffect by viewModel.sideEffect.collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

    // Side effect handling
    LaunchedEffect(sideEffect) {
        when (sideEffect) {
            is HomeContract.HomeSideEffect.NavigateToProductDetail -> {
                val productId = (sideEffect as HomeContract.HomeSideEffect.NavigateToProductDetail).productId
                onNavigateToDetail(productId)
            }
            is HomeContract.HomeSideEffect.ShowToast -> {
                val message = (sideEffect as HomeContract.HomeSideEffect.ShowToast).message
                snackbarHostState.showSnackbar(message)
            }
            null -> {}
        }
    }

    // State rendering
    when (uiState) {
        is HomeContract.HomeState.Success -> {
            val state = uiState as HomeContract.HomeState.Success
            SuccessView(
                products = state.products,
                isRefreshing = state.isRefreshing,
                onProductClick = { productId ->
                    coroutineScope.launch {
                        viewModel.submitEvent(
                            HomeContract.HomeEvent.OnProductClick(productId)
                        )
                    }
                },
                onRefresh = {
                    coroutineScope.launch {
                        viewModel.submitEvent(
                            HomeContract.HomeEvent.RefreshProducts
                        )
                    }
                }
            )
        }
        // ... other states
    }
}
```

#### Key Event Handlers

| Event | Trigger | Handler |
|-------|---------|---------|
| `OnProductClick` | Product card click | Navigate to ProductDetail |
| `RefreshProducts` | Pull-to-refresh | Reload product list |
| `RetryLoad` | Retry button click | Retry failed load |

### ProductDetailScreen Event Handlers

**File**: `feature/detail/src/main/kotlin/.../ProductDetailScreen.kt`

```kotlin
@Composable
fun ProductDetailScreen(
    state: ProductDetailState,
    onIntentSubmitted: (ProductDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Load product when screen appears
    LaunchedEffect(key1 = state) {
        // Product loading already triggered by NavigationHost
    }

    Column {
        // Product details
        Button(
            onClick = {
                coroutineScope.launch {
                    onIntentSubmitted(ProductDetailIntent.AddToCart(state.product))
                }
            }
        ) {
            Text("장바구니에 추가")
        }

        // Navigate to cart
        Button(onClick = onNavigateToCart) {
            Text("장바구니 보기")
        }

        // Back navigation
        Button(onClick = onNavigateBack) {
            Text("뒤로 가기")
        }
    }
}
```

#### Key Event Handlers

| Event | Trigger | Handler |
|-------|---------|---------|
| `LoadProduct` | Screen appears | Load product data |
| `AddToCart` | Add button click | Add product to cart |

### CartScreen Event Handlers

**File**: `feature/cart/src/main/kotlin/.../CartScreen.kt`

```kotlin
@Composable
fun CartScreen(
    state: CartState,
    onLoadCart: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onDismissError: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Load cart items when screen appears
    LaunchedEffect(Unit) {
        onLoadCart()
    }

    LazyColumn {
        items(state.cartItems) { item ->
            // Quantity controls
            Row {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onUpdateQuantity(item.productId, item.quantity - 1)
                        }
                    }
                ) {
                    Text("-")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            onUpdateQuantity(item.productId, item.quantity + 1)
                        }
                    }
                ) {
                    Text("+")
                }

                // Remove button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onRemoveItem(item.productId)
                        }
                    }
                ) {
                    Text("제거")
                }
            }
        }
    }

    // Clear cart
    Button(
        onClick = {
            coroutineScope.launch {
                onClearCart()
            }
        }
    ) {
        Text("장바구니 비우기")
    }

    // Error handling
    if (state.error != null) {
        AlertDialog(
            title = { Text("오류") },
            text = { Text(state.error.message ?: "") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onDismissError()
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            onDismissRequest = {
                coroutineScope.launch {
                    onDismissError()
                }
            }
        )
    }
}
```

#### Key Event Handlers

| Event | Trigger | Handler |
|-------|---------|---------|
| `LoadCartItems` | Screen appears | Load cart items |
| `UpdateQuantity` | Quantity button click | Update item quantity |
| `RemoveItem` | Remove button click | Remove from cart |
| `ClearCart` | Clear button click | Clear all items |
| `DismissError` | Error dialog dismiss | Clear error |

---

## ViewModel Event Processing

### Event Processing Pattern

```kotlin
class HomeViewModel(
    private val repository: HomeRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Initial)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<HomeSideEffect>()
    val sideEffect: SharedFlow<HomeSideEffect> = _sideEffect.asSharedFlow()

    fun submitEvent(event: HomeEvent) {
        viewModelScope.launch {
            processEvent(event)
        }
    }

    private suspend fun processEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.RefreshProducts -> {
                refreshProducts()
            }
            is HomeEvent.OnProductClick -> {
                _sideEffect.emit(
                    HomeSideEffect.NavigateToProductDetail(event.productId)
                )
            }
            is HomeEvent.RetryLoad -> {
                loadProducts()
            }
        }
    }

    private suspend fun loadProducts() {
        _uiState.value = HomeState.Loading

        try {
            val products = repository.getProducts()
            _uiState.value = HomeState.Success(products = products, isRefreshing = false)
        } catch (e: Exception) {
            _uiState.value = HomeState.Error(message = e.message ?: "Unknown error")
        }
    }

    private suspend fun refreshProducts() {
        val currentState = _uiState.value
        if (currentState is HomeState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }

        try {
            val products = repository.getProducts()
            _uiState.value = HomeState.Success(products = products, isRefreshing = false)
        } catch (e: Exception) {
            if (currentState is HomeState.Success) {
                _uiState.value = currentState.copy(isRefreshing = false)
            }
            _sideEffect.emit(HomeSideEffect.ShowError(message = e.message ?: "Unknown error"))
        }
    }
}
```

### Event Type Hierarchy

```kotlin
sealed class HomeEvent {
    object RefreshProducts : HomeEvent()
    object RetryLoad : HomeEvent()
    data class OnProductClick(val productId: String) : HomeEvent()
}

sealed class ProductDetailEvent {
    data class LoadProduct(val productId: String) : ProductDetailEvent()
    data class AddToCart(val product: Product) : ProductDetailEvent()
}

sealed class CartEvent {
    object LoadCartItems : CartEvent()
    data class UpdateQuantity(val productId: String, val newQuantity: Int) : CartEvent()
    data class RemoveItem(val productId: String) : CartEvent()
    object ClearCart : CartEvent()
    object DismissError : CartEvent()
}
```

---

## Navigation Events

### Navigation Event Flow

```
USER CLICKS PRODUCT
        │
        ▼
HomeScreen.onProductClick(productId)
        │
        ├─→ viewModel.submitEvent(OnProductClick(productId))
        │
        ▼
HomeViewModel.processEvent()
        │
        ├─→ emit(NavigateToProductDetail(productId))
        │
        ▼
HomeScreen.LaunchedEffect(sideEffect)
        │
        ├─→ onNavigateToDetail(productId)
        │
        ▼
NavigationHost
        │
        ├─→ navController.navigate(NavRoutes.productDetail(productId))
        │
        ▼
PRODUCTDETAILSCREEN RENDERED
```

### Navigation Event Types

```kotlin
sealed class HomeSideEffect {
    data class NavigateToProductDetail(val productId: String) : HomeSideEffect()
    data class ShowToast(val message: String) : HomeSideEffect()
    data class ShowError(val message: String) : HomeSideEffect()
}

sealed class ProductDetailSideEffect {
    object NavigateToCart : ProductDetailSideEffect()
    object NavigateBack : ProductDetailSideEffect()
}
```

---

## Side Effects

### Side Effect Management

Side effects are actions that don't directly change state but have external effects:

```
┌─────────────────────────────────────────┐
│          Side Effect Types              │
├─────────────────────────────────────────┤
│  1. Navigation                          │
│     - Navigate to new screen            │
│     - Pop back stack                    │
│                                         │
│  2. Toast/Snackbar                      │
│     - Show user messages                │
│     - Show errors                       │
│                                         │
│  3. Dialog                              │
│     - Show confirmation                 │
│     - Show error details                │
│                                         │
│  4. External APIs                       │
│     - Share content                     │
│     - Send notifications                │
│                                         │
│  5. Analytics/Logging                   │
│     - Track user actions                │
│     - Log errors                        │
└─────────────────────────────────────────┘
```

### Implementation Pattern

```kotlin
// Define side effects
sealed class UIEffect {
    object ShowLoadingDialog : UIEffect()
    object DismissLoadingDialog : UIEffect()
    data class ShowMessage(val message: String) : UIEffect()
    data class NavigateTo(val route: String) : UIEffect()
}

// ViewModel emits side effects
private val _sideEffect = MutableSharedFlow<UIEffect>()
val sideEffect: SharedFlow<UIEffect> = _sideEffect.asSharedFlow()

// Handle side effects in Composable
LaunchedEffect(sideEffect) {
    sideEffect.collect { effect ->
        when (effect) {
            UIEffect.ShowLoadingDialog -> {
                // Show loading
            }
            UIEffect.DismissLoadingDialog -> {
                // Dismiss loading
            }
            is UIEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is UIEffect.NavigateTo -> {
                navController.navigate(effect.route)
            }
        }
    }
}
```

---

## Error Handling

### Error Types and Handling

```kotlin
sealed class AppError {
    object NetworkError : AppError()
    object TimeoutError : AppError()
    data class ServerError(val code: Int) : AppError()
    data class ValidationError(val message: String) : AppError()
    object UnknownError : AppError()
}

// ViewModel processes errors
private suspend fun processEvent(event: HomeEvent) {
    try {
        when (event) {
            is HomeEvent.RefreshProducts -> {
                refreshProducts()
            }
        }
    } catch (e: IOException) {
        _uiState.value = HomeState.Error("네트워크 오류: ${e.message}")
        _sideEffect.emit(HomeSideEffect.ShowError("네트워크 연결을 확인하세요"))
    } catch (e: Exception) {
        _uiState.value = HomeState.Error(e.message ?: "알 수 없는 오류")
        _sideEffect.emit(HomeSideEffect.ShowError(e.message ?: "알 수 없는 오류"))
    }
}

// UI renders error state
when (uiState) {
    is HomeState.Error -> {
        ErrorView(
            message = (uiState as HomeState.Error).message,
            onRetry = {
                coroutineScope.launch {
                    viewModel.submitEvent(HomeEvent.RetryLoad)
                }
            }
        )
    }
}
```

---

## Best Practices

### 1. Always Use Coroutine Scope for Suspend Functions

```kotlin
// ✅ CORRECT
val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        coroutineScope.launch {
            viewModel.submitEvent(intent)
        }
    }
)

// ❌ WRONG: Compilation error
Button(
    onClick = {
        viewModel.submitEvent(intent)  // Suspend function outside coroutine
    }
)
```

### 2. Use Sealed Classes for Type-Safe Events

```kotlin
// ✅ CORRECT: Sealed class ensures all cases handled
sealed class HomeEvent {
    object LoadEvent : HomeEvent()
    data class SelectEvent(val id: String) : HomeEvent()
}

when (event) {  // Exhaustive, all cases required
    HomeEvent.LoadEvent -> {}
    is HomeEvent.SelectEvent -> {}
}

// ❌ WRONG: String constants lose type safety
val LOAD_EVENT = "load"
val SELECT_EVENT = "select"
// Can easily mistype event names
```

### 3. Separate State and Side Effects

```kotlin
// ✅ CORRECT: Separate flows for different concerns
val uiState: StateFlow<UIState> = _uiState
val sideEffect: SharedFlow<UIEffect> = _sideEffect

// ❌ WRONG: Mixed concerns in single stream
val events: SharedFlow<AppEvent> = _events  // Hard to handle predictably
```

### 4. Use LaunchedEffect with Proper Dependencies

```kotlin
// ✅ CORRECT: Dependency list ensures proper triggering
LaunchedEffect(productId) {
    viewModel.submitEvent(LoadProduct(productId))
}

// ❌ WRONG: No dependency - effect may not trigger properly
LaunchedEffect(Unit) {
    viewModel.submitEvent(LoadProduct(productId))  // productId change ignored
}
```

### 5. Handle Lifecycle Properly

```kotlin
// ✅ CORRECT: collectAsStateWithLifecycle respects lifecycle
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ✅ ALSO CORRECT: collectAsState works in Compose
val uiState by viewModel.uiState.collectAsState()

// ❌ RISKY: Manual collection without lifecycle awareness
var uiState: UIState? = null
LaunchedEffect(Unit) {
    viewModel.uiState.collect { state ->
        uiState = state
    }
}
```

---

## Summary

The **Event Handling System** provides:

1. ✅ **Unified Event Pipeline** - Consistent event processing across screens
2. ✅ **MVI Pattern** - Model-View-Intent ensures predictability
3. ✅ **Type Safety** - Sealed classes prevent runtime errors
4. ✅ **State Management** - StateFlow for reactive updates
5. ✅ **Side Effects** - SharedFlow for non-state changes
6. ✅ **Error Handling** - Centralized error processing

This ensures:
- 🎯 **Predictable Behavior** - Events always processed consistently
- 🚀 **Performance** - Efficient state updates via Compose
- 🛡️ **Safety** - Type-safe event definitions
- 📱 **Testability** - Easy to test event flows

---

**Related Documentation**:
- [APP_INTEGRATION_GUIDE.md](./APP_INTEGRATION_GUIDE.md) - Integration overview
- [NAVIGATION_ARCHITECTURE.md](./NAVIGATION_ARCHITECTURE.md) - Navigation patterns
- [INTEGRATION_TESTING.md](./INTEGRATION_TESTING.md) - Testing event flows

**Generated by**: Doc-Syncer Agent
**SPEC Reference**: SPEC-ANDROID-INTEGRATION-003
**TAG Coverage**: TAG-INT-004, TAG-INT-005
