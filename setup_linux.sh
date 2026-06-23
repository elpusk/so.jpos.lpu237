#!/bin/sh
# =============================================================================
# setup_linux.sh
# 목적 : git clone 직후 Linux 환경에서 1회 실행하여 실행 권한을 복원한다.
#        (Windows 에서 커밋된 .sh / gradlew 파일의 +x 가 누락되는 경우 대비)
# 위치 : 프로젝트 루트 (so.jpos.lpu237/)
# 사용 : sh setup_linux.sh
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "[setup] 실행 권한 부여 중..."

chmod +x "$SCRIPT_DIR/Source/pos-device-lpu237/gradlew"
chmod +x "$SCRIPT_DIR/Release/javapos/TestSample/debian_x64/javac_TestMsr_debian_x64.sh"
chmod +x "$SCRIPT_DIR/Release/javapos/TestSample/debian_x64/TestMsr_debian_x64.sh"

echo "[setup] 완료."
echo ""
echo "  빌드:    cd Source/pos-device-lpu237 && ./gradlew build"
echo "  테스트:  Release/javapos/TestSample/debian_x64/TestMsr_debian_x64.sh"
