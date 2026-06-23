#!/bin/sh
# =============================================================================
# setup_linux.sh
# 목적 : git clone 직후 Linux 환경에서 1회 실행
#        1) chmod +x  — 실행 권한 복원 (현재 working tree)
#        2) git update-index --chmod=+x  — git index 에 권한 영구 기록
#           → 이후 커밋/push 하면 Windows 포함 모든 환경에서 +x 유지됨
# 위치 : 프로젝트 루트 (so.jpos.lpu237/)
# 사용 : sh setup_linux.sh
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# ── 대상 파일 목록 ──────────────────────────────────────────────────────────
EXECUTABLES="
  Source/pos-device-lpu237/gradlew
  Release/javapos/TestSample/javac_TestMsr_debian_x64.sh
  Release/javapos/TestSample/TestMsr_debian_x64.sh
  setup_linux.sh
"

# ── 1) chmod +x (working tree) ──────────────────────────────────────────────
echo "[setup] chmod +x ..."
for f in $EXECUTABLES; do
    chmod +x "$SCRIPT_DIR/$f" && echo "  OK  $f"
done

# ── 2) git update-index --chmod=+x (index 영구 기록) ────────────────────────
if git -C "$SCRIPT_DIR" rev-parse --git-dir > /dev/null 2>&1; then
    echo ""
    echo "[setup] git update-index --chmod=+x ..."
    for f in $EXECUTABLES; do
        git -C "$SCRIPT_DIR" update-index --chmod=+x "$f" && echo "  OK  $f"
    done
    echo ""
    echo "[setup] git index 에 실행 권한이 기록되었습니다."
    echo "        'git commit -m \"fix: set executable bit for shell scripts\"' 후"
    echo "        push 하면 Windows 포함 모든 환경에서 영구 적용됩니다."
else
    echo ""
    echo "[setup] git repo 가 아닌 환경입니다. chmod 만 적용되었습니다."
fi

echo ""
echo "─────────────────────────────────────────────────────────────"
echo "  빌드    : cd Source/pos-device-lpu237 && ./gradlew build"
echo "  컴파일  : sh Release/javapos/TestSample/javac_TestMsr_debian_x64.sh"
echo "  테스트  : sh Release/javapos/TestSample/TestMsr_debian_x64.sh"
echo "─────────────────────────────────────────────────────────────"
