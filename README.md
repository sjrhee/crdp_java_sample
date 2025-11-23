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
// 1. 초기화 (설정 파일에서 자동 로드)
CrdpClient client = CrdpClient.fromConfigFile("crdp.properties");

// 2. 암호화
String encrypted = client.protect("주민등록번호 123456-1234567");
System.out.println("암호화: " + encrypted);

// 3. 복호화
String decrypted = client.reveal(encrypted);
System.out.println("복호화: " + decrypted);
```

---

## 📁 파일 구조
- `CrdpClient.java` - 핵심 라이브러리 클래스
- `crdp-client.jar` - 배포용 JAR 파일
- `SimpleExample.java` - 간단한 사용 예제
- `crdp.properties` - 설정 파일
- `build.sh` - 빌드 스크립트
- `run.sh` - 실행 스크립트

## 🚀 실행 방법
```bash
./build.sh   # 빌드
./run.sh     # 실행
```

## 🌟 Apache Spark와 함께 사용하기

대용량 데이터 처리를 위해 Apache Spark와 CRDP를 통합할 수 있습니다.

```java
// Spark DataFrame의 민감한 컬럼 암호화
SparkSession spark = SparkSession.builder()
    .appName("CRDP Protection")
    .getOrCreate();

CrdpClient crdp = CrdpClient.fromConfigFile("crdp.properties");

Dataset<Row> protectedDf = df.map(row -> {
    String ssn = row.getAs("ssn");
    String encryptedSsn = crdp.protect(ssn);
    return new Customer(row.getAs("name"), encryptedSsn, row.getAs("phone"));
}, Encoders.bean(Customer.class)).toDF();
```

자세한 내용은 [**Apache Spark 통합 가이드**](docs/Spark_통합_가이드.md)를 참조하세요.

## 💡 특징

- **Zero Dependency**: 외부 라이브러리(Jackson, Gson, Apache HttpClient 등) 의존성 없음
- **Easy Integration**: `CrdpClient.java` 파일 하나만 복사해서 프로젝트에 넣어도 바로 동작
- **Secure**: HTTPS 및 JWT 인증 지원
- **Big Data Ready**: Apache Spark와 통합하여 대용량 데이터 보호 가능

## 🔗 관련 링크
- [**Apache Spark 통합 가이드**](docs/Spark_통합_가이드.md) - Spark에서 CRDP 사용하기
- [**CipherTrust 정책 적용 방안 및 구조도**](docs/CipherTrust_정책_적용_방안.md)
- [CRDP API 문서](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
