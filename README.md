# Daitso - Android E-Commerce Application

**상태**: Active Development
**최신 업데이트**: 2025-12-14 (Cart 모듈 완료)
**테스트 커버리지**: 95%+

---

## 개요

**Daitso**는 Clean Architecture와 MVI 패턴을 기반으로 구축된 현대적인 안드로이드 이커머스 애플리케이션입니다.

### 주요 특징

- **Clean Architecture**: 계층 분리를 통한 유지보수성 향상
- **MVI 패턴**: 단방향 데이터 흐름으로 상태 관리 단순화
- **Modular Design**: Feature별 독립적 모듈로 확장성 극대화
- **Offline-first**: Room Database를 Single Source of Truth로 사용
- **Type-safe Navigation**: Kotlin Serialization 기반 네비게이션
- **Jetpack Compose**: 선언형 UI로 개발 생산성 향상
- **테스트 중심**: 높은 테스트 커버리지로 품질 보장

---

## 기술 스택

### Android Framework
| 컴포넌트 | 버전 | 용도 |
|---------|------|------|
| **Android Studio** | 2024.1.2+ | 개발 환경 |
| **Kotlin** | 2.1.0 | 프로그래밍 언어 |
| **AGP** | 8.7.3 | Android Gradle Plugin |
| **Gradle** | 8.11.1 | 빌드 시스템 |

### Target Environment
| 항목 | 값 |
|------|-----|
| **minSdk** | 26 |
| **targetSdk** | 35 |
| **compileSdk** | 35 |

### 주요 라이브러리
| 라이브러리 | 버전 | 용도 |
|----------|------|------|
| **Jetpack Compose** | 1.7.5 | UI 프레임워크 |
| **Material3** | 최신 | 디자인 시스템 |
| **Room Database** | 2.6.1 | 로컬 데이터 저장소 |
| **Coroutines** | 1.9.0 | 비동기 프로그래밍 |
| **Flow** | 1.9.0 | 반응형 데이터 흐름 |
| **Retrofit2** | 2.11.0 | REST API 통신 |
| **Hilt** | 2.54 | 의존성 주입 |
| **Jetpack Lifecycle** | 최신 | ViewModel, LiveData |
| **Jetpack Navigation** | 2.8.0 | 화면 네비게이션 |

---

## 프로젝트 구조

