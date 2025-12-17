# App API Reference - SPEC-ANDROID-INTEGRATION-003

**Latest Update**: December 17, 2025
**Status**: ✅ Completed
**Implementation Phase**: SPEC-ANDROID-INTEGRATION-003 (앱 통합 및 전체 네비게이션 구성)

---

## Table of Contents

1. [MainActivity](#mainactivity)
2. [DaitsoApplication](#daitsoapp)
3. [DaitsoNavHost](#daitsonavhost)
4. [NavRoutes](#navroutes)
5. [Navigation Callbacks](#navigation-callbacks)
6. [Type Definitions](#type-definitions)

---

## MainActivity

**Location**: `app/src/main/kotlin/com/bup/ys/daitso/MainActivity.kt`

### Class Definition

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
}
```

### Description

Main entry point for the Daitso application. Hosts the Compose UI tree and initializes the navigation graph.

### Annotations

- `@AndroidEntryPoint` - Enables Hilt dependency injection for this Activity

### Lifecycle

| Method | When Called | Purpose |
|--------|-------------|---------|
| `onCreate(savedInstanceState)` | App startup | Initialize Compose content, set theme, host navigation |

### Implementation Details

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

### Key Responsibilities

1. **Initialize Compose Runtime**
   - Calls `setContent {}` to initialize the Compose framework
   - Manages the root Composable function

2. **Apply Theme**
   - Wraps entire UI with `DaitsoTheme`
   - Applies Material3 color scheme

3. **Host Navigation Graph**
   - Renders `DaitsoNavHost()` as root Composable
   - Manages all screen transitions

4. **Hilt Integration**
   - Receives dependency injections automatically
   - All Hilt modules are initialized before `onCreate()`

### Exported Configuration

**AndroidManifest.xml**:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Testing

```kotlin
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainActivityRenders() {
        composeTestRule.setContent {
            MainActivity()
        }
        // Verify HomeScreen is displayed
        composeTestRule.onNodeWithText("홈").assertIsDisplayed()
    }
}
```

---

## DaitsoApplication

**Location**: `app/src/main/kotlin/com/bup/ys/daitso/DaitsoApplication.kt`

### Class Definition

```kotlin
@HiltAndroidApp
class DaitsoApplication : Application() {
    // Hilt initialization happens automatically
}
```

### Description

Application class that initializes Hilt dependency injection container. Must be declared in AndroidManifest.xml.

### Annotations

- `@HiltAndroidApp` - Marks this class as Hilt application root
  - Generates Hilt component at compile-time
  - Enables dependency injection throughout the app

### Initialization Order

1. **App Process Started** (Linux/Android kernel)
2. **Application.onCreate()** called
3. **Hilt Component Generated** (Dagger code generation)
4. **All @Module classes initialized**
5. **Singleton dependencies created**
6. **Application ready for use**

### Key Responsibilities

1. **Hilt Initialization**
   - Generates DaggerApplicationComponent
   - Registers all @Module classes
   - Creates singleton instances

2. **Dependency Container**
   - Makes dependencies available to Activities
   - Manages dependency lifecycle (Singleton scope)

3. **Application Lifecycle**
   - Integrates with Android lifecycle
   - Survives Activity recreation

### Example Hilt Module Integration

```kotlin
// Example module that gets initialized
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Singleton
    @Provides
    fun provideProductService(retrofit: Retrofit): ProductService {
        return retrofit.create(ProductService::class.java)
    }
}

// Automatically injected in MainActivity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Dependencies are automatically provided
}
```

### AndroidManifest.xml Configuration

```xml
<application
    android:name=".DaitsoApplication"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.Daitso">
    <!-- Activities and services -->
</application>
```

### Testing

```kotlin
@RunWith(HiltTestRunner::class)
@HiltAndroidTest
class DaitsoApplicationTest {

    @Test
    fun applicationCreatesSuccessfully() {
        // Hilt automatically creates the application
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(context is DaitsoApplication)
    }
}
```

---

## DaitsoNavHost

**Location**: `app/src/main/kotlin/com/bup/ys/daitso/navigation/NavigationHost.kt`

### Composable Definition

```kotlin
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
)
```

### Description

Main navigation composable that defines the navigation graph for the entire application. Manages navigation between Home, ProductDetail, and Cart screens.

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `navController` | `NavHostController` | `rememberNavController()` | Navigation controller managing the back stack |

### Routes Defined

1. **HOME** - Home screen with product listing
2. **PRODUCT_DETAIL** - Product detail screen with parameter
3. **CART** - Shopping cart screen

### Implementation Structure

```kotlin
@Composable
fun DaitsoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        // Route definitions
    }
}
```

### Routes Detail

#### Route 1: HOME

```kotlin
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
```

**Characteristics**:
- Route: `"home"`
- Start destination (initially shown)
- No parameters
- ViewModel: `HomeViewModel`
- Transitions: To PRODUCT_DETAIL

#### Route 2: PRODUCT_DETAIL

```kotlin
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
```

**Characteristics**:
- Route: `"product_detail/{productId}"`
- Parameter: `productId` (String, required)
- ViewModel: `ProductDetailViewModel`
- Loading: Triggered via LaunchedEffect
- Transitions: To CART or back to HOME

#### Route 3: CART

```kotlin
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
```

**Characteristics**:
- Route: `"cart"`
- No parameters
- ViewModel: `CartViewModel`
- Loading: Triggered via LaunchedEffect
- Transitions: Back to PRODUCT_DETAIL

### Key Features

1. **ViewModel Injection** - Uses `hiltViewModel()` for scoped instances
2. **State Management** - Collects state via `collectAsState()`
3. **Side Effects** - Handles loading via `LaunchedEffect()`
4. **Event Handling** - Wraps suspend functions in `coroutineScope.launch {}`
5. **Back Stack** - Uses `popBackStack()` for back navigation

---

## NavRoutes

**Location**: `app/src/main/kotlin/com/bup/ys/daitso/navigation/NavigationHost.kt`

### Object Definition

```kotlin
object NavRoutes {
    const val HOME: String
    const val PRODUCT_DETAIL: String
    const val CART: String

