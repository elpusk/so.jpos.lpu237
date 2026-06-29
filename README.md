# Javapos Service Object of lpu237

- Javapos Service Object of lpu237 Magnetic strip reader
- device io 는 windows 경우 tg_lpu237_dll.dll, 리눅스의 경우 libtg_lpu237_dll.so 를 사용
- IDE 는 vscode
- builder 는 gradle
- JDK 17 로 빌드 - [Temurin JDK](https://adoptium.net/temurin/releases)
- 지원 OS win11 x86, x64, Debian12 x64
- test 는 [JVM](https://en.wikipedia.org/wiki/Java_virtual_machine) 8, [Temurin JDK8](https://adoptium.net/temurin/releases)
- 기존 사용자 java application 지원 목적

## 허용된 작업

- 버그 수정
- 장비 Firmware 대응
- 새로운 Device 추가
- Logging 개선
- 성능 개선(호환성 유지 범위)

## 제한된 작업

- JavaPOS API 변경
- Event 동작 변경
- Property 의미 변경
- Exception 변경
- Java 9 이상 API 사용

## 프로젝트 구조

<img src="./so_jpos_lpu237_architecture.svg">


## 디렉터리 구성

``` text
so.jpos.lpu237/
├── setup_linux.sh    ← git clone 직후 Linux 에서 빌드 시 1회 실행
├── Source/pos-device-lpu237/     ← Java 프로젝트 (소스)
│   └── src/kr/co/elpusk/javapos/msr/
│       ├── Lpu237MSRService.java           ← 핵심 서비스 구현
│       └── Lpu237ServiceInstanceFactory.java ← 인스턴스 팩토리
└── Release/javapos/              ← 배포 패키지
    ├── jposlib/                  ← JavaPOS 공통 라이브러리 (jpos114.jar 등)
    │   ├── jpos.properties  ← Java Control Library 동작 설정
    │   ├── jpos.xml  ← JavaPOS에서 사용할 장치(Service Object) 등록
    │   ├── jpos114.jar  ← 과거에 Javapos.com 에서 배포하던 JavaPOS API(Contracts)
    │   ├── jpos114-controls.jar  ← 과거에 Javapos.com 에서 배포하던 JavaPOS Control 구현
│   │   └── xerces.jar  ← XML Parser 라이브러리
    │
    ├── lpu237lib/
    │   ├── JposLpu237MsrSO.jar  ← 빌드 결과물
    │   ├── x86/ tg_lpu237_jni.ini  ← debugging 용 tg_lpu237_jni.dll 설정 
    │   ├── x64/ tg_lpu237_jni.ini  ← debugging 용 tg_lpu237_jni.dll 설정
    │   └── debian_x64/ tg_lpu237_jni.ini  ← debugging 용 libtg_lpu237_jni.so 설정
    └── TestSample/               ← 테스트 코드 및 배치 파일
        ├── TestMSR.java  ←  JposLpu237MsrSO.jar 사용 예제.
        ├── javac_TestMsr.bat  ← win 에서 default javac 로 TestMSR.java 빌드
        ├── javac_TestMsr_debian_x64.sh  ← debian12 x64 에서 default javac 로 TestMSR.java 빌드
        ├── TestMsr_debian_x64.sh  ← debian12 x64 에서 TestMSR 실행
        ├── TestMsr32.bat  ← win 의 jvm x86 에서  TestMSR 실행
        └── TestMsr64.bat  ← win 의 jvm x64 에서  TestMSR 실행


```

## 핵심 클래스 역할

Lpu237MSRService — 전체 서비스의 핵심

|항목|내용|
|---|---|
|구현 인터페이스|MSRService111 (JavaPOS 1.11), JposServiceInstance, Runnable|
|서비스 버전|deviceServiceVersion = 1011000 (v1.11.0)|
|Native 연동|JNI 메서드 선언 (lpu237_open, lpu237_close, lpu237_wait_read 등)|
|스레드 구조|단일 Worker Thread + Command Queue (cmd_start_wait)|
|콜백 진입점|lpu237CallbackReadDone() — C++ JNI DLL이 호출하는 static 메서드|
|카드 파싱|analysisCardData() — ISO 1/2/3 트랙에서 PAN, 이름, 유효기간 등 추출|

Lpu237ServiceInstanceFactory — Reflection으로 서비스 인스턴스 생성, jpos.xml의 serviceClass 속성을 읽어서 인스턴스화

## 주요 설계 특징

Static 필드 공유 패턴 — claimed, worker, out_iso1/2/3, self_cur 등이 모두 static. 즉 JVM 내에서 동시에 하나의 인스턴스만 활성화 가능한 설계입니다.

## JNI 콜백 흐름

``` text
C++ DLL → lpu237CallbackReadDone() [static]
         → self_cur(현재 인스턴스)로 데이터 복사
         → callbacks.fireDataEvent() 호출
         → EnQ(cmd_start_wait) 로 다음 대기 재시작
```

## 빌드 환경

- Gradle 사용.

## Target

- Target Bytecode : Java 8
- Tested JVM : Java 8

## 빌드 방법

- 현재 directory 를 Source/pos-device-lpu237 directory 로 변경.
- win 에서 빌드 할 경우 : `./gradlew.bat build` 실행
- debian12  에서 빌드 할 경우 : `./gradlew build` 실행