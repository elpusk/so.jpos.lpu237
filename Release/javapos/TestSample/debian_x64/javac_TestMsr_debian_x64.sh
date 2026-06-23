#!/bin/bash

# ***************************************
# * LPU237 MSR TestMSR compiler         *
# * Target : Debian 12, x64             *
# ***************************************

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTSAMPLE_DIR="$SCRIPT_DIR/.."
JAVAPOS_DIR="$TESTSAMPLE_DIR/.."
JPOSLIB_DIR="$JAVAPOS_DIR/jposlib"

# TestMSR.java 는 TestSample/ 에 있으므로 그 위치에서 컴파일
javac -cp "$JPOSLIB_DIR/jpos114.jar" \
      -d "$TESTSAMPLE_DIR" \
      "$TESTSAMPLE_DIR/TestMSR.java"
