# SPEC-ANDROID-INIT-001: 구현 계획 (Implementation Plan)

## TAG BLOCK

```yaml
spec_id: SPEC-ANDROID-INIT-001
version: 1.0.0
type: implementation_plan
created_at: 2025-11-28
updated_at: 2025-11-28
owner: GOOS
```

---

## 📋 구현 개요

본 계획은 Android MVI Modular 프로젝트의 초기 설정 및 Core 모듈 구성을 위한 단계별 구현 가이드입니다. 총 13개의 작업으로 구성되며, 우선순위와 의존성에 따라 순차적으로 진행됩니다.

---

## 🎯 구현 목표

### 주요 목표 (Primary Goals)
1. ✅ **타입 안전한 의존성 관리 시스템 구축** - Version Catalog를 통한 중앙 집중식 라이브러리 관리
2. ✅ **재사용 가능한 Gradle 설정 구현** - Convention Plugin을 통한 모듈 간 일관된 설정
3. ✅ **클린 아키텍처 기반 모듈 분리** - 책임 분리와 의존성 역전 원칙 적용
4. ✅ **Offline-first 데이터 레이어 구축** - Room을 Single Source of Truth로 사용

### 부차 목표 (Secondary Goals)
1. ✅ Git 커밋 컨벤션 준수
2. ✅ 각 모듈별 단위 테스트 작성 (최소 85% 커버리지)
3. ✅ Compose Preview 및 UI 컴포넌트 문서화

---

## 📐 구현 단계

### Phase 1: 프로젝트 초기 설정 (5개 작업)

#### [INIT-001] Android Studio 프로젝트 생성 및 Git 초기화

**우선순위:** 🔴 Critical (최우선)

**작업 내용:**
1. Android Studio에서 'No Activity' 템플릿으로 프로젝트 생성
   - Project Name: `Daitso`
   - Package Name: `com.bup.ys.daitso`
   - Language: Kotlin
   - Build Configuration Language: Kotlin DSL (build.gradle.kts)
   - Minimum SDK: API 26 (Android 8.0 Oreo)
   - Target SDK: API 35 (Android 15)
   - Compile SDK: API 35

2. Git 저장소 초기화:
   ```bash
   git init
   git add .
   git commit -m "chore: initial commit - No Activity template"
   ```

3. `.gitignore` 검증:
   - `build/`, `.gradle/`, `.idea/`, `*.iml`, `local.properties` 포함 확인

**산출물:**
- `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`
- `.gitignore`
- Git 초기 커밋

**검증 기준:**
- ✅ `./gradlew build` 성공
- ✅ Git 커밋 히스토리 확인 (`git log`)

**의존성:** 없음

---

#### [INIT-002] Version Catalog 설정

**우선순위:** 🔴 Critical

**작업 내용:**
1. `gradle/libs.versions.toml` 파일 생성
2. 라이브러리 버전 정의:
   - Kotlin 2.1.0
   - AGP 8.7.3
   - Hilt 2.54
   - Compose BOM 2024.12.01
   - Retrofit 2.11.0
   - Room 2.6.1
   - Coil 2.7.0
   - Kotlin Serialization 1.7.3
   - Coroutines 1.9.0
   - KSP 2.1.0-1.0.29

3. 라이브러리 정의:
   - `[libraries]` 섹션에 모든 의존성 정의
   - `[plugins]` 섹션에 플러그인 정의

4. 프로젝트 루트 `build.gradle.kts`에서 Version Catalog 적용:
   ```kotlin
   plugins {
       alias(libs.plugins.android.application) apply false
       alias(libs.plugins.kotlin.android) apply false
       alias(libs.plugins.hilt) apply false
       alias(libs.plugins.ksp) apply false
   }
   ```

**산출물:**
- `gradle/libs.versions.toml`
- 업데이트된 `build.gradle.kts`

**검증 기준:**
- ✅ Gradle Sync 성공
- ✅ `libs.<name>` 형태로 의존성 접근 가능
- ✅ `./gradlew dependencies` 실행 시 모든 의존성 해결

**의존성:** [INIT-001] 완료 후

---

#### [INIT-003] Convention Plugin 구현

**우선순위:** 🟠 High

