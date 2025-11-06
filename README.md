# CRDP Java Simple Demo

간단한 Java CLI 애플리케이션으로 Thales CipherTrust Data Protection (CRDP) API의 데이터 보호/복원 기능을 테스트합니다.

## 주요 특징

- ✅ **초보자 친화적** - 복잡한 설정 없이 간단한 구조
- ✅ **순수 Java** - 외부 라이브러리 없이 표준 라이브러리만 사용
- ✅ **한글 지원** - 한국 사용자를 위한 한글 메시지
- ✅ **단순한 CLI** - 필수 옵션만 제공
- ✅ **즉시 실행** - javac로 간단히 컴파일하여 바로 사용

## 요구사항

- Java 8 이상
- javac 컴파일러

## 빠른 시작

```bash
# 저장소 클론
git clone https://github.com/sjrhee/crdp_java_sample.git
cd crdp_java_sample

# 빌드
./build.sh

# 기본 실행
./run.sh
```

## 사용법

### 기본 실행

```bash
./run.sh
```

출력 예시:
```
=== CRDP 데모 ===
서버: sjrhee.ddns.net:32082
정책: P03
데이터: 1234567890123

1. 데이터 보호 중...
   성공: 8555545382975

2. 데이터 복원 중...
   성공: 1234567890123

3. 검증 결과:
   원본: 1234567890123
   복원: 1234567890123
   일치: 예
```

### 옵션 사용

```bash
# 도움말
./run.sh --help

# 다른 데이터로 테스트
./run.sh --data "9876543210987"

# 다른 서버 사용
./run.sh --host example.com --port 8080

# 상세 정보 표시
./run.sh --details
```

## 명령행 옵션

| 옵션 | 설명 | 기본값 |
|------|------|--------|
| `--host HOST` | 서버 주소 | sjrhee.ddns.net |
| `--port PORT` | 서버 포트 | 32082 |
| `--policy POLICY` | 보호 정책 | P03 |
| `--data DATA` | 보호할 데이터 | 1234567890123 |
| `--details` | 상세 정보 표시 | - |
| `--help` | 도움말 표시 | - |

## 프로젝트 구조

```
.
├── src/
│   ├── CrdpDemo.java      # 메인 애플리케이션
│   ├── CrdpClient.java    # HTTP 클라이언트
│   └── ApiResponse.java   # API 응답 래퍼
├── build.sh               # 빌드 스크립트
├── run.sh                 # 실행 스크립트 (빌드 후 생성)
└── README.md              # 이 문서
```

## 핵심 클래스 설명

### CrdpDemo.java
- 메인 애플리케이션 클래스
- 명령행 옵션 처리
- 전체 워크플로우 관리

### CrdpClient.java
- CRDP API와의 HTTP 통신 담당
- `protect()`: 데이터 보호 (암호화/토큰화)
- `reveal()`: 데이터 복원 (복호화/디토큰화)

### ApiResponse.java
- HTTP 응답을 래핑하는 유틸리티 클래스
- JSON 응답에서 필요한 데이터 추출

## 빌드 및 실행 방법

### 1. 자동 빌드
```bash
./build.sh
./run.sh
```

### 2. 수동 빌드
```bash
# 컴파일
mkdir -p build/classes
javac -d build/classes src/*.java

# 실행
java -cp build/classes CrdpDemo
```

## 데이터 형식 안내

- **입력 데이터**: 13자리 숫자 형식 권장 (예: 1234567890123)
- **보호된 데이터**: API에서 반환하는 암호화/토큰화된 값
- **복원된 데이터**: 원본과 동일한 형태로 복원

## 에러 처리

애플리케이션은 다음과 같은 에러를 처리합니다:

- **네트워크 연결 실패**
- **HTTP 4xx/5xx 에러**
- **JSON 응답 파싱 에러**
- **데이터 불일치**

## 예제

### 기본 테스트
```bash
./run.sh
```

### 다른 정책으로 테스트
```bash
./run.sh --policy P01 --data "1111222233334"
```

<<<<<<< HEAD
이 프로젝트는 샘플 코드로 제공됩니다.
=======
### 로컬 서버 테스트
```bash
./run.sh --host localhost --port 8080
```

## 개발자 정보

- **목적**: CRDP API 학습 및 테스트용 샘플
- **대상**: Java 초보자 및 CRDP API 사용자
- **라이선스**: 샘플 코드 (자유 사용)

## 기여하기

이 프로젝트는 학습 목적의 샘플 코드입니다. 개선사항이나 버그가 있다면 이슈를 등록해 주세요.

## 관련 링크

- [Thales CipherTrust Data Protection 문서](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
- [GitHub 저장소](https://github.com/sjrhee/crdp_java_sample)
>>>>>>> 674f6ed (리드미 업데이트: 리팩토링된 코드에 맞게 기본값 수정 및 정리)
