#!/bin/bash

echo "=== CRDP 클라이언트 컴파일 ==="

# Gson 라이브러리 다운로드 (없는 경우)
GSON_VERSION="2.10.1"
GSON_JAR="gson-${GSON_VERSION}.jar"

if [ ! -f "$GSON_JAR" ]; then
    echo "Gson 라이브러리 다운로드 중..."
    wget -q "https://repo1.maven.org/maven2/com/google/code/gson/gson/${GSON_VERSION}/${GSON_JAR}"
    if [ $? -eq 0 ]; then
        echo "Gson 다운로드 완료: $GSON_JAR"
    else
        echo "Gson 다운로드 실패! 수동으로 다운로드하거나 Maven/Gradle을 사용하세요."
        exit 1
    fi
fi

# 기존 빌드 정리
echo "기존 class 파일 정리 중..."
rm -f *.class

# 컴파일
echo "컴파일 중..."
javac -source 11 -target 11 -cp .:$GSON_JAR CrdpClient.java SimpleExample.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 컴파일 성공!"
    echo ""
    echo "생성된 파일:"
    ls -1 *.class 2>/dev/null | sed 's/^/  - /'
    echo ""
    echo "실행 방법:"
    echo "  ./run.sh"
else
    echo "❌ 컴파일 실패!"
    exit 1
fi