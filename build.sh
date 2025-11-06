#!/bin/bash

# CRDP Java Simple Build Script
# javac를 사용하여 간단한 CRDP 데모를 빌드합니다.

set -e  # 에러 발생시 스크립트 중단

echo "=== CRDP Java Simple Build Script ==="

# 현재 디렉토리 확인
if [ ! -d "src" ]; then
    echo "Error: src directory not found. Please run this script from the project root directory."
    exit 1
fi

# 빌드 디렉토리 생성
echo "1. Setting up build directory..."
mkdir -p build/classes
mkdir -p build/lib

# 기존 클래스 파일 정리
echo "2. Cleaning previous build..."
rm -f build/classes/*.class

# Java 소스 컴파일
echo "3. Compiling Java source files..."
javac -d build/classes src/*.java

if [ $? -eq 0 ]; then
    echo "   Compilation successful!"
else
    echo "   Compilation failed!"
    exit 1
fi

# 실행 가능한 스크립트 생성
echo "4. Creating run script..."
cat > run.sh << 'EOF'
#!/bin/bash

# CRDP Demo 실행 스크립트
cd "$(dirname "$0")"

if [ ! -d "build/classes" ]; then
    echo "Error: build/classes directory not found. Please run build.sh first."
    exit 1
fi

echo "Running CRDP Demo..."
java -cp build/classes CrdpDemo "$@"
EOF

chmod +x run.sh

echo "5. Build completed successfully!"
echo ""
echo "Files generated:"
echo "  - build/classes/*.class  (compiled Java classes)"
echo "  - run.sh                 (execution script)"
echo ""
echo "You can now run the application with:"
echo "  ./run.sh --help"
echo ""
echo "Example usage:"
echo "  ./run.sh"
echo "  ./run.sh --host 192.168.0.231 --policy P03 --data \"test-data\""
echo "  ./run.sh --verbose --show-bodies"
echo ""
echo "Or run directly with java:"
echo "  java -cp build/classes CrdpDemo --help"