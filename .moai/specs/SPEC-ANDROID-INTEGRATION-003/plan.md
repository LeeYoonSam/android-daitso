# SPEC-ANDROID-INTEGRATION-003: 앱 통합 및 네비게이션 - 구현 계획

## 📊 구현 단계

### Phase 1: :app 모듈 설정 (모든 Feature 의존성 추가)

**목표:** :app 모듈이 모든 Feature 모듈을 의존성으로 포함

**작업:**
1. :app/build.gradle.kts 수정
   ```kotlin
   dependencies {
       // Core modules
       implementation(project(":core:model"))
       implementation(project(":core:common"))
       implementation(project(":core:designsystem"))
       implementation(project(":core:network"))
       implementation(project(":core:database"))
       implementation(project(":core:data"))

       // Feature modules
       implementation(project(":feature:home"))
       implementation(project(":feature:detail"))
       implementation(project(":feature:cart"))

       // Compose & Navigation
       implementation(platform(libs.androidx.compose.bom))
       implementation(libs.androidx.compose.ui)
       implementation(libs.androidx.compose.material3)
       implementation(libs.androidx.compose.navigation)
       implementation(libs.androidx.activity.compose)

       // Hilt
       implementation(libs.hilt.android)
       kapt(libs.hilt.compiler)
       implementation(libs.androidx.hilt.navigation.compose)

       // Serialization for type-safe navigation
       implementation(libs.kotlinx.serialization.json)
   }
   ```

2. Gradle Sync 및 검증

**예상 소요 시간:** 30분

**성공 기준:**
- ✅ Gradle Sync 성공
- ✅ 모든 Feature 모듈 의존성 해결

---

### Phase 2: MainActivity 작성 및 NavigationHost 설정

**목표:** 메인 액티비티 작성 및 네비게이션 호스트 설정

**작업:**
1. DaitsoApplication.kt 검증 (이미 작성됨)
   ```kotlin
   @HiltAndroidApp
   class DaitsoApplication : Application()
   ```

2. MainActivity.kt 작성
   ```kotlin
   @AndroidEntryPoint
   class MainActivity : ComponentActivity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           enableEdgeToEdge()
           setContent {
               DaitsoTheme {
                   NavigationHost()
               }
           }
       }
   }
   ```

3. navigation/NavigationHost.kt 작성
   - NavHostController 설정
   - 네비게이션 라우트 정의
   - 각 composable 등록

4. AndroidManifest.xml 수정
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

**예상 소요 시간:** 1시간

**성공 기준:**
- ✅ MainActivity 빌드 성공
- ✅ Manifest 구성 완료

---

### Phase 3: 전체 Navigation Graph 구성

**목표:** Type-safe Navigation Route 정의 및 구현

**작업:**
1. Navigation Routes 정의
   ```kotlin
   // navigation/Routes.kt
   @Serializable
   object HomeRoute

   @Serializable
   data class ProductDetailRoute(val productId: String)

   @Serializable
   object CartRoute

   @Serializable
   object CheckoutRoute
   ```

2. NavigationHost 구현
   ```kotlin
   @Composable
   fun NavigationHost(
       navController: NavHostController = rememberNavController()
   ) {
       NavHost(
           navController = navController,
           startDestination = HomeRoute,
           modifier = Modifier.fillMaxSize()
       ) {
           composable<HomeRoute> {
               HomeScreen(
                   onNavigateToDetail = { productId ->
                       navController.navigate(ProductDetailRoute(productId))
                   }
               )
           }
           composable<ProductDetailRoute> { backStackEntry ->
               val args = backStackEntry.arguments
               ProductDetailScreen(
                   productId = args?.getString("productId") ?: "",
                   onNavigateBack = { navController.navigateUp() },
                   onNavigateToCart = { navController.navigate(CartRoute) }
               )
           }
           composable<CartRoute> {
               CartScreen(
                   onNavigateBack = { navController.navigateUp() },
                   onNavigateToCheckout = { navController.navigate(CheckoutRoute) }
               )
           }
       }
   }
   ```

3. 각 Feature 모듈에서 NavigationHost에 연동되는지 확인

**예상 소요 시간:** 2시간

**성공 기준:**
- ✅ 모든 Route 정의 완료
- ✅ NavigationHost 구현 완료
- ✅ 타입 안전 네비게이션 동작

---

### Phase 4: Hilt 의존성 그래프 검증

**목표:** 모든 Hilt 모듈이 올바르게 통합되었는지 확인

**작업:**
1. 각 모듈의 Hilt 설정 검증
   - `@HiltViewModel` 적용 확인
   - `@Inject` 필드 검증
   - `@Module` + `@InstallIn` 확인

