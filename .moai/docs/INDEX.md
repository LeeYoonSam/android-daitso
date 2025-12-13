# Daitso 문서 인덱스

**최종 업데이트**: 2025-12-13
**프로젝트**: Android MVI Modular
**SPEC**: SPEC-ANDROID-INIT-001
**Phase 상태**: Phase 1-4 진행 중 (Detail Feature 문서화 완료)

---

## 문서 목록

### 핵심 문서

#### 1. 아키텍처 문서
- **[ARCHITECTURE.md](ARCHITECTURE.md)**
  - Clean Architecture 계층 구조
  - MVI 패턴 상세 설명
  - 모듈 의존성 그래프
  - 데이터 흐름 다이어그램
  - Offline-first 전략

#### 2. 설정 가이드
- **[SETUP.md](SETUP.md)**
  - 프로젝트 초기 설정
  - 개발 환경 구성
  - Gradle 설정
  - IDE 설정

### 모듈 문서

각 Core 모듈의 상세 문서입니다.

#### 데이터 모듈

- **[CORE_MODEL_README.md](modules/CORE_MODEL_README.md)** `:core:model`
  - 도메인 모델 정의 (Product, CartItem, User)
  - Kotlin Serialization 지원
  - 모듈 간 의존성

- **[CORE_COMMON_README.md](modules/CORE_COMMON_README.md)** `:core:common`
  - Result<T> 비동기 작업 래퍼
  - @Dispatcher 코루틴 디스패처 주입
  - Logger 로깅 유틸리티
  - 확장 함수

#### UI 및 테마

- **[CORE_DESIGNSYSTEM_README.md](modules/CORE_DESIGNSYSTEM_README.md)** `:core:designsystem`
  - Material Design 3 테마
  - 색상 팔레트 정의
  - 타이포그래피 설정
  - 공통 UI 컴포넌트 (Button, TextField, LoadingIndicator, ErrorView)

#### 네트워크 및 데이터 저장소

- **[CORE_NETWORK_README.md](modules/CORE_NETWORK_README.md)** `:core:network`
  - Retrofit2 REST API 클라이언트
  - OkHttp 인터셉터 설정
  - NetworkDataSource 인터페이스
  - API 호출 패턴
  - 에러 처리 및 Retry 로직

- **[CORE_DATABASE_README.md](modules/CORE_DATABASE_README.md)** `:core:database`
  - Room Database 설정
  - Entity 정의 (CartItemEntity)
  - DAO 작업 (CRUD)
  - 트랜잭션 및 마이그레이션
  - 로컬 데이터 관리

#### 데이터 계층

- **[CORE_DATA_README.md](modules/CORE_DATA_README.md)** `:core:data`
  - Repository 패턴 구현
  - Offline-first 동기화 로직
  - ProductRepository 인터페이스 및 구현
  - DataModule Hilt 바인딩
  - 고급 패턴 (Retry, 캐시 무효화)

---

## Phase 3: 문서화 및 아키텍처

### 새로운 문서

#### 1. 모듈 네비게이션 가이드
- **[modules/INDEX.md](modules/INDEX.md)** (신규)
  - 각 Core 모듈 상세 가이드
  - 모듈 의존성 매트릭스
  - Inter-Module 통신 패턴
  - 개발 워크플로우
  - 문제 해결 가이드

#### 2. 동기화 보고서
- **[../reports/sync-report-2025-11-29.md](../reports/sync-report-2025-11-29.md)** (신규)
  - Phase 3 문서화 현황
  - 품질 지표 (테스트, 코드 스타일, 아키텍처)
  - 구현 하이라이트
  - 검증 체크리스트
  - 다음 단계 계획

---

## Phase 4: MVI UI 아키텍처 및 Feature 모듈

### 새로운 문서

#### 1. Core UI 모듈 가이드
- **[modules/CORE_UI_README.md](modules/CORE_UI_README.md)** (신규)
  - MVI 패턴 상세 설명
  - BaseViewModel 구조 및 사용법
  - UiState/Event/SideEffect 인터페이스
  - Type-safe Navigation (Routes)
  - 사용 예시 및 코드 샘플
  - 테스트 작성 방법
  - 문제 해결 가이드

#### 2. Feature 모듈 가이드

##### Home Feature (:feature:home)
- **[FEATURE_HOME.md](FEATURE_HOME.md)** (v1.0.0)
  - Home Feature 아키텍처 및 화면 구조
  - HomeContract, HomeViewModel, HomeScreen 구현
  - 상품 목록 조회 및 상태 관리
  - Compose UI 컴포넌트 (LazyColumn, ProductCard)
  - 테스트 전략 및 테스트 케이스
  - 네비게이션 및 의존성 주입

- **[HOME_API_REFERENCE.md](HOME_API_REFERENCE.md)** (v1.0.0)
  - HomeUiState API 문서
  - HomeIntent, HomeSideEffect 명세
  - HomeViewModel 메서드
  - HomeScreen Composable 파라미터 문서