**작업 내용:**
1. `build-logic/convention` 모듈 생성:
   ```
   build-logic/
   ├── settings.gradle.kts
   ├── convention/
   │   ├── build.gradle.kts
   │   └── src/main/kotlin/
   │       ├── AndroidApplicationConventionPlugin.kt
   │       ├── AndroidLibraryConventionPlugin.kt
   │       ├── AndroidHiltConventionPlugin.kt
   │       ├── AndroidLibraryComposeConventionPlugin.kt
   │       ├── KotlinJvmConventionPlugin.kt
   │       └── AndroidFeatureConventionPlugin.kt (선택)
   ```

2. 각 플러그인 구현:
   - **AndroidApplicationConventionPlugin**:
     ```kotlin
     class AndroidApplicationConventionPlugin : Plugin<Project> {
         override fun apply(target: Project) {
             with(target) {
                 with(pluginManager) {
                     apply("com.android.application")
                     apply("org.jetbrains.kotlin.android")
                 }

                 extensions.configure<ApplicationExtension> {
                     compileSdk = 35
                     defaultConfig {
                         minSdk = 26
                         targetSdk = 35
                     }
                     compileOptions {
                         sourceCompatibility = JavaVersion.VERSION_17
                         targetCompatibility = JavaVersion.VERSION_17
                     }
                     kotlinOptions {
                         jvmTarget = "17"
                     }
                 }
             }
         }
     }
     ```
   - **AndroidLibraryConventionPlugin**: 유사하게 Library 설정
   - **AndroidHiltConventionPlugin**: Hilt + KSP 설정
   - **AndroidLibraryComposeConventionPlugin**: Compose Compiler 설정

3. `build-logic/convention/build.gradle.kts` 설정:
   ```kotlin
   plugins {
       `kotlin-dsl`
   }

   gradlePlugin {
       plugins {
           register("androidApplication") {
               id = "daitso.android.application"
               implementationClass = "AndroidApplicationConventionPlugin"
           }
           register("androidLibrary") {
               id = "daitso.android.library"
               implementationClass = "AndroidLibraryConventionPlugin"
           }
           register("androidHilt") {
               id = "daitso.android.hilt"
               implementationClass = "AndroidHiltConventionPlugin"
           }
           register("androidLibraryCompose") {
               id = "daitso.android.library.compose"
               implementationClass = "AndroidLibraryComposeConventionPlugin"
           }
           register("kotlinJvm") {
               id = "daitso.kotlin.jvm"
               implementationClass = "KotlinJvmConventionPlugin"
           }
       }
   }
   ```

**산출물:**
- `build-logic/` 디렉토리
- 5개의 Convention Plugin 클래스
- `build-logic/convention/build.gradle.kts`

**검증 기준:**
- ✅ `./gradlew :build-logic:convention:build` 성공
- ✅ 플러그인 ID가 올바르게 등록됨

**의존성:** [INIT-002] 완료 후

---

#### [INIT-004] settings.gradle.kts 설정

**우선순위:** 🟠 High

**작업 내용:**
1. 프로젝트 루트 `settings.gradle.kts` 수정:
   ```kotlin
   pluginManagement {
       includeBuild("build-logic")
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
       }
   }

   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           google()
           mavenCentral()
       }
   }

   rootProject.name = "Daitso"
   include(":app")
   include(":core:model")
   include(":core:common")
   include(":core:designsystem")
   include(":core:network")
   include(":core:database")
   include(":core:data")
   ```

2. Gradle Sync 실행

**산출물:**
- 업데이트된 `settings.gradle.kts`

**검증 기준:**
- ✅ Gradle Sync 성공
- ✅ `build-logic`이 포함되어 플러그인 사용 가능

**의존성:** [INIT-003] 완료 후

---

