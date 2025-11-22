#!/bin/bash

echo "=== 간단 빌드 ==="

# 기존 빌드 정리
rm -f *.class SimpleDemo MinimalDemo

# 컴파일 (Java 11 호환성 유지)
echo "컴파일 중..."
javac -source 11 -target 11 SimpleDemo.java
javac -source 11 -target 11 MinimalDemo.java

if [ $? -eq 0 ]; then
    echo "컴파일 성공!"
    
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
    
    echo "사용법:"
    echo "  ./run.sh            (옵션 지원 버전)"
    echo "  ./run_minimal.sh    (최소 버전)"
    echo ""
    echo "예제:"
    echo "  ./run.sh"
    echo "  ./run.sh --help"
    echo "  ./run.sh --data 9876543210987"
    echo "  ./run_minimal.sh"
else
    echo "컴파일 실패!"
    exit 1
fi