##### Cart Feature (:feature:cart)
- **[FEATURE_CART.md](FEATURE_CART.md)** (신규, v1.0.0)
  - Cart Feature 아키텍처 및 기능 설명
  - CartContract, CartViewModel, CartScreen 구현
  - MVI 패턴 적용 상세 분석
  - Room Database 통합
  - Compose UI (CartItemRow, QuantityControl, CartSummary)
  - 장바구니 상태 관리 및 총 가격 계산
  - 테스트 전략 (Unit Test, UI Test)

- **[CART_API_REFERENCE.md](CART_API_REFERENCE.md)** (신규, v1.0.0)
  - CartUiState API 문서
  - CartIntent 모든 Intent 타입 명세
  - CartSideEffect 타입 정의
  - CartViewModel 메서드 API
  - CartScreen Composable 파라미터 문서
  - CartItem 데이터 모델

- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** (신규, v1.0.0)
  - Feature 모듈 개발 패턴 및 체크리스트
  - MVI 패턴 구현 가이드
  - Room Database 통합 방법
  - Compose UI 개발 베스트 프랙티스
  - 테스트 작성 지침 (Unit, Integration, UI)
  - 성능 최적화 고려사항
  - 일반적인 문제 해결

- **[FEATURE_API_INDEX.md](FEATURE_API_INDEX.md)** (신규, v1.0.0)
  - 모든 Feature 모듈 개요 테이블
  - 각 Feature의 주요 기능 요약
  - Feature 간 네비게이션 포인트
  - 의존성 관계 및 통합 포인트

##### Detail Feature (:feature:detail)
- **[FEATURE_DETAIL.md](FEATURE_DETAIL.md)** (신규, v1.0.0, 2025-12-13)
  - Detail Feature 아키텍처 및 상품 상세 화면 구조
  - ProductDetailContract, ProductDetailViewModel, ProductDetailScreen 구현
  - MVI 패턴 상세 분석
  - 수량 선택 및 장바구니 추가 기능
  - Compose UI 컴포넌트 (ProductInfoSection, QuantitySelector, AddToCartButton)
  - 테스트 전략 (Unit Test, Compose UI Test)
  - 네비게이션 및 의존성 주입

- **[DETAIL_API_REFERENCE.md](DETAIL_API_REFERENCE.md)** (신규, v1.0.0, 2025-12-13)
  - ProductDetailUiState API 문서
  - ProductDetailIntent 모든 Intent 타입 명세
  - ProductDetailSideEffect 타입 정의
  - ProductDetailViewModel 메서드 API
  - UI Components 파라미터 문서 (ProductDetailScreen, QuantitySelector, AddToCartButton)
  - CartRepository 인터페이스
  - 네비게이션 경로 및 딥링크 정의

#### 3. 아키텍처 문서 업데이트
- **[ARCHITECTURE.md](ARCHITECTURE.md)** (Phase 4 섹션 추가)
  - Phase 4 MVI UI 아키텍처
  - UI 상태 관리 흐름 다이어그램
  - BaseViewModel 패턴
  - Feature ViewModel 구현 예시
  - Compose 통합 예시
  - 의존성 흐름 및 모듈 구조

### SPEC 문서

#### SPEC-ANDROID-MVI-002 (Phase 4 관련)
- **[../specs/SPEC-ANDROID-MVI-002/plan.md](../specs/SPEC-ANDROID-MVI-002/plan.md)** (v1.0.1)
  - Phase 1~4 구현 완료 현황
  - 테스트 결과 (104줄, 12개 이상)
  - AC 진행률 (3/4 PASSED)

- **[../specs/SPEC-ANDROID-MVI-002/acceptance.md](../specs/SPEC-ANDROID-MVI-002/acceptance.md)** (v1.0.1)
  - AC 검증 결과 상세 분석
  - 각 AC별 PASSED/PENDING 상태
  - 코드 품질 메트릭
  - 최종 서명 및 승인

### SPEC 문서 업데이트

#### SPEC-ANDROID-INIT-001 최종 버전
- **spec.md** (v1.0.2 업데이트)
  - Phase 2 완료 섹션 추가
  - 6개 Core 모듈 상세 설명
  - 기술 스택 검증 테이블
  - 품질 게이트 결과

- **plan.md** (v1.0.1 업데이트)
  - 작업 완료 결과 (13개 작업 모두 ✅)
  - 테스트 범위 및 커버리지
  - 얻은 교훈 5가지

- **acceptance.md** (v1.0.1 업데이트)
  - 수락 기준 검증 결과 (13개 AC 모두 ✅ PASS)
  - 품질 게이트 최종 승인
  - 최종 결과: READY FOR PRODUCTION

---

## 주제별 가이드

### 아키텍처 학습

1. 먼저 **[ARCHITECTURE.md](ARCHITECTURE.md)** 읽기
2. 각 계층의 모듈 문서 읽기:
   - Data Layer: `CORE_DATA_README.md`
   - Domain Layer: `CORE_MODEL_README.md`, `CORE_COMMON_README.md`
   - Presentation Layer: `CORE_DESIGNSYSTEM_README.md`

### 기능 구현

#### API 통신
1. **[CORE_NETWORK_README.md](modules/CORE_NETWORK_README.md)** - NetworkDataSource 구현
2. **[CORE_DATA_README.md](modules/CORE_DATA_README.md)** - Repository에서 활용

