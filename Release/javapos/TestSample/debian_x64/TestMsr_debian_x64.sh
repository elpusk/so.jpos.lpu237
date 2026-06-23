#!/bin/bash

# ***************************************
# * LPU237 MSR TestMSR runner           *
# * Target : Debian 12, x64             *
# ***************************************

# 이 스크립트 위치(TestSample/) 기준으로 경로 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTSAMPLE_DIR="$SCRIPT_DIR/.."         # TestSample/
JAVAPOS_DIR="$TESTSAMPLE_DIR/.."        # javapos/

JPOSLIB_DIR="$JAVAPOS_DIR/jposlib"
LPU237LIB_DIR="$JAVAPOS_DIR/lpu237lib/debian_x64"

# ***************************************
# * Classpath 설정                       *
# ***************************************
CP="$JPOSLIB_DIR/jpos114.jar"
CP="$CP:$JPOSLIB_DIR/xerces.jar"
CP="$CP:$TESTSAMPLE_DIR"
CP="$CP:$LPU237LIB_DIR/../JposLpu237MsrSO.jar"

# ***************************************
# * JNI .so 경로 설정                    *
# * libtg_lpu237_jni.so 가 있는 디렉터리 *
# ***************************************
JAVA_LIB_PATH="$LPU237LIB_DIR"

# ***************************************
# * TestMSR 실행                         *
# ***************************************
java -cp "$CP" \
     -Djava.library.path="$JAVA_LIB_PATH" \
     TestMSR
