# Core DesignSystem 모듈 (:core:designsystem)

**Jetpack Compose 기반 Design System 모듈**

---

## 개요

`:core:designsystem` 모듈은 애플리케이션 전체에서 일관된 UI를 제공하는 Design System을 정의합니다. Material Design 3를 기반으로 하여 DaitsoTheme, 색상, 타이포그래피, 형태, 그리고 공통 UI 컴포넌트를 구현합니다.

### 모듈 특징

- ✅ Material Design 3 테마
- ✅ 재사용 가능한 UI 컴포넌트
- ✅ 일관된 색상 팔레트
- ✅ Jetpack Compose 기반

---

## 모듈 구조

```
core/designsystem/
├── build.gradle.kts
└── src/main/kotlin/com/bup/ys/daitso/core/designsystem/
    ├── theme/
    │   ├── Color.kt
    │   ├── Typography.kt
    │   ├── Shape.kt
    │   └── Theme.kt
    └── components/
        ├── DaitsoButton.kt
        ├── DaitsoTextField.kt
        ├── DaitsoLoadingIndicator.kt
        └── DaitsoErrorView.kt
```

---

## 테마 구성

### 1. Color.kt - 색상 팔레트

```kotlin
import androidx.compose.ui.graphics.Color

// Primary Colors
val PrimaryColor = Color(0xFF6200EE)
val OnPrimaryColor = Color(0xFFFFFFFF)
val PrimaryContainerColor = Color(0xFFEADDFF)
val OnPrimaryContainerColor = Color(0xFF21005E)

// Secondary Colors
val SecondaryColor = Color(0xFF03DAC6)
val OnSecondaryColor = Color(0xFF000000)
val SecondaryContainerColor = Color(0xFFB1F8F6)
val OnSecondaryContainerColor = Color(0xFF002019)

// Tertiary Colors
val TertiaryColor = Color(0xFFBB86FC)
val OnTertiaryColor = Color(0xFF000000)
val TertiaryContainerColor = Color(0xFFEADDFF)
val OnTertiaryContainerColor = Color(0xFF21005E)

// Error Colors
val ErrorColor = Color(0xFFB3261E)
val OnErrorColor = Color(0xFFFFFFFF)
val ErrorContainerColor = Color(0xFFF9DEDC)
val OnErrorContainerColor = Color(0xFF410E0B)

// Neutral Colors
val BackgroundColor = Color(0xFFFFFBFE)
val OnBackgroundColor = Color(0xFF1C1B1F)
val SurfaceColor = Color(0xFFFFFBFE)
val OnSurfaceColor = Color(0xFF1C1B1F)
val SurfaceVariantColor = Color(0xFFE7E0EC)
val OnSurfaceVariantColor = Color(0xFF49454E)
val OutlineColor = Color(0xFF79747E)
val OutlineVariantColor = Color(0xFFCAC4D0)
val ScrimColor = Color(0xFF000000)
```

#### 색상 사용 가이드

| 색상 | 용도 | 예시 |
|------|------|------|
| **Primary** | 주요 버튼, 강조 | FloatingActionButton, 선택된 탭 |
| **Secondary** | 보조 강조 | 숨겨진 버튼 |
| **Error** | 에러 상태 | 에러 메시지, 유효성 검사 실패 |
| **Surface** | 배경, 카드 | Card, Dialog |

---

### 2. Typography.kt - 타이포그래피

```kotlin
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DaitsoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
```

#### 텍스트 스타일 사용

```kotlin
@Composable
fun ProductCard() {
    Column {
        Text(
            text = "상품명",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "상품 설명",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

---

### 3. Shape.kt - 형태

```kotlin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val DaitsoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
```

#### 형태 사용

```kotlin
@Composable
fun ProductCard() {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 내용
        }
    }
}
```

---

### 4. Theme.kt - DaitsoTheme

```kotlin
@Composable
fun DaitsoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSystemInDarkTheme()) {
        // For now, use light scheme even in dark mode
        LightColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DaitsoTypography,
        shapes = DaitsoShapes,
        content = content
    )
}
```

#### Theme 적용

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaitsoTheme {
                MainScreen()
            }
        }
    }
}
```

---

## UI 컴포넌트

### 1. DaitsoButton

```kotlin
@Composable
fun DaitsoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}
```

**사용 예시:**

```kotlin
DaitsoButton(
    text = "로그인",
    onClick = { viewModel.login() },
    isLoading = state.isLoading
)
```

---

### 2. DaitsoTextField

```kotlin
@Composable
fun DaitsoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            isError = isError,
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
```

**사용 예시:**

```kotlin
DaitsoTextField(
    value = email,
    onValueChange = { email = it },
    label = "이메일",
    isError = emailError != null,
    errorMessage = emailError
)
```

---

### 3. DaitsoLoadingIndicator

```kotlin
@Composable
fun DaitsoLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
```

**사용 예시:**

```kotlin
when (state) {
    is UiState.Loading -> DaitsoLoadingIndicator()
    is UiState.Content -> ProductList(state.products)
}
```

---

### 4. DaitsoErrorView

```kotlin
@Composable
fun DaitsoErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "오류 발생",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DaitsoButton(
                text = "다시 시도",
                onClick = onRetry
            )
        }
    }
}
```

**사용 예시:**

```kotlin
when (state) {
    is UiState.Error -> DaitsoErrorView(
        message = state.message,
        onRetry = { viewModel.retry() }
    )
}
```

---

## 의존성

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.daitso.android.library)
    alias(libs.plugins.daitso.android.library.compose)
}

dependencies {
    implementation(libs.androidx.compose.bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
```

---

## 확장 가이드

새로운 컴포넌트를 추가할 때는 다음을 고려합니다:

1. **Theme 일관성**
   - MaterialTheme의 색상, 타이포그래피, 형태 사용
   - 커스텀 색상 최소화

2. **재사용성**
   - 일반적인 UI 패턴 구현
   - 파라미터로 유연성 제공

3. **접근성**
   - contentDescription 제공
   - 충분한 터치 영역 (48dp 권장)

---

## 테스트

Compose Preview로 UI 컴포넌트를 테스트합니다:

```kotlin
@Preview(showBackground = true)
@Composable
fun DaitsoButtonPreview() {
    DaitsoTheme {
        DaitsoButton(
            text = "클릭",
            onClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun DaitsoButtonDarkPreview() {
    DaitsoTheme {
        DaitsoButton(
            text = "클릭",
            onClick = { }
        )
    }
}
```

---

## 참고

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [SPEC-ANDROID-INIT-001](../../specs/SPEC-ANDROID-INIT-001/spec.md)

---

**최종 업데이트**: 2025-11-28
**SPEC 기반**: SPEC-ANDROID-INIT-001
