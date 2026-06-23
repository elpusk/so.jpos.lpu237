# debian_x64 — LPU237 JNI 라이브러리 디렉터리 (Debian 12, x64)

## 이 디렉터리의 역할

`TestMsr_debian_x64.sh` 실행 시 JVM 이 `-Djava.library.path` 로 이 경로를 탐색합니다.  
JNI `.so` 파일과 INI 설정 파일이 함께 있어야 합니다.

---

## 배치해야 할 파일

git clone 후 아래 두 가지를 이 디렉터리에 직접 준비하십시오.

### 1) `libtg_lpu237_jni.so` (soname symlink)

보유 중인 `libtg_lpu237_jni.so.2.0.0` 을 이 디렉터리에 복사한 뒤 symlink 를 생성합니다.

```bash
# 이 디렉터리(debian_x64/)에서 실행
cp /path/to/libtg_lpu237_jni.so.2.0.0 .

ln -sf libtg_lpu237_jni.so.2.0.0 libtg_lpu237_jni.so.2
ln -sf libtg_lpu237_jni.so.2      libtg_lpu237_jni.so
```

JVM 은 `System.loadLibrary("tg_lpu237_jni")` 호출 시  
`libtg_lpu237_jni.so` → `libtg_lpu237_jni.so.2` → `libtg_lpu237_jni.so.2.0.0` 순으로 resolving 합니다.

### 2) `tg_lpu237_jni.ini` (이미 포함됨)

이 디렉터리에 이미 존재하는 `tg_lpu237_jni.ini` 를 확인하고,  
`[subcomponent_path]` 섹션의 `tg_lpu237_dll` 값이 실제 환경 경로와 맞는지 확인하십시오.

```ini
[subcomponent_path]
tg_lpu237_dll = /usr/share/elpusk/program/00000006/coffee_manager/so
```

---

## 최종 디렉터리 상태 (준비 완료 기준)

```
debian_x64/
├── libtg_lpu237_jni.so.2.0.0   ← 복사
├── libtg_lpu237_jni.so.2       ← symlink → libtg_lpu237_jni.so.2.0.0
├── libtg_lpu237_jni.so         ← symlink → libtg_lpu237_jni.so.2
├── tg_lpu237_jni.ini           ← 이미 존재 (경로 확인 필요)
└── README.md                   ← 이 파일
```

---

## `.gitignore` 안내

`.so` 바이너리는 git 으로 관리하지 않습니다.  
프로젝트 루트 `.gitignore` 에 아래 항목이 포함되어 있는지 확인하십시오.

```
*.so
*.so.*
```
