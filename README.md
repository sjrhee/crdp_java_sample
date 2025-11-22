# CRDP Java Sample & Library

**CRDP(CipherTrust RESTful Data Protection) API**를 Java 애플리케이션에서 가장 쉽게 사용하는 방법입니다.

## 🚀 30초 만에 시작하기

복잡한 설정 없이 **라이브러리(`crdp-client.jar`)**를 추가하고 바로 사용하세요.

### 1. 라이브러리 빌드

```bash
./build.sh
# 생성된 crdp-client.jar 파일을 프로젝트에 추가하세요.
```

### 2. 코드 작성 (복사-붙여넣기용)

```java
// 1. 초기화 (호스트, 포트, 정책, 토큰, 타임아웃)
CrdpClient client = new CrdpClient("192.168.0.233", 32182, "P01", "JWT_TOKEN...", 10);

// 2. 암호화
String encrypted = client.protect("주민등록번호 123456-1234567");
System.out.println("암호화: " + encrypted);

// 3. 복호화
String decrypted = client.reveal(encrypted);
System.out.println("복호화: " + decrypted);
```

---

## 📦 파일 구조

- **`crdp-client.jar`**: 배포용 라이브러리 (빌드 후 생성됨)
- **`CrdpClient.java`**: 라이브러리 소스 코드 (핵심 로직)
- **`LibraryUsageDemo.java`**: 위 예제의 전체 실행 가능한 소스 코드
- `SimpleDemo.java` / `MinimalDemo.java`: (참고용) HTTP 통신 과정을 직접 구현한 예제

## 🛠️ 실행 방법

### 라이브러리 데모 실행
```bash
./build.sh
./run_lib_demo.sh
```

### 기존 데모 실행 (참고용)
```bash
./run.sh          # CLI 옵션 지원 버전
./run_minimal.sh  # 설정 파일 기반 최소 버전
```

## 💡 특징

- **Zero Dependency**: 외부 라이브러리(Jackson, Gson, Apache HttpClient 등) 의존성 없음
- **Easy Integration**: `CrdpClient.java` 파일 하나만 복사해서 프로젝트에 넣어도 바로 동작
- **Secure**: HTTPS 및 JWT 인증 지원

## 🔗 관련 링크
- [CRDP API 문서](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
- [**CipherTrust 정책 적용 방안 및 구조도**](CipherTrust_정책_적용_방안.md) - *CRDP 구조 및 특징 이미지 포함*
- [GitHub 저장소](https://github.com/sjrhee/crdp_java_sample)