    fun productDetail(productId: String): String
}
```

### Description

Singleton object providing type-safe route constants and builders for all navigation routes.

### Constants

#### HOME

```kotlin
const val HOME = "home"
```

**Type**: String literal
**Value**: `"home"`
**Usage**: `NavRoutes.HOME`
**Destination**: HomeScreen

#### PRODUCT_DETAIL

```kotlin
const val PRODUCT_DETAIL = "product_detail/{productId}"
```

**Type**: String template with parameter
**Value**: `"product_detail/{productId}"`
**Parameter**: `productId` (required, String type)
**Usage**: `NavRoutes.productDetail("P123")`
**Destination**: ProductDetailScreen

#### CART

```kotlin
const val CART = "cart"
```

**Type**: String literal
**Value**: `"cart"`
**Usage**: `NavRoutes.CART`
**Destination**: CartScreen

### Functions

#### productDetail(productId: String): String

```kotlin
fun productDetail(productId: String): String {
    return "product_detail/$productId"
}
```

**Purpose**: Build a complete route string with productId parameter

**Parameters**:
- `productId` (String, required) - The product identifier

**Returns**: Complete route string with parameter (e.g., `"product_detail/P123"`)

**Example Usage**:
```kotlin
// In HomeScreen
navController.navigate(NavRoutes.productDetail("P123"))

// Route: "product_detail/P123"
// Extracted parameter: productId = "P123"
```

### Usage Patterns

#### Navigate to Home
```kotlin
navController.navigate(NavRoutes.HOME)
```

#### Navigate to Product Detail
```kotlin
navController.navigate(NavRoutes.productDetail("P123"))
```

#### Navigate to Cart
```kotlin
navController.navigate(NavRoutes.CART)
```

#### Extract Parameter
```kotlin
composable(
    route = NavRoutes.PRODUCT_DETAIL,
    arguments = listOf(
        navArgument("productId") { type = NavType.StringType }
    )
) { backStackEntry ->
    val productId = backStackEntry.arguments?.getString("productId") ?: ""
    // Use productId
}
```

---

## Navigation Callbacks

### HomeScreen Callbacks

```kotlin
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
)
```

| Callback | Parameters | Purpose |
|----------|-----------|---------|
| `onProductClick` | `productId: String` | User clicked product card |
| `onNavigateToDetail` | `productId: String` | Navigate to product detail |

### ProductDetailScreen Callbacks

```kotlin
fun ProductDetailScreen(
    state: ProductDetailState,
    onIntentSubmitted: (ProductDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
)
```

| Callback | Parameters | Purpose |
|----------|-----------|---------|
| `onIntentSubmitted` | `intent: ProductDetailIntent` | Submit MVI intent |
| `onNavigateBack` | None | Go back to previous screen |
| `onNavigateToCart` | None | Navigate to cart screen |

### CartScreen Callbacks

```kotlin
fun CartScreen(
    state: CartState,
    onLoadCart: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onDismissError: () -> Unit
)
```

| Callback | Parameters | Purpose |
|----------|-----------|---------|
| `onLoadCart` | None | Reload cart items |
| `onUpdateQuantity` | `productId: String, newQuantity: Int` | Update item quantity |
| `onRemoveItem` | `productId: String` | Remove item from cart |
| `onClearCart` | None | Clear all items |
| `onDismissError` | None | Dismiss error message |

---

## Type Definitions

### Navigation Controller

```kotlin
val navController: NavHostController = rememberNavController()
```

**Type**: `NavHostController`
**Lifetime**: Remembered across recompositions
**Usage**: Manages back stack and route changes

### Route Arguments

```kotlin
navArgument("productId") {
    type = NavType.StringType
    nullable = false
}
```

**Types Supported**:
- `NavType.StringType` - String parameters
- `NavType.IntType` - Integer parameters
- `NavType.BoolType` - Boolean parameters
- `NavType.FloatType` - Float parameters

### Back Stack Entry

```kotlin
val backStackEntry: NavBackStackEntry = it
val productId: String? = backStackEntry.arguments?.getString("productId")
```

**Type**: `NavBackStackEntry`
**Properties**:
- `arguments: Bundle?` - Route parameters
- `destination: NavDestination` - Current route info
- `savedStateHandle: SavedStateHandle` - State persistence

---

## Summary

The **App API Reference** provides:

1. ✅ **MainActivity** - Application entry point
2. ✅ **DaitsoApplication** - Hilt configuration
3. ✅ **DaitsoNavHost** - Navigation graph definition
4. ✅ **NavRoutes** - Route constants and builders
5. ✅ **Callbacks** - Event handlers for each screen
6. ✅ **Type Definitions** - Navigation types and patterns

---

**Related Documentation**:
- [APP_INTEGRATION_GUIDE.md](./APP_INTEGRATION_GUIDE.md) - Integration overview
- [NAVIGATION_ARCHITECTURE.md](./NAVIGATION_ARCHITECTURE.md) - Navigation design
- [EVENT_HANDLING_GUIDE.md](./EVENT_HANDLING_GUIDE.md) - Event patterns

**Generated by**: Doc-Syncer Agent
**SPEC Reference**: SPEC-ANDROID-INTEGRATION-003
**TAG Coverage**: TAG-INT-001, TAG-INT-002, TAG-INT-003
