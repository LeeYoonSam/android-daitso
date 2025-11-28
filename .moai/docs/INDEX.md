# Daitso 문서 인덱스

**최종 업데이트**: 2025-11-28
**프로젝트**: Android MVI Modular
**SPEC**: SPEC-ANDROID-INIT-001

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

**최종 업데이트**: 2025-11-28
**유지관리자**: GOOS
**상태**: Active Development
