#!/bin/bash

echo "=== 간단 빌드 ==="

# 기존 빌드 정리
rm -f *.class SimpleDemo MinimalDemo

# 컴파일 (Java 11 호환성 유지)
echo "컴파일 중..."
javac -source 11 -target 11 SimpleDemo.java
javac -source 11 -target 11 MinimalDemo.java

# 라이브러리 및 예제 컴파일
echo "라이브러리 빌드 중..."
javac -source 11 -target 11 CrdpClient.java
javac -source 11 -target 11 -cp . LibraryUsageDemo.java

if [ $? -eq 0 ]; then
    echo "컴파일 성공!"
    
    # JAR 파일 생성
    echo "JAR 패키징 중..."
    jar cf crdp-client.jar CrdpClient.class
    echo "생성됨: crdp-client.jar"

    # SimpleDemo 실행 스크립트 생성
    cat > run.sh << 'EOF'
#!/bin/bash
# 현재 디렉토리에서 properties 파일을 찾을 수 있도록 설정
java -cp . SimpleDemo "$@"
EOF
    chmod +x run.sh
    
    # MinimalDemo 실행 스크립트 생성
    cat > run_minimal.sh << 'EOF'
#!/bin/bash
# 최소 버전 실행 (옵션 없음, properties만 사용)
java -cp . MinimalDemo
EOF
    chmod +x run_minimal.sh

    # 라이브러리 데모 실행 스크립트 생성
    cat > run_lib_demo.sh << 'EOF'
#!/bin/bash
# 라이브러리(JAR)를 사용하여 데모 실행
java -cp .:crdp-client.jar LibraryUsageDemo
EOF
    chmod +x run_lib_demo.sh
    
    echo "사용법:"
    echo "  ./run.sh            (기존 데모)"
    echo "  ./run_minimal.sh    (최소 데모)"
    echo "  ./run_lib_demo.sh   (라이브러리 사용 데모)"
    echo ""
    echo "배포 파일:"
    echo "  crdp-client.jar     (다른 프로젝트에서 사용 가능한 라이브러리)"
else
    echo "컴파일 실패!"
    exit 1
fi