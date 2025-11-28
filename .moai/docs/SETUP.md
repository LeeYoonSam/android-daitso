# Daitso 개발 환경 설정 가이드

**SPEC**: SPEC-ANDROID-INIT-001
**최종 업데이트**: 2025-11-28
**작성자**: GOOS

---

## 목차

- [시스템 요구사항](#시스템-요구사항)
- [초기 설정](#초기-설정)
- [Android Studio 설정](#android-studio-설정)
- [Gradle 설정](#gradle-설정)
- [KSP (Kotlin Symbol Processing) 설정](#ksp-kotlin-symbol-processing-설정)
- [개발 환경 검증](#개발-환경-검증)
- [빌드 및 실행](#빌드-및-실행)
- [문제 해결](#문제-해결)
- [IDE 플러그인 (선택)](#ide-플러그인-선택)

---

## 시스템 요구사항

### 필수 요구사항

| 요구사항 | 최소 버전 | 권장 버전 | 설명 |
| --- | --- | --- | --- |
| **Java Development Kit (JDK)** | 17 | 21 | Kotlin 2.1.0 호환성 필요 |
| **Android Studio** | 2024.1 Hedgehog | 2025.1 Koala | 최신 Compose 지원 |
| **Gradle** | 8.6 | 8.9 | Build tools 호환성 |
| **Android SDK compileSdk** | 35 | 35 | Android 15 대응 |
| **Git** | 2.20 | 최신 | 버전 관리 |
| **인터넷 연결** | - | 필수 | Gradle 의존성 다운로드 |

### 선택사항

- **Docker**: CI/CD 파이프라인 테스트 (선택)
- **Fastlane**: 자동 배포 (선택)
- **Sonatype Nexus**: 프라이빗 라이브러리 저장소 (선택)

---

## 초기 설정

### 1단계: JDK 설치 및 설정

#### macOS (Homebrew)

```bash
# JDK 21 설치
brew install openjdk@21

# 환경 변수 설정
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc

# 확인
java -version
```

#### Windows

```powershell
# Chocolatey를 사용한 설치
choco install openjdk21

# 환경 변수 설정 (수동)
# 1. Win + Pause/Break → 고급 시스템 설정
# 2. 환경 변수 클릭
# 3. JAVA_HOME = C:\Program Files\OpenJDK\jdk-21

# 확인
java -version
```

#### Linux (Ubuntu/Debian)

```bash
# JDK 설치
sudo apt update
sudo apt install openjdk-21-jdk

# 확인
java -version
```

### 2단계: Git 설정

```bash
# Git 설치 확인
git --version

# 사용자 정보 설정
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# SSH 키 설정 (GitHub 추천)
ssh-keygen -t ed25519 -C "your.email@example.com"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# GitHub에 공개 키 등록
cat ~/.ssh/id_ed25519.pub  # 출력된 내용을 GitHub에 추가
```

### 3단계: 저장소 클론

```bash
# SSH를 사용한 클론 (권장)
git clone git@github.com:your-org/android-mvi-modular.git
cd android-mvi-modular

# HTTPS를 사용한 클론 (SSH 설정 전)
git clone https://github.com/your-org/android-mvi-modular.git
cd android-mvi-modular

# 저장소 구조 확인
ls -la
```

---

## Android Studio 설정

### 1단계: Android Studio 설치

#### 다운로드

- [Android Studio 공식 사이트](https://developer.android.com/studio)에서 최신 버전 다운로드
- 최소 버전: **2024.1 Hedgehog**
- 권장 버전: **2025.1 Koala** (최신)

#### 설치 후 초기 설정

1. **Android Studio 실행**
2. **Welcome Screen에서:**
   - **SDK Manager** 클릭
   - **SDK Platforms 탭**:
     - API Level 35 (Android 15) 설치 (compileSdk용)
     - API Level 26 (Android 8.0) 설치 (minSdk용)
   - **SDK Tools 탭**:
     - Android Gradle Plugin 8.7.3 이상 설치
     - Build Tools 설치
     - Android Emulator 설치

### 2단계: JDK 설정

1. **File → Project Structure**
2. **SDK Location** 탭:
   - **JDK location**: `/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home` (macOS)
   - 또는 자동 감지: **JDK location detected**
3. **Apply → OK** 클릭

### 3단계: Gradle 설정

1. **File → Settings** (또는 **Preferences** on macOS)
2. **Build, Execution, Deployment → Gradle**:
   - **Gradle JDK**: 위에서 설정한 JDK 17+
   - **Gradle User Home**: 기본값 유지 또는 커스텀 경로
3. **Apply → OK** 클릭

### 4단계: 플러그인 설정

1. **File → Settings → Plugins**:
   - **Marketplace 탭 검색**:
     - "Kotlin" → Kotlin 플러그인 확인 (기본 포함)
     - "Compose" → Jetpack Compose IDE Support 설치
     - "Detekt" → 코드 분석 (선택)

---

## Gradle 설정

### 1단계: Gradle Wrapper 확인

```bash
# Gradle 버전 확인
./gradlew --version

# 예상 출력:
# ----
# Gradle 8.6 or higher
# ----
```

### 2단계: gradle.properties 설정

파일: `/Users/leeyoonsam/Documents/android-mvi-modular/gradle.properties`

```properties
# JVM 설정
org.gradle.jvmargs=-Xmx4096m -XX:+UseG1GC

# Gradle 성능 최적화
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.workers.max=8

# Android Gradle Plugin 설정
android.useNewApkCreator=true
android.buildCacheDir=.gradle/build-cache

# Kotlin 설정
kotlin.code.style=official

# 빌드 속도 향상
android.enableJetifier=true
android.useAndroidX=true
```

### 3단계: Version Catalog 확인

파일: `/Users/leeyoonsam/Documents/android-mvi-modular/gradle/libs.versions.toml`

주요 버전:
- Kotlin: 2.1.0
- AGP: 8.7.3
- compileSdk: 35
- minSdk: 26

---

## KSP (Kotlin Symbol Processing) 설정

### 개요

KSP는 Hilt와 Room이 코드 생성을 위해 필요한 도구입니다.

**주의**: KAPT(Kotlin Annotation Processing Tool) 대신 KSP 사용 (더 빠름)

### 1단계: KSP 플러그인 확인

**build-logic/convention/build.gradle.kts**:

```kotlin
plugins {
    `kotlin-dsl`
}

// Convention Plugin들이 KSP를 적용
```

**build-logic/convention/src/main/kotlin/AndroidHiltConventionPlugin.kt**:

```kotlin
with(pluginManager) {
    apply("com.google.devtools.ksp")  // KSP 플러그인 적용
}
```

### 2단계: Android Studio KSP 설정

1. **File → Settings → Languages & Frameworks → Kotlin → Compiler**
2. **Kotlin Symbol Processing**:
   - ☑️ **Pass arguments to KSP** (권장)
3. **Apply → OK** 클릭

### 3단계: KSP 실행 및 검증

```bash
# KSP 실행
./gradlew kspDebugKotlin

# 생성된 코드 확인
find . -path "*/generated/ksp/*" -name "*.kt" | head -10

# 예상 생성 파일:
# - Hilt_ProductRepositoryImpl.kt
# - DaitsoDatabase_Impl.kt
# - 등
```

### 4단계: IDE 캐시 정리 (필요시)

KSP 에러가 발생한 경우:

```bash
# IDE 캐시 무효화
# File → Invalidate Caches / Restart → Invalidate and Restart

# 또는 CLI 명령:
./gradlew clean
./gradlew kspDebugKotlin
```

---

## 개발 환경 검증

### 1단계: Gradle Sync 확인

```bash
cd /Users/leeyoonsam/Documents/android-mvi-modular

# Gradle 동기화
./gradlew sync

# 예상 결과:
# BUILD SUCCESSFUL in XXs
```

### 2단계: 프로젝트 구조 검증

```bash
# 모든 모듈 나열
./gradlew projects

# 예상 출력:
# :app
# :core:model
# :core:common
# :core:designsystem
# :core:network
# :core:database
# :core:data
```

### 3단계: 의존성 해결 확인

```bash
# 모든 의존성 다운로드
./gradlew dependencies

# 특정 모듈 의존성 확인
./gradlew :core:data:dependencies
```

### 4단계: 빌드 검증

```bash
# 전체 프로젝트 빌드
./gradlew build

# 예상 결과:
# BUILD SUCCESSFUL in XXs
```

### 5단계: 테스트 실행

```bash
# 단위 테스트 실행
./gradlew test

# 예상 결과:
# BUILD SUCCESSFUL in XXs
# X tests passed

# 특정 모듈 테스트
./gradlew :core:model:test
```

---

## 빌드 및 실행

### 빌드 타입

#### 디버그 빌드 (개발)

```bash
# APK 생성
./gradlew assembleDebug

# 출력 경로:
# app/build/outputs/apk/debug/app-debug.apk

# 기기에 설치
./gradlew installDebug
```

#### 릴리스 빌드 (배포)

```bash
# 키스토어 생성 (처음 한 번)
keytool -genkey -v -keystore daitso-release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias daitso-key

# 서명 설정 (build.gradle.kts)
# android {
#   signingConfigs {
#     release {
#       storeFile = file("path/to/daitso-release.keystore")
#       storePassword = "your-password"
#       keyAlias = "daitso-key"
#       keyPassword = "your-key-password"
#     }
#   }
# }

# 릴리스 APK 생성
./gradlew assembleRelease

# 출력 경로:
# app/build/outputs/apk/release/app-release.apk
```

### Emulator 실행

#### 기본 Emulator 실행

```bash
# 사용 가능한 Emulator 목록
emulator -list-avds

# Emulator 실행
emulator -avd Pixel_5_API_35

# (또는 Android Studio의 Device Manager 사용)
```

#### 앱 실행

```bash
# 연결된 기기/Emulator 확인
adb devices

# 앱 설치 및 실행
./gradlew installDebugRun

# 또는 Android Studio에서 "Run" 버튼 클릭
```

### Gradle 빌드 옵션

```bash
# 병렬 빌드 (더 빠름)
./gradlew build --parallel

# 빌드 캐시 사용
./gradlew build --build-cache

# 특정 모듈만 빌드
./gradlew :app:assembleDebug
./gradlew :core:data:build

# 빌드 프로파일링 (성능 분석)
./gradlew build --profile
# 결과: build/reports/profile/profile-TIMESTAMP.html
```

---

## 문제 해결

### 1. Gradle Sync 실패

**증상**: "Failed to resolve dependency"

**해결방법**:

```bash
# 1단계: 캐시 정리
./gradlew clean

# 2단계: 의존성 재다운로드
./gradlew build --refresh-dependencies

# 3단계: IDE 캐시 무효화
# File → Invalidate Caches / Restart
```

### 2. KSP/Hilt 컴파일 에러

**증상**: "Cannot find symbol: @HiltViewModel"

**해결방법**:

```bash
# 1단계: 빌드 캐시 정리
./gradlew clean

# 2단계: KSP 다시 실행
./gradlew kspDebugKotlin

# 3단계: 빌드
./gradlew build
```

### 3. Room Database 에러

**증상**: "Cannot find implementation for DaitsoDatabase"

**해결방법**:

```bash
# 1단계: Room 컴파일 주석 처리 제거
// @Database(entities = [...], version = 1)
// abstract class DaitsoDatabase : RoomDatabase()

# 2단계: @Entity, @Dao 확인
# - @Entity(tableName = "...") 추가
# - @Dao interface 확인

# 3단계: KSP 다시 실행
./gradlew kspDebugKotlin
```

### 4. Compose Preview 미작동

**증상**: "Waiting for build..." (무한 대기)

**해결방법**:

```bash
# 1단계: Android Studio 재시작
# 2단계: 캐시 무효화
# File → Invalidate Caches / Restart → Invalidate and Restart

# 3단계: 수동 빌드 후 Preview 다시 열기
./gradlew :core:designsystem:build
```

### 5. 메모리 부족 에러

**증상**: "OutOfMemoryError: Java heap space"

**해결방법**:

gradle.properties 수정:

```properties
# 현재 설정
org.gradle.jvmargs=-Xmx4096m -XX:+UseG1GC

# 더 큰 메모리 할당 (8GB)
org.gradle.jvmargs=-Xmx8192m -XX:+UseG1GC

# 또는
org.gradle.jvmargs=-Xmx6144m -XX:+UseParallelGC
```

### 6. 네트워크 타임아웃

**증상**: "Connection timed out" (의존성 다운로드 실패)

**해결방법**:

```bash
# 1단계: 인터넷 연결 확인
ping gradle.org

# 2단계: Gradle 저장소 변경 (설정/회사 방화벽의 경우)
# build.gradle.kts에 Aliyun 미러 추가:
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    google()
    mavenCentral()
}

# 3단계: 오프라인 모드 비활성화
# File → Settings → Gradle → Gradle VM Options
# --offline 제거
```

### 7. Git 관련 에러

**증상**: "fatal: not a git repository"

**해결방법**:

```bash
# 1단계: 저장소 초기화 (처음만)
git init

# 2단계: 원격 저장소 추가
git remote add origin git@github.com:your-org/android-mvi-modular.git

# 3단계: 저장소 확인
git remote -v
```

---

## IDE 플러그인 (선택)

### 권장 플러그인

#### 1. Jetpack Compose IDE Support

- **설치**: File → Settings → Plugins → Marketplace 검색 "Compose"
- **용도**: Compose Preview 개선, Hot Reload
- **필수도**: 선택

#### 2. Detekt

- **설치**: File → Settings → Plugins → Marketplace 검색 "Detekt"
- **용도**: 코드 스타일 및 복잡도 분석
- **필수도**: 선택

#### 3. Kotlin Multiplatform

- **설치**: File → Settings → Plugins → Marketplace 검색 "Kotlin Multiplatform"
- **용도**: 멀티플랫폼 개발 지원 (향후)
- **필수도**: 선택

#### 4. Gradlewrapper (GW)

- **설치**: File → Settings → Plugins → Marketplace 검색 "Gradle Wrapper"
- **용도**: Gradle 래퍼 빠른 실행
- **필수도**: 선택

### 플러그인 설치 방법

1. **Android Studio → Marketplace 탭**
2. **플러그인명 검색**
3. **Install 클릭**
4. **Android Studio 재시작**

---

## 개발 팁

### 1. Gradle 빌드 캐시 활용

```bash
# 빌드 캐시 활성화 (gradle.properties)
org.gradle.caching=true

# 빌드 시간 단축
./gradlew build --build-cache
```

### 2. 병렬 빌드

```bash
# gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=8  # CPU 코어 수에 맞춤

./gradlew build --parallel
```

### 3. 증분 빌드

변경된 모듈만 빌드:

```bash
# :core:data만 변경된 경우
./gradlew :core:data:build

# 의존하는 모듈도 함께 빌드
./gradlew build --build-cache
```

### 4. 빌드 프로파일링

성능 병목 지점 분석:

```bash
./gradlew build --profile

# 결과: build/reports/profile/profile-TIMESTAMP.html
# 브라우저에서 열어서 빌드 시간 분석
```

---

## 다음 단계

1. **프로젝트 열기**: Android Studio에서 프로젝트 디렉토리 열기
2. **Gradle Sync**: 자동으로 실행되거나 File → Sync Now
3. **테스트 실행**: ./gradlew test
4. **앱 실행**: Run 버튼 클릭 또는 ./gradlew installDebug

---

**설정 가이드 버전**: 1.0.0
**최종 검토**: 2025-11-28
**상태**: Active
