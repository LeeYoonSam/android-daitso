# App Integration Guide - SPEC-ANDROID-INTEGRATION-003

**Latest Update**: December 17, 2025
**Status**: ✅ Completed
**Implementation Phase**: SPEC-ANDROID-INTEGRATION-003 (앱 통합 및 전체 네비게이션 구성)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Overview](#architecture-overview)
3. [Core Components](#core-components)
4. [Integration Flow](#integration-flow)
5. [Setup and Configuration](#setup-and-configuration)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## Overview

The **App Integration Layer** represents the complete integration of all features (Home, Product Detail, Cart) into a cohesive Android application using:

- **Jetpack Compose** - Modern declarative UI framework
- **Jetpack Navigation** - Type-safe navigation (string-based routes)
- **Hilt Dependency Injection** - Compile-time DI with constructor injection
- **MVI (Model-View-Intent)** - Unidirectional data flow architecture
- **Kotlin Coroutines** - Async/concurrent operations

### Key Objectives

✅ **Unified App Entry Point** - Single `MainActivity` manages all UI
✅ **Seamless Navigation** - Type-safe routing between screens
✅ **Dependency Management** - All features properly injected via Hilt
✅ **State Management** - Centralized MVI state across all screens
✅ **Event Handling** - Coordinated event processing from all features

---

## Architecture Overview

### Three-Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│ Presentation Layer (Jetpack Compose UI)                │
│ ┌────────────┬──────────────┬──────────────┐           │
│ │ HomeScreen │ DetailScreen │  CartScreen  │           │
│ └────────────┴──────────────┴──────────────┘           │
└─────────────────────────────────────────────────────────┘
                         ▲
                         │ Navigation Events
                         ▼
┌─────────────────────────────────────────────────────────┐
│ Navigation Layer (Jetpack Navigation Compose)          │
│ ┌──────────────────────────────────────────────────┐  │
│ │ DaitsoNavHost - Manages navigation routes       │  │
│ │ NavRoutes - Route constants and builders        │  │
│ │ NavController - Back stack and route changes    │  │
│ └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                         ▲
                         │ State & Side Effects
                         ▼
┌─────────────────────────────────────────────────────────┐
│ ViewModel Layer (MVI Pattern)                          │
│ ┌────────────┬──────────────┬──────────────┐           │
│ │HomeViewModel│ DetailViewModel│CartViewModel│          │
│ └────────────┴──────────────┴──────────────┘           │
└─────────────────────────────────────────────────────────┘
                         ▲
                         │ Data Flow
                         ▼
┌─────────────────────────────────────────────────────────┐
│ Data Layer (Repository Pattern)                        │
│ ┌────────────┬──────────────┬──────────────┐           │
│ │HomeRepository│DetailRepository│CartRepository│        │
│ └────────────┴──────────────┴──────────────┘           │
└─────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI Framework** | Jetpack Compose | Declarative UI rendering |
| **Navigation** | Jetpack Navigation Compose | Screen routing and back stack |
| **Dependency Injection** | Hilt + Dagger | Constructor injection |
| **State Management** | Kotlin StateFlow | Reactive state updates |
| **Concurrency** | Kotlin Coroutines | Async operations |
| **HTTP Client** | Retrofit | Network requests |
| **Local Storage** | Room Database | Data persistence |
| **Image Loading** | Coil | Efficient image rendering |

---

## Core Components

### 1. MainActivity - Application Entry Point

**File**: `app/src/main/kotlin/com/bup/ys/daitso/MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaitsoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DaitsoNavHost()
                }
            }
        }
    }
}
```

**Key Responsibilities**:
- ✅ Initializes the Compose runtime
- ✅ Applies theme (DaitsoTheme)
- ✅ Hosts the main navigation graph (DaitsoNavHost)
- ✅ Receives Hilt injections via `@AndroidEntryPoint`

### 2. DaitsoApplication - Hilt Configuration

**File**: `app/src/main/kotlin/com/bup/ys/daitso/DaitsoApplication.kt`

```kotlin
@HiltAndroidApp
class DaitsoApplication : Application() {
    // Hilt initialization happens automatically
    // All dependency injection is configured through Hilt modules
}
```

**Key Responsibilities**:
- ✅ Marks the application for Hilt code generation
- ✅ Initializes dependency injection container
- ✅ Registers all @Module annotated classes
- ✅ Enables Hilt's automatic injection support

**AndroidManifest.xml Configuration**:
```xml
<application
    android:name=".DaitsoApplication"
    ...>
    <activity android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

### 3. DaitsoNavHost - Navigation Graph

**File**: `app/src/main/kotlin/com/bup/ys/daitso/navigation/NavigationHost.kt`

```kotlin
/**
 * Navigation route constants
 */
object NavRoutes {
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"

    fun productDetail(productId: String) = "product_detail/$productId"
}

/**
 * Main navigation host for the Daitso application.
 *
 * Manages navigation between three main screens:
 * - Home: Product listing screen
 * - ProductDetail: Product detail page with add to cart functionality
 * - Cart: Shopping cart screen
 */
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
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

**Key Features**:
- ✅ String-based route definitions (not type-safe routes for compatibility)
- ✅ Route arguments support (e.g., `productId` parameter)
- ✅ ViewModel injection via `hiltViewModel()`
- ✅ LaunchedEffect for side effects (e.g., loading data)
- ✅ Back stack management with `popBackStack()`

---

## Integration Flow

### User Navigation Journey

```
┌─────────┐
│  START  │ (Cold start - MainActivity created)
└────┬────┘
     │
     ▼
┌──────────────────────────┐
│ Hilt Initializes DI      │
│ (DaitsoApplication)      │
└────┬─────────────────────┘
     │
     ▼
┌──────────────────────────┐
│ MainActivity renders     │
│ DaitsoNavHost            │
└────┬─────────────────────┘
     │
     ▼
┌──────────────────────────┐
│ HomeScreen displayed     │
│ (startDestination)       │
└────┬─────────────────────┘
     │ User clicks product
     ▼
┌──────────────────────────────────────────┐
│ navController.navigate(                  │
│   NavRoutes.productDetail(productId)    │
│ )                                        │
└────┬─────────────────────────────────────┘
     │
     ▼
┌──────────────────────────┐
│ ProductDetailScreen      │
│ (new back stack entry)   │
└────┬─────────────────────┘
     │ User adds to cart
     ├─→ CartIntent.AddToCart(product)
     │   → CartViewModel processes event
     │   → State updates with new item
     │
     │ User clicks "View Cart"
     ▼
┌──────────────────────────┐
│ navController.navigate(  │
│   NavRoutes.CART        │
│ )                        │
└────┬─────────────────────┘
     │
     ▼
┌──────────────────────────┐
│ CartScreen displayed     │
│ (with cart items)        │
└────┬─────────────────────┘
     │ User clicks back/up
     ▼
┌──────────────────────────┐
│ navController.popBackStack()
│ (returns to previous)    │
└──────────────────────────┘
```

### Event Processing Pipeline

```
User Action (Click, Input)
        │
        ▼
UI Event Handler (Composable)
        │
        ├─→ coroutineScope.launch {
        │     viewModel.submitEvent(intent)
        │   }
        │
        ▼
ViewModel.submitEvent(intent)
        │
        ├─→ Validate intent
        ├─→ Process via reducer
        ├─→ Update state
        │
        ▼
StateFlow emission
        │
        ├─→ collectAsState() in Composable
        │   (subscribes to state updates)
        │
        ▼
UI Recomposition
        │
        ├─→ Display new state
        ├─→ Re-render affected Composables
        │
        ▼
Side Effects Processed
        │
        ├─→ Navigation events
        ├─→ Snackbar/Toast messages
        ├─→ Data loading
        │
        ▼
User sees updated UI
```

---

## Setup and Configuration

### 1. Dependencies Configuration

**File**: `app/build.gradle.kts`

Required dependencies for app integration:

```kotlin
dependencies {
    // Compose
    implementation(libs.androidx.compose.bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.compose.navigation)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Feature modules
    implementation(project(":feature:home"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:cart"))

    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:model"))
}
```

### 2. Hilt Module Configuration

**File**: `app/src/main/kotlin/com/bup/ys/daitso/di/AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplicationContext(app: Application): Context {
        return app.applicationContext
    }

    // Retrofit, Room, other singleton instances
}
```

### 3. AndroidManifest.xml

**File**: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".DaitsoApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:debuggable="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Daitso">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Daitso">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

### 4. Theme Configuration

**File**: `app/src/main/kotlin/com/bup/ys/daitso/ui/theme/Theme.kt`

```kotlin
@Composable
fun DaitsoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )
        else -> lightColorScheme(
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Best Practices

### 1. ViewModel Lifecycle Management

```kotlin
// ✅ CORRECT: Use hiltViewModel() to get scoped ViewModels
@Composable
fun MyScreen() {
    val viewModel: MyViewModel = hiltViewModel()
    // ViewModel is scoped to this composable's lifecycle
}

// ❌ WRONG: Creating ViewModel manually
val viewModel = MyViewModel()
// No Hilt injection, manual state management
```

### 2. Navigation Best Practices

```kotlin
// ✅ CORRECT: Use NavRoutes constants
navController.navigate(NavRoutes.productDetail(productId))

// ❌ WRONG: Hardcoded route strings
navController.navigate("product_detail/$productId")
```

### 3. Event Processing in Composables

```kotlin
// ✅ CORRECT: Use coroutineScope for suspend functions
val coroutineScope = rememberCoroutineScope()

Button(
    onClick = {
        coroutineScope.launch {
            viewModel.submitEvent(MyIntent.DoSomething)
        }
    }
)

// ❌ WRONG: Calling suspend function directly
Button(
    onClick = {
        viewModel.submitEvent(MyIntent.DoSomething)  // Compilation error
    }
)
```

### 4. State Collection Best Practices

```kotlin
// ✅ CORRECT: Use collectAsState() for recomposition
val uiState by viewModel.uiState.collectAsState()

// ✅ ALSO CORRECT: Use collectAsStateWithLifecycle() for lifecycle awareness
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ❌ WRONG: Collecting in LaunchedEffect without recomposition trigger
LaunchedEffect(Unit) {
    viewModel.uiState.collect { state ->
        // Not recommended for state updates
    }
}
```

### 5. Side Effect Handling

```kotlin
// ✅ CORRECT: Use LaunchedEffect with dependency list
LaunchedEffect(productId) {
    viewModel.submitEvent(ProductDetailIntent.LoadProduct(productId))
}

// ✅ ALSO CORRECT: Handle side effects in Composable
LaunchedEffect(sideEffect) {
    when (sideEffect) {
        is NavigateToCart -> navController.navigate(NavRoutes.CART)
        is ShowError -> snackbarHostState.showSnackbar(sideEffect.message)
        null -> {}
    }
}
```

---

## Troubleshooting

### Issue: "Cannot find Hilt ViewModel provider"

**Cause**: Missing `@AndroidEntryPoint` on Activity or Activity not running in Hilt context

**Solution**:
```kotlin
@AndroidEntryPoint  // Required!
class MainActivity : ComponentActivity() {
    // ...
}
```

### Issue: "Navigation route not found"

**Cause**: Using incorrect route string or parameter type

**Solution**:
```kotlin
// Use NavRoutes constants
navController.navigate(NavRoutes.productDetail(productId))
// OR
navController.navigate("product_detail/$productId")  // Must match PRODUCT_DETAIL constant

// For parameters, ensure type is correct
navArgument("productId") { type = NavType.StringType }  // STRING, not INT
```

### Issue: "Suspend function submitEvent should be called only from coroutine"

**Cause**: Calling suspend function directly from click handler

**Solution**:
```kotlin
val coroutineScope = rememberCoroutineScope()

Button(
    onClick = {
        coroutineScope.launch {  // Launch coroutine
            viewModel.submitEvent(intent)  // Now safe to call
        }
    }
)
```

### Issue: "Process death and ViewModel state loss"

**Cause**: ViewModel not properly saving state before process death

**Solution**:
```kotlin
// Use SavedStateHandle for persistence
class MyViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: MyRepository
) : ViewModel() {

    private val savedData = savedStateHandle.getLiveData<Data>("saved_key")

    fun saveData(data: Data) {
        savedStateHandle["saved_key"] = data
    }
}
```

---

## Summary

The **App Integration Layer** successfully unifies all features into a cohesive application through:

1. ✅ **Single Entry Point** - MainActivity as the main Compose container
2. ✅ **Dependency Injection** - Hilt manages all dependencies automatically
3. ✅ **Navigation Graph** - DaitsoNavHost connects all screens
4. ✅ **State Management** - MVI pattern across all ViewModels
5. ✅ **Event Processing** - Unified event handling pipeline

This architecture ensures:
- 🔄 **Reactive Updates** - UI responds to state changes
- 🎯 **Type Safety** - Navigation routes validated at compile-time (via constants)
- 🚀 **Performance** - Efficient recomposition via Compose
- 🛡️ **Testability** - Dependency injection enables easy mocking
- 📱 **Maintainability** - Clear separation of concerns

---

**Related Documentation**:
- [NAVIGATION_ARCHITECTURE.md](./NAVIGATION_ARCHITECTURE.md) - Detailed navigation flow
- [APP_API_REFERENCE.md](./APP_API_REFERENCE.md) - Component API documentation
- [EVENT_HANDLING_GUIDE.md](./EVENT_HANDLING_GUIDE.md) - Event processing patterns
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Overall system architecture

**Generated by**: Doc-Syncer Agent
**SPEC Reference**: SPEC-ANDROID-INTEGRATION-003
**TAG Coverage**: TAG-INT-001, TAG-INT-002, TAG-INT-003, TAG-INT-004, TAG-INT-005
