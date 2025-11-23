# Apache Spark와 CRDP 통합 가이드

## Apache Spark란?

**Apache Spark**는 대규모 데이터 처리를 위한 오픈소스 분산 컴퓨팅 시스템입니다.

### 주요 특징

- **빠른 처리 속도**: 메모리 내 연산으로 기존 Hadoop MapReduce 대비 최대 100배 빠른 성능
- **범용성**: 배치 처리, 스트리밍, 머신러닝, 그래프 처리 등 다양한 워크로드 지원
- **사용 편의성**: Java, Scala, Python, R 등 여러 언어 API 제공
- **확장성**: 수천 개의 노드로 확장 가능

### Spark의 핵심 구성 요소

1. **Spark Core**: 기본 엔진 및 RDD(Resilient Distributed Dataset) 제공
2. **Spark SQL**: 구조화된 데이터 처리 및 SQL 쿼리 지원
3. **Spark Streaming**: 실시간 스트림 데이터 처리
4. **MLlib**: 머신러닝 라이브러리
5. **GraphX**: 그래프 처리 라이브러리

## 왜 Spark에서 CRDP가 필요한가?

빅데이터 환경에서 민감한 정보(주민등록번호, 신용카드 번호, 의료 정보 등)를 처리할 때 데이터 보호가 필수적입니다:

- **규정 준수**: GDPR, HIPAA, 개인정보보호법 등 데이터 보호 규정 준수
- **데이터 유출 방지**: 분산 환경에서 데이터 유출 시 피해 최소화
- **접근 제어**: 권한이 있는 사용자만 민감 데이터 복호화 가능
- **감사 추적**: 데이터 접근 및 사용 이력 추적

## Spark에서 CRDP 사용하기

### 1. 환경 설정

#### Maven 프로젝트 설정 (pom.xml)

```xml
<dependencies>
    <!-- Spark Dependencies -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_2.12</artifactId>
        <version>3.5.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql_2.12</artifactId>
        <version>3.5.0</version>
    </dependency>
</dependencies>
```

#### CRDP 라이브러리 추가

```bash
# crdp-client.jar를 프로젝트의 lib 디렉토리에 복사
cp crdp-client.jar your-spark-project/lib/
```

### 2. Spark 애플리케이션에서 CRDP 사용 예제

#### 예제 1: DataFrame의 민감한 컬럼 암호화

```java
import org.apache.spark.sql.*;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Encoders;

public class SparkCrdpExample {
    
    public static void main(String[] args) throws Exception {
        // Spark 세션 생성
        SparkSession spark = SparkSession.builder()
            .appName("CRDP Protection Example")
            .master("local[*]")
            .getOrCreate();
        
        // CRDP 클라이언트 초기화
        CrdpClient crdp = CrdpClient.fromConfigFile("crdp.properties");
        
        // 샘플 데이터 생성
        Dataset<Row> df = spark.createDataFrame(
            Arrays.asList(
                new Customer("홍길동", "123456-1234567", "010-1234-5678"),
                new Customer("김철수", "234567-2345678", "010-2345-6789"),
                new Customer("이영희", "345678-3456789", "010-3456-7890")
            ),
            Customer.class
        );
        
        // 주민등록번호 컬럼 암호화
        Dataset<Row> protectedDf = df.map(
            (MapFunction<Row, Customer>) row -> {
                String name = row.getAs("name");
                String ssn = row.getAs("ssn");
                String phone = row.getAs("phone");
                
                // 주민등록번호 암호화
                String encryptedSsn = crdp.protect(ssn);
                
                return new Customer(name, encryptedSsn, phone);
            },
            Encoders.bean(Customer.class)
        ).toDF();
        
        // 결과 출력
        protectedDf.show(false);
        
        // 암호화된 데이터 저장
        protectedDf.write()
            .mode(SaveMode.Overwrite)
            .parquet("hdfs://path/to/protected_data");
        
        spark.stop();
    }
    
    // Customer 클래스
    public static class Customer implements java.io.Serializable {
        private String name;
        private String ssn;
        private String phone;
        
        public Customer() {}
        
        public Customer(String name, String ssn, String phone) {
            this.name = name;
            this.ssn = ssn;
            this.phone = phone;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSsn() { return ssn; }
        public void setSsn(String ssn) { this.ssn = ssn; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
```

#### 예제 2: Spark Streaming에서 실시간 데이터 보호

```java
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.*;

public class SparkStreamingCrdpExample {
    
    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession.builder()
            .appName("Streaming CRDP Protection")
            .master("local[*]")
            .getOrCreate();
        
        CrdpClient crdp = CrdpClient.fromConfigFile("crdp.properties");
        
        // Kafka 스트림에서 데이터 읽기
        Dataset<Row> stream = spark
            .readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "customer-data")
            .load();
        
        // 스트림 데이터 처리 및 암호화
        Dataset<Row> protectedStream = stream
            .selectExpr("CAST(value AS STRING) as json")
            .select(functions.from_json(
                functions.col("json"),
                new StructType()
                    .add("name", "string")
                    .add("ssn", "string")
                    .add("phone", "string")
            ).as("data"))
            .select("data.*")
            .map((MapFunction<Row, Customer>) row -> {
                String name = row.getAs("name");
                String ssn = row.getAs("ssn");
                String phone = row.getAs("phone");
                
                // 실시간으로 주민등록번호 암호화
                String encryptedSsn = crdp.protect(ssn);
                
                return new Customer(name, encryptedSsn, phone);
            }, Encoders.bean(Customer.class))
            .toDF();
        
        // 암호화된 스트림 데이터 저장
        StreamingQuery query = protectedStream
            .writeStream()
            .format("parquet")
            .option("path", "hdfs://path/to/protected_stream")
            .option("checkpointLocation", "/tmp/checkpoint")
            .start();
        
        query.awaitTermination();
    }
}
```