#### [INIT-005] Hilt 설정 및 Application 클래스 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `app/build.gradle.kts`에 Convention Plugin 적용:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.application)
       alias(libs.plugins.daitso.android.hilt)
   }

   dependencies {
       implementation(libs.androidx.core.ktx)
       implementation(libs.androidx.lifecycle.runtime.ktx)
   }
   ```

2. `DaitsoApplication.kt` 생성:
   ```kotlin
   package com.bup.ys.daitso

   import android.app.Application
   import dagger.hilt.android.HiltAndroidApp

   @HiltAndroidApp
   class DaitsoApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           // 초기화 로직 (선택)
       }
   }
   ```

3. `AndroidManifest.xml` 수정:
   ```xml
   <manifest xmlns:android="http://schemas.android.com/apk/res/android">
       <application
           android:name=".DaitsoApplication"
           android:allowBackup="true"
           android:icon="@mipmap/ic_launcher"
           android:label="@string/app_name"
           android:roundIcon="@mipmap/ic_launcher_round"
           android:supportsRtl="true"
           android:theme="@style/Theme.Daitso">
       </application>
   </manifest>
   ```

**산출물:**
- `DaitsoApplication.kt`
- 업데이트된 `AndroidManifest.xml`

**검증 기준:**
- ✅ `./gradlew :app:build` 성공
- ✅ Hilt 의존성 그래프 생성 성공

**의존성:** [INIT-004] 완료 후

---

### Phase 2: Core 모듈 구성 (5개 작업)

#### [CORE-001] :core:model 모듈 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `core/model/` 디렉토리 생성
2. `core/model/build.gradle.kts` 작성:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.kotlin.jvm)
       alias(libs.plugins.kotlin.serialization)
   }

   dependencies {
       implementation(libs.kotlinx.serialization.json)
   }
   ```

3. 데이터 클래스 정의:
   - `Product.kt`
   - `CartItem.kt`
   - `User.kt`

**산출물:**
- `core/model/build.gradle.kts`
- 3개의 데이터 클래스

**검증 기준:**
- ✅ 모듈 빌드 성공 (Android 의존성 없음)
- ✅ Kotlin Serialization 정상 작동

**의존성:** [INIT-005] 완료 후

---

#### [CORE-002] :core:common 모듈 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `core/common/` 디렉토리 생성
2. `core/common/build.gradle.kts` 작성 (순수 Kotlin 모듈)
3. 유틸리티 클래스 구현:
   - `Result.kt` - Success/Error/Loading Wrapper
   - `Dispatcher.kt` - Annotation 및 Enum
   - `Logger.kt` - 로깅 유틸리티

**산출물:**
- `core/common/build.gradle.kts`
- 3개의 유틸리티 클래스

**검증 기준:**
- ✅ 모듈 빌드 성공
- ✅ Result 클래스 타입 안전성 검증

**의존성:** [INIT-005] 완료 후

---

#### [CORE-003] :core:designsystem 모듈 생성

**우선순위:** 🟡 Medium

**작업 내용:**
1. `core/designsystem/` 디렉토리 생성
2. `core/designsystem/build.gradle.kts` 작성:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.library)
       alias(libs.plugins.daitso.android.library.compose)
   }

   dependencies {
       implementation(libs.androidx.compose.ui)
       implementation(libs.androidx.compose.material3)
       debugImplementation(libs.androidx.compose.ui.tooling)
       implementation(libs.androidx.compose.ui.tooling.preview)
   }
   ```

3. 테마 및 컴포넌트 구현:
   - `theme/Color.kt`
   - `theme/Typography.kt`
   - `theme/Shape.kt`
   - `theme/DaitsoTheme.kt`
   - `components/DaitsoButton.kt`
   - `components/DaitsoTextField.kt`
   - `components/DaitsoLoadingIndicator.kt`
   - `components/DaitsoErrorView.kt`

**산출물:**
- `core/designsystem/build.gradle.kts`
- Material3 테마 및 4개의 공통 컴포넌트

**검증 기준:**
- ✅ Compose Preview 렌더링 성공
- ✅ 모든 컴포넌트가 DaitsoTheme 적용

**의존성:** [INIT-005] 완료 후

---

#### [CORE-004] :core:network 모듈 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `core/network/` 디렉토리 생성
2. `core/network/build.gradle.kts` 작성:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.library)
       alias(libs.plugins.daitso.android.hilt)
       alias(libs.plugins.kotlin.serialization)
   }

   dependencies {
       implementation(project(":core:model"))
       implementation(libs.retrofit)
       implementation(libs.retrofit.kotlin.serialization)
       implementation(libs.okhttp)
       implementation(libs.okhttp.logging.interceptor)
       implementation(libs.kotlinx.serialization.json)
   }
   ```