```
Daitso/
├── app/                          # 메인 애플리케이션
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── core/                         # 공통 Core 모듈들
│   ├── model/                   # 도메인 모델
│   ├── common/                  # 공통 유틸리티
│   ├── designsystem/            # UI 테마 및 컴포넌트
│   ├── network/                 # API 통신
│   ├── database/                # Room Database
│   ├── data/                    # Repository 구현
│   └── ui/                      # MVI 기본 구조
│
├── feature/                     # Feature 모듈들
│   ├── cart/                    # 장바구니 (✅ 완료)
│   ├── detail/                  # 상품 상세 (✅ 완료)
│   ├── home/                    # 홈 화면 (✅ 완료)
│   └── ...
│
├── .moai/                       # MOAI 프레임워크
│   ├── specs/                   # SPEC 문서
│   ├── docs/                    # 생성 문서
│   └── config.json
│
├── docs/                        # 프로젝트 문서
│   ├── MODULES.md              # 모듈 구조 가이드
│   ├── ARCHITECTURE.md         # 아키텍처 설계
│   └── ...
│
├── gradle/                      # Gradle 관련
├── build-logic/                # 빌드 로직
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 모듈 설명

### Core 모듈

#### :core:model
도메인 모델 정의
- `Product` - 상품 정보
- `CartItem` - 장바구니 아이템
- `User` - 사용자 정보

#### :core:common
공통 유틸리티
- `Result<T>` - 비동기 작업 결과 래퍼
- `@Dispatcher` - Coroutine Dispatcher 주입

#### :core:designsystem
UI 디자인 시스템
- `DaitsoTheme` - Material3 테마
- 커스텀 컴포넌트들

#### :core:network
REST API 통신
- `DaitsoApiService` - Retrofit 인터페이스
- `NetworkDataSource` - 네트워크 데이터 소스

#### :core:database
로컬 데이터 저장소
- `DaitsoDatabase` - Room Database
- `CartDao` - Cart 접근 객체
- `CartItemEntity` - Room 엔티티

#### :core:data
Repository 패턴
- `ProductRepository` - 인터페이스
- `ProductRepositoryImpl` - 구현

#### :core:ui
MVI 아키텍처 기초
- `BaseViewModel<S, E, SE>` - MVI 기본 클래스
- `UiState/Event/SideEffect` - 마커 인터페이스
- `AppRoute` - Type-safe Navigation

### Feature 모듈

#### :feature:cart (✅ 완료)
**상태**: Completed (2025-12-14)
**SPEC**: SPEC-ANDROID-FEATURE-CART-001

장바구니 관리 기능:
- 아이템 조회, 추가, 수정, 삭제
- 총 가격 자동 계산
- Room DB 통합
- MVI 아키텍처 적용

```
cart/
├── contract/
│   └── CartContract.kt       # State, Intent, SideEffect
├── presentation/
│   ├── CartScreen.kt         # UI 컴포넌트
│   └── CartViewModel.kt      # 상태 관리
├── repository/
│   └── CartRepositoryImpl.kt  # Repository 구현
├── domain/
│   └── CartRepository.kt     # Repository 인터페이스
├── navigation/
│   └── CartNavigation.kt     # Navigation 설정
├── di/
│   └── CartModule.kt         # Hilt 모듈
├── util/
│   └── PriceFormatter.kt     # 가격 포맷팅
└── README.md                 # 상세 가이드
```

**문서**: [cart/README.md](./feature/cart/README.md)

#### :feature:detail (✅ 완료)
**상태**: Completed (2025-12-13)
**SPEC**: SPEC-ANDROID-FEATURE-DETAIL-001

상품 상세 정보:
- 상품 정보 조회
- 이미지 갤러리
- 리뷰 및 평점
- 장바구니 추가

#### :feature:home (✅ 완료)
**상태**: Completed (2025-12-13)
**SPEC**: SPEC-ANDROID-FEATURE-HOME-001

홈 화면:
- 상품 목록 표시
- 검색 및 필터
- 추천 상품

---

## 빌드 및 실행

### 필수 요구사항
- Android Studio 2024.1.2 이상
- JDK 17 이상
- Gradle 8.11.1 이상

### 빌드

```bash
# 전체 프로젝트 빌드
./gradlew build

# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease
```

### 실행

```bash
# 에뮬레이터 또는 디바이스에서 실행
./gradlew installDebug

# 특정 모듈만 빌드
./gradlew :feature:cart:build
```

---

## 테스트

### 단위 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :feature:cart:test

# 특정 테스트 클래스
./gradlew :feature:cart:test --tests "*CartViewModelTest"
```

### 통합 테스트

```bash
# Android 디바이스 테스트
./gradlew connectedAndroidTest

# 특정 모듈의 Android 테스트
./gradlew :feature:cart:connectedAndroidTest
```

### 커버리지 리포트

```bash
# Jacoco 커버리지 생성
./gradlew jacocoTestReport

# 커버리지 리포트 확인
# build/reports/jacoco/jacocoTestReport/html/index.html
```

---

## 아키텍처 개요

### MVI 패턴

```
User Action (클릭, 입력)
       ↓
   Intent (사용자 의도)
       ↓
  ViewModel (비즈니스 로직)
       ↓
  UiState (UI 상태)
       ↓
    Compose (UI 렌더링)
```

### 데이터 흐름 (Offline-first)

```
View/ViewModel
     ↓
Repository
   ↙   ↖
Room   Retrofit
(DB)   (API)
```

### 모듈 의존성

```
app
├── :feature:cart
├── :feature:detail
├── :feature:home
└── :core:*
    ├── :core:ui
    ├── :core:data
    ├── :core:database
    ├── :core:network
    ├── :core:designsystem
    ├── :core:common
    └── :core:model
```

---

## 주요 파일

