# CRDP Java Simple Demo

간단한 Java CLI 애플리케이션으로 Thales CipherTrust Data Protection (CRDP) API의 단일 protect/reveal 기능을 테스트합니다.

## 주요 기능

- 단일 protect → reveal 처리 및 검증
- 순수 Java 표준 라이브러리만 사용 (외부 의존성 없음)
- 간단한 명령행 인터페이스
- 상세한 요청/응답 로깅 지원 (옵션)
- 데이터 일치성 검증

## 요구사항

- Java 8 이상
- javac 컴파일러

## 빌드 및 실행

### 프로젝트 빌드

```bash
# 프로젝트 빌드
./build.sh
```

### 실행 방법

#### 실행 스크립트 사용

```bash
# 기본 실행
./run.sh

# 도움말 확인
./run.sh --help

# 옵션과 함께 실행
./run.sh --host 192.168.0.231 --policy P03 --data "test-data" --verbose
```

#### 직접 Java 실행

```bash
# 기본 실행
java -cp build/classes CrdpDemo

# 옵션과 함께 실행
java -cp build/classes CrdpDemo --host 192.168.0.231 --policy P03 --verbose
```

## 명령행 옵션

```
Usage: java CrdpDemo [OPTIONS]

Simple CRDP protect/reveal demo

Options:
  --host HOST          API host (default: 192.168.0.231)
  --port PORT          API port (default: 32082)
  --policy POLICY      Protection policy name (default: P03)
  --data DATA          Data to protect (default: 1234567890123)
  --username USER      Username for reveal operation (optional)
  --timeout SECONDS    Request timeout in seconds (default: 10)
  --show-bodies        Show request/response bodies
  --verbose            Enable verbose output
  --help               Show this help message
```

## 사용 예시

### 기본 실행

```bash
./run.sh
```

출력 예시:
```
=== CRDP Java Demo ===
Host: 192.168.0.231:32082
Policy: P03
Data: 1234567890123

1. Calling protect API...
   SUCCESS: Protected data length = 64

2. Calling reveal API...
   SUCCESS: Revealed data = 1234567890123

3. Verification:
   Original data:  1234567890123
   Revealed data:  1234567890123
   Data matches:   YES

=== Summary ===
Protect operation:  SUCCESS (0.1234s)
Reveal operation:   SUCCESS (0.0987s)
Data verification:  PASSED
Total time:         0.2543s
```

### 상세 로그와 함께 실행

```bash
./run.sh --verbose --show-bodies --data "my-secret-data"
```

### 특정 서버와 정책으로 실행

```bash
./run.sh --host 10.0.0.100 --port 8080 --policy MyPolicy --username testuser
```

## 프로젝트 구조

```
.
├── src/
│   ├── CrdpDemo.java     # 메인 CLI 애플리케이션
│   ├── CrdpClient.java   # HTTP 클라이언트
│   └── ApiResponse.java  # API 응답 래퍼
├── build/
│   └── classes/          # 컴파일된 클래스 파일
├── build.sh              # 빌드 스크립트
├── run.sh                # 실행 스크립트 (빌드 후 생성)
└── README.md             # 이 문서
```

## 빌드 과정

1. `build.sh` 스크립트가 `src/` 디렉토리의 모든 `.java` 파일을 컴파일
2. 컴파일된 `.class` 파일들이 `build/classes/` 디렉토리에 저장
3. 실행을 위한 `run.sh` 스크립트 자동 생성

## 에러 처리

- **네트워크 에러**: HTTP 연결 실패, 타임아웃 등의 네트워크 관련 오류
- **API 에러**: 4xx/5xx HTTP 상태 코드 응답 처리
- **데이터 검증**: 원본 데이터와 복원된 데이터의 일치성 검증
- **JSON 파싱**: 간단한 문자열 기반 JSON 파싱으로 필수 필드 추출

## 특징

- **의존성 없음**: 순수 Java 표준 라이브러리만 사용
- **간단한 구조**: 3개의 클래스로 구성된 최소한의 구현
- **명확한 출력**: 각 단계별 결과와 최종 요약 정보 제공
- **유연한 설정**: 명령행 옵션으로 모든 주요 설정 변경 가능

## 개발 및 테스트

```bash
# 프로젝트 클론
git clone <repository-url>
cd crdp_java_sample

# 빌드
./build.sh

# 실행
./run.sh --help
```

## 라이선스

이 프로젝트는 샘플 코드로 제공됩니다.