3. 구현:
   - `NetworkDataSource.kt` - 인터페이스
   - `NetworkModule.kt` - Hilt 모듈 (Retrofit, OkHttp 제공)

**산출물:**
- `core/network/build.gradle.kts`
- NetworkDataSource 인터페이스
- NetworkModule

**검증 기준:**
- ✅ Hilt가 Retrofit 인스턴스 정상 주입
- ✅ Mock 서버를 사용한 API 호출 테스트 성공

**의존성:** [CORE-001] 완료 후

---

#### [CORE-005] :core:database 모듈 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `core/database/` 디렉토리 생성
2. `core/database/build.gradle.kts` 작성:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.library)
       alias(libs.plugins.daitso.android.hilt)
       alias(libs.plugins.ksp)
   }

   dependencies {
       implementation(project(":core:model"))
       implementation(libs.room.runtime)
       implementation(libs.room.ktx)
       ksp(libs.room.compiler)
   }
   ```

3. 구현:
   - `entity/CartItemEntity.kt`
   - `dao/CartDao.kt`
   - `DaitsoDatabase.kt`
   - `DatabaseModule.kt` - Hilt 모듈

**산출물:**
- `core/database/build.gradle.kts`
- Entity, DAO, Database 클래스
- DatabaseModule

**검증 기준:**
- ✅ Room 스키마 생성 성공
- ✅ CRUD 작업 단위 테스트 성공

**의존성:** [CORE-001] 완료 후

---

### Phase 3: Data Layer 구성 (3개 작업)

#### [DATA-001] :core:data 모듈 생성

**우선순위:** 🟠 High

**작업 내용:**
1. `core/data/` 디렉토리 생성
2. `core/data/build.gradle.kts` 작성:
   ```kotlin
   plugins {
       alias(libs.plugins.daitso.android.library)
       alias(libs.plugins.daitso.android.hilt)
   }

   dependencies {
       implementation(project(":core:model"))
       implementation(project(":core:common"))
       implementation(project(":core:network"))
       implementation(project(":core:database"))

       implementation(libs.kotlinx.coroutines.core)
       implementation(libs.kotlinx.coroutines.android)
   }
   ```

**산출물:**
- `core/data/build.gradle.kts`

**검증 기준:**
- ✅ 모듈 빌드 성공
- ✅ 모든 Core 모듈 의존성 정상 해결

**의존성:** [CORE-004], [CORE-005] 완료 후

---

#### [DATA-002] Repository 인터페이스 및 구현체 작성

**우선순위:** 🔴 Critical

**작업 내용:**
1. `repository/ProductRepository.kt` - 인터페이스 정의
2. `repository/ProductRepositoryImpl.kt` - Offline-first 구현:
   - Room에서 로컬 데이터 먼저 방출
   - 네트워크에서 최신 데이터 가져와 Room 업데이트
   - Flow를 사용하여 여러 Result 상태 방출

3. `LocalDataSource.kt` 및 `RemoteDataSource.kt` (선택적 추상화)

**산출물:**
- ProductRepository 인터페이스 및 구현체
- LocalDataSource, RemoteDataSource (선택)

**검증 기준:**
- ✅ Offline 상태에서 로컬 데이터 반환
- ✅ Online 상태에서 네트워크 데이터 동기화
- ✅ Flow가 Loading → Success(로컬) → Success(네트워크) 순으로 방출

**의존성:** [DATA-001] 완료 후

---

#### [DATA-003] DataModule 작성

**우선순위:** 🟠 High

**작업 내용:**
1. `di/DataModule.kt` 작성:
   - `@Binds`로 Repository 인터페이스 바인딩
   - `@Provides`로 Dispatcher 제공
   - `@Singleton` 스코프 적용

**산출물:**
- `di/DataModule.kt`

**검증 기준:**
- ✅ Hilt가 Repository 정상 주입
- ✅ Dispatcher Annotation 정상 작동
- ✅ 의존성 역전 원칙(DIP) 준수

**의존성:** [DATA-002] 완료 후

---

## 🔧 기술적 고려사항

### 1. Gradle 빌드 성능 최적화

**설정 항목:**
- `gradle.properties`에 다음 추가:
  ```properties
  org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC
  org.gradle.caching=true
  org.gradle.parallel=true
  org.gradle.configureondemand=true
  android.useAndroidX=true
  android.enableJetifier=false
  kotlin.code.style=official
  ```

### 2. KSP vs Kapt

**선택:** KSP 사용
- **이유:** Kapt보다 2배 이상 빌드 속도 향상
- **적용:** Hilt, Room 모두 KSP 사용
- **주의:** KSP 1.0.29는 Kotlin 2.1.0과 호환

### 3. ProGuard/R8 설정

**초기 단계에서는 생략**, 릴리스 빌드 시 추가:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 4. 테스트 전략

**각 모듈별 테스트:**
- **:core:model** - 데이터 클래스 직렬화 테스트
- **:core:common** - Result Wrapper 상태 전환 테스트
- **:core:network** - Mock 서버를 사용한 API 호출 테스트
- **:core:database** - Room DAO CRUD 테스트 (Robolectric 또는 InMemory DB)
- **:core:data** - Repository Offline-first 동작 테스트

**테스트 라이브러리:**
- JUnit 5
- MockK
- Turbine (Flow 테스트)
- Robolectric (Android 의존성 테스트)

---

## ⚠️ 위험 요소 및 대응 방안

### 위험 1: Convention Plugin 초기 학습 곡선

**영향도:** Medium
**대응:**
- Google의 Now in Android 샘플 참고
- 단순한 플러그인부터 점진적 확장
- 문서화 및 주석 작성 철저히

### 위험 2: Hilt 의존성 그래프 순환 참조

**영향도:** High
**대응:**
- 각 모듈의 Hilt Module을 명확히 분리
- `@InstallIn` 스코프를 정확히 지정
- 순환 참조 발생 시 인터페이스로 의존성 역전

### 위험 3: Room 마이그레이션 전략 부재

**영향도:** Medium
**대응:**
- 초기 버전(v1)부터 마이그레이션 계획 수립
- `autoMigrations` 고려
- 스키마 버전 관리 철저히

### 위험 4: Compose Compiler 버전 불일치

**영향도:** Low
**대응:**
- Compose BOM 사용으로 버전 통합 관리
- Kotlin 버전과 Compose Compiler 호환성 확인

---

## 📊 완료 기준 (Definition of Done)

**모든 작업 완료 시 다음 조건을 충족해야 함:**

1. ✅ **빌드 성공:**
   - `./gradlew build` 실행 시 모든 모듈 빌드 성공
   - Warning 없이 깨끗한 빌드

2. ✅ **Gradle Sync 성공:**
   - Android Studio에서 Gradle Sync 에러 없음
   - Version Catalog 모든 의존성 해결

3. ✅ **Hilt 의존성 그래프 생성:**
   - `./gradlew :app:kspDebugKotlin` 성공
   - DaitsoApplication에서 Hilt 초기화 성공

4. ✅ **모듈 의존성 검증:**
   - `:core:data`가 다른 Core 모듈 의존성 정상 해결
   - 순환 참조 없음

5. ✅ **테스트 커버리지:**
   - 각 Core 모듈 단위 테스트 85% 이상

6. ✅ **Git 커밋 히스토리:**
   - Conventional Commits 준수
   - 각 Phase별로 커밋 분리

7. ✅ **문서화:**
   - 각 모듈의 README.md 작성 (선택)
   - Compose 컴포넌트 Preview 주석 작성

---

## 🔗 관련 리소스

**참고 문서:**
- [SPEC-ANDROID-INIT-001/spec.md](./spec.md) - 상세 요구사항
- [SPEC-ANDROID-INIT-001/acceptance.md](./acceptance.md) - 수락 기준

**다음 단계:**
- `SPEC-ANDROID-MVI-002` - MVI 아키텍처 및 Feature 모듈 구성
- `/moai:2-run SPEC-ANDROID-INIT-001` - TDD 구현 시작

---

**END OF IMPLEMENTATION PLAN**