| 파일 | 용도 |
|------|------|
| **README.md** | 프로젝트 개요 (현재 파일) |
| **docs/ARCHITECTURE.md** | 전체 아키텍처 설계 |
| **docs/MODULES.md** | 모듈 구조 가이드 |
| **feature/cart/README.md** | Cart 모듈 상세 가이드 |
| **.moai/specs/** | 기능 정의 SPEC 문서 |

---

## 개발 워크플로우

### 1. 기능 계획
1. SPEC 문서 작성 (`.moai/specs/SPEC-*.md`)
2. 요구사항, 수용 기준 정의

### 2. 구현
1. Feature 모듈 구조 생성
2. MVI 계약 정의 (State, Intent, SideEffect)
3. ViewModel 구현
4. UI 구현
5. Repository/DataSource 구현

### 3. 테스트
1. 단위 테스트 작성
2. 통합 테스트 작성
3. 커버리지 확인 (95%+ 목표)

### 4. 문서화
1. API 문서 작성
2. 사용 예제 작성
3. 통합 가이드 작성

### 5. 동기화
1. 코드 변경사항 확인
2. 문서 업데이트
3. SPEC 상태 업데이트

---

## 성능 최적화

### Database 쿼리 최적화
- 필요한 컬럼만 선택
- 인덱스 활용
- 페이지네이션 구현

### UI 렌더링 최적화
- LazyColumn 사용
- State hoisting
- Compose 리컴포지션 최소화

### 네트워크 최적화
- OkHttp 캐싱
- 이미지 캐싱 (Coil)
- 요청 배칭

---

## 문제 해결

### 빌드 오류

```bash
# Gradle 캐시 초기화
./gradlew clean build

# Kotlin 캐시 초기화
./gradlew --stop
rm -rf .gradle/
./gradlew build
```

### 테스트 실패

```bash
# 테스트 로그 확인
./gradlew test --info

# 특정 테스트 디버그
./gradlew :feature:cart:test --debug-tests
```

### Room Database 마이그레이션

```bash
# 스키마 검증
./gradlew :core:database:build --info

# 마이그레이션 생성
# core/database/src/main/kotlin/com/bup/ys/daitso/core/database/migration/
```

---

## 기여 가이드

### 코드 스타일

```bash
# Kotlin Lint 검사
./gradlew ktlint

# 자동 포맷팅
./gradlew ktlintFormat
```

### Commit 메시지 규칙

```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드 또는 의존성 수정
```

예시:
```
feat(cart): add item removal with undo functionality
```

### 브랜치 전략

```
main (프로덕션)
└── develop (개발)
    ├── feature/cart-001
    ├── feature/detail-001
    └── fix/database-migration
```

---

## 배포

### 릴리즈 준비

```bash
# Version 업데이트
./gradlew versionBump --type=minor

# Release 빌드
./gradlew assembleRelease

# 사인 (선택사항)
./gradlew bundleRelease -PsigningKeystore=/path/to/keystore
```

### Play Store 배포

1. 버전 업데이트
2. Release Notes 작성
3. Signed APK/AAB 생성
4. Play Console에 업로드

---

## 참고 자료

### 공식 문서
- [Android Jetpack 가이드](https://developer.android.com/jetpack)
- [Jetpack Compose 문서](https://developer.android.com/jetpack/compose)
- [Room Database 가이드](https://developer.android.com/training/data-storage/room)
- [Hilt 의존성 주입](https://dagger.dev/hilt/)

### 아키텍처 참고
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [MVI 패턴](https://medium.com/@shelajev/clean-architecture-with-kotlin-coroutines-and-mvvm-part-1-7d0ce2013d8f)

### 프로젝트 문서
- [전체 아키텍처 설계](./docs/ARCHITECTURE.md)
- [모듈 구조 가이드](./docs/MODULES.md)
- [Cart 모듈 가이드](./feature/cart/README.md)

---

## 라이선스

MIT License - 자세한 내용은 LICENSE 파일을 참고하세요.

---

## 연락처

- **문제 보고**: [GitHub Issues](https://github.com/your-repo/issues)
- **토론**: [GitHub Discussions](https://github.com/your-repo/discussions)
- **Email**: dev@daitso.com

---

## 프로젝트 상태

| 항목 | 상태 |
|------|------|
| **프로젝트 단계** | Active Development |
| **현재 버전** | 1.0.0-beta |
| **마지막 업데이트** | 2025-12-14 |
| **주요 기능** | Cart, Detail, Home 완료 |
| **테스트 커버리지** | 95%+ |
| **문서화** | 완료 (Cart, Detail, Home) |

---

**README 버전**: 1.0.0
**최종 검토**: 2025-12-14
**작성자**: Doc Syncer
**상태**: Complete