#### 로컬 데이터 관리
1. **[CORE_DATABASE_README.md](modules/CORE_DATABASE_README.md)** - Room 설정
2. **[CORE_DATA_README.md](modules/CORE_DATA_README.md)** - Repository 통합

#### UI 개발
1. **[CORE_DESIGNSYSTEM_README.md](modules/CORE_DESIGNSYSTEM_README.md)** - 테마 및 컴포넌트
2. 상위 Feature 모듈에서 구성

### 문제 해결

| 문제 | 관련 문서 |
|------|---------|
| 네트워크 요청 실패 | [CORE_NETWORK_README.md](modules/CORE_NETWORK_README.md) |
| 데이터베이스 에러 | [CORE_DATABASE_README.md](modules/CORE_DATABASE_README.md) |
| UI 상태 관리 | [ARCHITECTURE.md](ARCHITECTURE.md) |
| 의존성 주입 문제 | [CORE_COMMON_README.md](modules/CORE_COMMON_README.md) |
| Offline 동작 | [CORE_DATA_README.md](modules/CORE_DATA_README.md) |

---

## 프로젝트 구조 참고

```
android-mvi-modular/
├── README.md                          # 프로젝트 개요
├── CLAUDE.md                          # MoAI-ADK 실행 가이드
│
├── .moai/
│   ├── specs/
│   │   └── SPEC-ANDROID-INIT-001/
│   │       └── spec.md                # 전체 요구사항 명세
│   │
│   └── docs/
│       ├── INDEX.md                   # 이 파일
│       ├── ARCHITECTURE.md            # 아키텍처 상세
│       ├── SETUP.md                   # 개발 환경 설정
│       │
│       └── modules/
│           ├── CORE_MODEL_README.md
│           ├── CORE_COMMON_README.md
│           ├── CORE_DESIGNSYSTEM_README.md
│           ├── CORE_NETWORK_README.md
│           ├── CORE_DATABASE_README.md
│           └── CORE_DATA_README.md
│
├── core/
│   ├── model/                         # 도메인 모델
│   ├── common/                        # 공통 유틸리티
│   ├── designsystem/                  # UI 시스템
│   ├── network/                       # 네트워크
│   ├── database/                      # 로컬 DB
│   └── data/                          # Repository
│
└── build-logic/
    └── convention/                    # Gradle 플러그인
```

---

## 개발 워크플로우

### 1단계: 환경 설정
- [SETUP.md](SETUP.md) 참고하여 개발 환경 구성

### 2단계: 아키텍처 이해
- [ARCHITECTURE.md](ARCHITECTURE.md) 정독
- 각 모듈 문서 검토

### 3단계: 기능 구현
- 모듈별 가이드 참고
- 테스트 코드 작성 (README 예시 참고)

### 4단계: 코드 리뷰
- SPEC-ANDROID-INIT-001 요구사항 확인
- 모듈 간 의존성 검증

---

## 빠른 참조

### Result<T> 상태
- `Result.Success<T>(data)` - 성공
- `Result.Error(exception)` - 실패
- `Result.Loading<T>(data?)` - 진행 중

### 주요 디렉토리
- **네트워크**: `core/network/`
- **데이터베이스**: `core/database/`
- **저장소**: `core/data/`
- **UI 컴포넌트**: `core/designsystem/`

### 중요 인터페이스
- `ProductRepository` - 데이터 접근
- `NetworkDataSource` - API 통신
- `CartDao` - 로컬 DB 접근

### Hilt 주입 포인트
- `NetworkModule` - Retrofit 설정
- `DatabaseModule` - Room 설정
- `DataModule` - Repository 바인딩

---

## 문서 유지보수

### 문서 업데이트 규칙
1. 코드 변경 → 해당 모듈 문서 업데이트
2. 아키텍처 변경 → `ARCHITECTURE.md` 업데이트
3. 새 모듈 추가 → 모듈 README 작성 및 INDEX 수정

### 최종 업데이트 항목 확인
각 문서 하단의 "최종 업데이트" 섹션 확인

---

## 참고 자료

### 공식 문서
- [Android 공식 문서](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt 의존성 주입](https://dagger.dev/hilt/)
- [Retrofit](https://square.github.io/retrofit/)

### 샘플 프로젝트
- [Now in Android (Google)](https://github.com/android/nowinandroid)
- [Architecture Samples (Google)](https://github.com/android/architecture-samples)

### 관련 SPEC
- [SPEC-ANDROID-INIT-001](../specs/SPEC-ANDROID-INIT-001/spec.md) - 초기 설정 및 Core 모듈
- SPEC-ANDROID-MVI-002 - MVI 아키텍처 (TBD)
- SPEC-ANDROID-INTEGRATION-003 - 통합 및 테스트 (TBD)

---

**노트**: 이 인덱스는 문서가 추가/수정될 때마다 업데이트됩니다.

**최종 업데이트**: 2025-12-13
**유지관리자**: GOOS
**상태**: Active Development (Detail Feature 문서화 완료)