2. 의존성 그래프 생성
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```

3. 순환 참조 검사
   - Module 간 순환 참조 없는지 확인
   - 의존성 방향 검증

4. 컴파일 에러 해결
   - Hilt 의존성 충돌 해결
   - Missing binding 해결

**예상 소요 시간:** 1시간

**성공 기준:**
- ✅ 빌드 성공
- ✅ Hilt 의존성 그래프 생성 성공
- ✅ 런타임 DI 오류 없음

---

### Phase 5: 에뮬레이터 테스트 및 디버깅

**목표:** 전체 플로우 검증 및 버그 해결

**작업:**
1. 앱 빌드 및 설치
   ```bash
   ./gradlew :app:installDebug
   ```

2. 에뮬레이터에서 실행
   - 앱 시작
   - Hilt 초기화 확인
   - 로그 검토

3. 네비게이션 플로우 테스트
   ```
   Step 1: Home 화면 열기 → 상품 목록 표시 확인
   Step 2: 상품 클릭 → ProductDetail 화면 이동 확인
   Step 3: "장바구니 담기" 클릭 → Cart 화면 이동 확인
   Step 4: 장바구니 아이템 확인
   ```

4. 각 화면 상태 검증
   - Loading 상태
   - Success 상태
   - Error 상태

5. 뒤로가기 동작 검증
   - 각 화면에서 뒤로가기 버튼 동작
   - 네비게이션 스택 관리

6. 성능 측정
   - App Startup Time 측정 (< 3초)
   - Memory Profiler로 메모리 누수 검사
   - CPU Profiler로 병목 구간 분석

**예상 소요 시간:** 2시간

**성공 기준:**
- ✅ 앱 실행 성공
- ✅ 모든 네비게이션 플로우 동작
- ✅ 성능 기준 충족

---

## ⏱️ 타임라인

| Phase | 작업 | 소요 시간 |
|-------|------|---------|
| 1 | :app 설정 | 30분 |
| 2 | MainActivity + NavigationHost | 1시간 |
| 3 | Navigation Graph 구성 | 2시간 |
| 4 | Hilt 의존성 검증 | 1시간 |
| 5 | 에뮬레이터 테스트 | 2시간 |
| **총계** | | **6-8시간** |

---

## 🛠️ 기술 접근 방식

### Type-safe Navigation

**장점:**
- ✅ 컴파일 시점에 Route 유효성 검사
- ✅ 파라미터 타입 안전
- ✅ 리팩토링 시 자동 감지

**구현:**
- Kotlin Serialization 기반 Route 정의
- @Serializable 어노테이션 사용
- NavGraph에 Route 등록

### Hilt 통합

**원칙:**
- 모든 DI는 Hilt를 통해 관리
- @HiltViewModel, @Inject 사용
- 모듈 간 명확한 의존성

**검증:**
```bash
./gradlew :app:compileDebugKotlin  # Hilt 그래프 생성
./gradlew :app:installDebugAndroidTest  # 설치 및 테스트
```

---

## 📋 의존성 순서

**반드시 완료되어야 할 순서:**
1. ✅ SPEC-ANDROID-INIT-001: Core 모듈
2. ✅ SPEC-ANDROID-MVI-002: MVI 패턴
3. ✅ SPEC-ANDROID-FEATURE-HOME-001: Home 모듈
4. ✅ SPEC-ANDROID-FEATURE-DETAIL-001: Detail 모듈
5. ✅ SPEC-ANDROID-FEATURE-CART-001: Cart 모듈
6. → SPEC-ANDROID-INTEGRATION-003: 통합 (현재)

---

## ✅ 정의된 완료 조건

1. ✅ :app 모듈이 모든 Feature 의존성 포함
2. ✅ MainActivity 작성 완료
3. ✅ NavigationHost 구성 완료
4. ✅ Type-safe Navigation Routes 정의
5. ✅ Hilt 의존성 그래프 컴파일 성공
6. ✅ 에뮬레이터 앱 실행 성공
7. ✅ Home → Detail → Cart 플로우 동작
8. ✅ 모든 네비게이션 동작 검증
9. ✅ 성능 기준 충족 (App Startup < 3초)
10. ✅ Code Review 및 Approval

---

## 🔧 주요 커맨드

```bash
# 빌드
./gradlew :app:build

# 설치
./gradlew :app:installDebug

# Hilt 검증
./gradlew :app:compileDebugKotlin

# 테스트
./gradlew :app:connectedAndroidTest

# 성능 측정
./gradlew :app:release  # Release APK 생성
```

---

**END OF PLAN**
