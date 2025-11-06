# CRDP Java Simple Demo

**단 하나의 파일**로 CRDP API 테스트하기!

## ✨ 특징

- 🚀 **하나의 파일** - SimpleDemo.java 만으로 모든 기능
- 📦 **외부 의존성 없음** - 순수 Java만 사용
- ⚡ **빠른 실행** - 컴파일하고 바로 실행

## 요구사항

- Java 8+

## 30초 시작

```bash
# 다운로드
git clone https://github.com/sjrhee/crdp_java_sample.git
cd crdp_java_sample

# 빌드 & 실행
./build.sh
./run.sh
```

## 출력 예시

```
=== CRDP 간단 데모 ===
서버: sjrhee.ddns.net:32082
정책: P01
데이터: 1234567890123

1. 데이터 보호 중... 성공: 435b7e99fdf33e10a29e4708710cacc2
2. 데이터 복원 중... 성공: 1234567890123

3. 검증 결과:
   원본: 1234567890123
   복원: 1234567890123
   일치: 예
```

## 옵션

```bash
./run.sh --help                    # 도움말
./run.sh --data "9876543210987"    # 다른 데이터
./run.sh --host example.com        # 다른 서버
```

## 파일 구조

```
.
├── SimpleDemo.java      # 🎯 모든 기능이 여기 있음!
├── build.sh            # 빌드 스크립트
└── run.sh              # 실행 스크립트 (빌드 후 생성)
```

## 수동 실행

```bash
# 컴파일
javac SimpleDemo.java

# 실행
java SimpleDemo
java SimpleDemo --data "테스트데이터"
```

## 코드 설명

`SimpleDemo.java` 하나의 파일에:
- ✅ HTTP 클라이언트
- ✅ JSON 파싱
- ✅ CLI 옵션 처리
- ✅ CRDP API 호출 (protect/reveal)

**140줄로 모든 기능 완성!**

## 관련 링크

- [CRDP API 문서](https://thalesdocs.com/ctp/con/crdp/latest/crdp-apis/index.html)
- [GitHub 저장소](https://github.com/sjrhee/crdp_java_sample)
