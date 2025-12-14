# CRDP Java Sample & Library

**CRDP(CipherTrust RESTful Data Protection) API**를 Java 애플리케이션에서 가장 쉽게 사용하는 방법입니다.

## 🚀 30초 만에 시작하기

간단한 컴파일과 실행으로 바로 사용할 수 있습니다.

### 1. 컴파일

```bash
./build.sh
# CrdpClient.class 파일이 생성됩니다.
```

### 2. 코드 작성 (복사-붙여넣기용)

```java
// 1. 초기화 (설정 파일에서 자동 로드)
CrdpClient client = CrdpClient.fromConfigFile("crdp.properties");

// 2. 암호화
String encrypted = client.enc("주민등록번호 123456-1234567");
System.out.println("암호화: " + encrypted);

// 3. 복호화
String decrypted = client.dec(encrypted);
System.out.println("복호화: " + decrypted);
```

---

## 📁 파일 구조
- `CrdpClient.java` - 핵심 라이브러리 클래스
- `SimpleExample.java` - 간단한 사용 예제
- `crdp.properties` - 설정 파일
- `build.sh` - 컴파일 스크립트
- `run.sh` - 실행 스크립트
- `gson-2.10.1.jar` - JSON 처리 라이브러리 (자동 다운로드)

## 🚀 실행 방법
```bash
./build.sh   # 컴파일
./run.sh     # 실행
```

## 💡 특징

- **Java 11 HttpClient**: 최신 표준 HTTP 클라이언트 사용으로 성능 향상
- **Minimal Dependency**: Gson만 필요 (JSON 처리용)
- **Easy Integration**: 간단한 API로 빠른 통합 가능
- **Secure**: HTTPS 및 JWT 인증 지원

## 🔗 관련 링크
- [**CipherTrust 정책 적용 방안 및 구조도**](docs/CipherTrust_정책_적용_방안.md)
- [**성능에 영향을 미치는 요소**](docs/성능에_영향을_미치는_요소.md)
- [**실무 적용 시 성능 및 안정성 개선 제안**](docs/PERFORMANCE_GUIDE.md)
- [**CRDP API 문서**](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
