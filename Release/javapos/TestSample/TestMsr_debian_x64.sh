#!/bin/sh

# ***************************************
# * LPU237 MSR TestMSR runner           *
# * Target : Debian 12, x64             *
# ***************************************

# POSIX sh 호환 방식으로 스크립트 디렉터리 탐색
# (bash 전용 ${BASH_SOURCE[0]} 사용 금지)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# TestSample/ 기준으로 경로 설정 (TestMsr64.bat 와 동일한 기준)
# 이 스크립트는 TestSample/ 에 위치 — debian_x64/ 서브디렉터리 아님
TESTSAMPLE_DIR="$SCRIPT_DIR"
JAVAPOS_DIR="$TESTSAMPLE_DIR/.."

JPOSLIB_DIR="$JAVAPOS_DIR/jposlib"
# LPU237LIB_DIR="$JAVAPOS_DIR/lpu237lib/debian_x64" ; jni so 가 있는 경로
LPU237LIB_DIR="/usr/share/elpusk/program/00000006/coffee_manager/so"

# ***************************************
# * Classpath 설정 (TestMsr64.bat 동일)  *
# ***************************************
CP="$JPOSLIB_DIR/jpos114.jar"
CP="$CP:$JPOSLIB_DIR/xerces.jar"
CP="$CP:$TESTSAMPLE_DIR"
CP="$CP:$JAVAPOS_DIR/lpu237lib/JposLpu237MsrSO.jar"

# ***************************************
# * JNI .so 경로                         *
# ***************************************
JAVA_LIB_PATH="$JAVAPOS_DIR/lpu237lib"

# ***************************************
# * jpos.xml 을 찾을 수 있도록            *
# * TestSample/ 디렉터리에서 실행         *
# ***************************************
cd "$TESTSAMPLE_DIR" || exit 1

java -cp "$CP" \
     -Djava.library.path="$JAVA_LIB_PATH" \
     TestMSR