#### 예제 3: UDF(User Defined Function)로 CRDP 암호화 적용

```java
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class SparkUdfExample {
    
    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession.builder()
            .appName("CRDP UDF Example")
            .master("local[*]")
            .getOrCreate();
        
        final CrdpClient crdp = CrdpClient.fromConfigFile("crdp.properties");
        
        // CRDP 암호화 UDF 등록
        spark.udf().register("protect_data", 
            (UDF1<String, String>) plaintext -> {
                try {
                    return crdp.protect(plaintext);
                } catch (Exception e) {
                    return "ERROR: " + e.getMessage();
                }
            }, DataTypes.StringType);
        
        // CRDP 복호화 UDF 등록
        spark.udf().register("reveal_data",
            (UDF1<String, String>) ciphertext -> {
                try {
                    return crdp.reveal(ciphertext);
                } catch (Exception e) {
                    return "ERROR: " + e.getMessage();
                }
            }, DataTypes.StringType);
        
        // SQL로 암호화 적용
        Dataset<Row> df = spark.read()
            .option("header", "true")
            .csv("customers.csv");
        
        df.createOrReplaceTempView("customers");
        
        // SQL에서 UDF 사용
        Dataset<Row> result = spark.sql(
            "SELECT name, " +
            "       protect_data(ssn) as encrypted_ssn, " +
            "       phone " +
            "FROM customers"
        );
        
        result.show(false);
        
        spark.stop();
    }
}
```

### 3. 성능 최적화 팁

#### 배치 처리로 성능 향상

```java
// CRDP 클라이언트를 Broadcast Variable로 공유
Broadcast<CrdpClient> broadcastCrdp = spark.sparkContext()
    .broadcast(CrdpClient.fromConfigFile("crdp.properties"), 
               scala.reflect.ClassTag$.MODULE$.apply(CrdpClient.class));

// 파티션별로 한 번만 초기화
Dataset<Row> protectedDf = df.mapPartitions(
    (MapPartitionsFunction<Row, Customer>) iterator -> {
        CrdpClient crdp = broadcastCrdp.value();
        List<Customer> results = new ArrayList<>();
        
        while (iterator.hasNext()) {
            Row row = iterator.next();
            String name = row.getAs("name");
            String ssn = row.getAs("ssn");
            String phone = row.getAs("phone");
            
            String encryptedSsn = crdp.protect(ssn);
            results.add(new Customer(name, encryptedSsn, phone));
        }
        
        return results.iterator();
    },
    Encoders.bean(Customer.class)
).toDF();
```

### 4. 주의사항

1. **직렬화**: CrdpClient가 Spark 태스크 간에 전달될 때 직렬화 가능해야 합니다.
2. **연결 관리**: 각 Executor에서 CRDP 서버와의 연결을 효율적으로 관리하세요.
3. **오류 처리**: 네트워크 오류나 CRDP 서버 장애에 대비한 재시도 로직 구현을 권장합니다.
4. **성능 고려**: 대용량 데이터 처리 시 CRDP API 호출 횟수를 최소화하세요.
5. **보안 설정**: JWT 토큰과 설정 파일은 안전하게 관리하세요.

## 실전 시나리오

### 시나리오 1: 대용량 고객 데이터 일괄 암호화

```java
// 1TB의 고객 데이터를 읽어서 민감 정보 암호화 후 저장
Dataset<Row> bigData = spark.read().parquet("hdfs://customer_data/*.parquet");

Dataset<Row> protected = bigData.repartition(1000) // 병렬 처리 최적화
    .mapPartitions(rows -> protectSensitiveData(rows), encoder);

protected.write()
    .mode(SaveMode.Overwrite)
    .partitionBy("year", "month")
    .parquet("hdfs://protected_customer_data/");
```

### 시나리오 2: 데이터 분석 시 선택적 복호화

```java
// 권한이 있는 사용자만 특정 컬럼 복호화하여 분석
Dataset<Row> protectedData = spark.read()
    .parquet("hdfs://protected_customer_data/");

// 분석가 권한에 따라 선택적 복호화
if (hasDecryptPermission(currentUser)) {
    protectedData = protectedData.withColumn(
        "ssn_revealed",
        functions.callUDF("reveal_data", functions.col("ssn_encrypted"))
    );
}

protectedData.groupBy("region")
    .agg(functions.count("*").as("customer_count"))
    .show();
```

## 추가 리소스

- [Apache Spark 공식 문서](https://spark.apache.org/docs/latest/)
- [CRDP API 문서](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
- [CipherTrust 정책 적용 방안](CipherTrust_정책_적용_방안.md)

## 문의 및 지원

Spark와 CRDP 통합에 대한 문의사항이 있으시면 프로젝트 이슈를 생성해 주세요.
