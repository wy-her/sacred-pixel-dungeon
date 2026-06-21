# 077. 폰트 중복 제거 최적화

**날짜**: 2026-05-30

## 개요

빌드 에셋에서 중복된 웹 폰트(woff2)를 제거하여 번들 크기를 28MB 절감. Cloudflare 배포 및 Android APK 모두 적용.

---

## 변경 사항

### 1. 문제 분석

### 폰트 중복 현황

빌드 결과물에 동일한 woff2 폰트가 두 곳에 존재:

| 경로 | 용도 | 크기 |
|------|------|------|
| `fonts/` | CSS `@font-face`로 브라우저에 등록 | 28MB |
| `assets/fonts/` | TeaVM 프리로더 에셋 (preload.txt) | 28MB |

### 원인

1. **`core/src/main/assets/fonts/`**: 원본 게임 에셋 위치 (Shattered PD 기반)
2. **`teavm/webapp/fonts/`**: HTML5 빌드용 CSS 폰트 (별도 추가)
3. 빌드 시 양쪽 모두 복사되어 **28MB 중복 발생**

### 실제 사용 현황

```javascript
// index.html - CSS @font-face 사용 (fonts/ 경로)
fontsToLoad.push(['NotoSansKR', "url('fonts/noto-sans-kr.woff2')"]);
```

```java
// Java 코드에서 woff2 참조 없음
// pixel_font.png/ttf만 사용
Assets.PIXELFONT = "fonts/pixel_font.png";
```

**결론:** `assets/fonts/*.woff2`는 불필요 (preload=0으로 이미 비활성화됨)

### 2. 해결 방안

### 빌드 시 중복 폰트 자동 제거

`teavm/build.gradle`의 모든 빌드 태스크에 정리 로직 추가:

#### 2.1 preload.txt에서 woff2 항목 제거

**중요:** 파일만 삭제하면 프리로더가 preload.txt에 등록된 파일을 찾지 못해 **로딩이 멈춤**. 반드시 preload.txt에서 해당 항목도 제거해야 함.

```groovy
def preloadFile = file("${...}/dist/webapp/assets/preload.txt")
if (preloadFile.exists()) {
    def content = preloadFile.text
    // 음악 파일 프리로드 비활성화
    content = content.replaceAll('(i:b:/music/[^:]+:[^:]+):1', '$1:0')
    // woff2 폰트 항목 완전 제거 (fonts는 CSS로 로드)
    content = content.replaceAll('(?m)^i:b:/fonts/[^:]+\\.woff2:[^:]+:[01]\\r?\\n', '')
    content = content.replaceAll('(?m)^i:b:/fonts/OFL\\.txt:[^:]+:[01]\\r?\\n', '')
    preloadFile.text = content
}
```

#### 2.2 assets/fonts/ 디렉토리에서 woff2 삭제

```groovy
def assetsFontsDir = file("${...}/dist/webapp/assets/fonts")
if (assetsFontsDir.exists()) {
    assetsFontsDir.listFiles().each { f ->
        if (f.name.endsWith('.woff2') || f.name == 'OFL.txt') {
            f.delete()
        }
    }
}
```

### 적용 태스크

- `buildRelease`
- `buildDebug`
- `runRelease`
- `runDebug`

### 주의사항

| 실수 | 결과 |
|------|------|
| 파일만 삭제하고 preload.txt 수정 안 함 | **로딩 화면에서 멈춤** |
| preload.txt만 수정하고 파일 삭제 안 함 | 용량 절감 안 됨 |
| **두 작업 모두 수행** | ✅ 정상 동작 |

---

### 3. 빌드 결과 비교

### Cloudflare 번들

| 항목 | 이전 | 이후 | 절감 |
|------|------|------|------|
| **전체 크기** | 148MB | 120MB | **-28MB (19%)** |
| `assets/fonts/` | 28MB | 60KB | -28MB |
| `fonts/` | 28MB | 28MB | - |

### Android APK

| 항목 | 이전 | 이후 | 절감 |
|------|------|------|------|
| **APK 크기** | 116MB | 89MB | **-27MB (23%)** |

---

### 4. 최적화 후 구조

```
build/dist/webapp/
├── assets/
│   └── fonts/              # 60KB (필수 에셋만)
│       ├── pixel_font.png  # BitmapText 스프라이트 시트
│       └── pixel_font.ttf  # 픽셀 폰트 TTF
│
└── fonts/                  # 28MB (CSS용)
    ├── inter-full.woff2
    ├── noto-sans-v42-latin-regular.woff2
    ├── noto-sans-kr.woff2
    ├── noto-sans-jp.woff2
    ├── noto-sans-sc.woff2
    ├── noto-sans-tc.woff2
    └── OFL.txt
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `teavm/build.gradle` | 4개 빌드 태스크에 woff2/OFL.txt 삭제 로직 추가 |

---

## 빌드 로그 예시

```
> Task :teavm:buildRelease
Disabled preloading for music files in preload.txt
Disabled preloading for music files and removed woff2 font entries from preload.txt
Removed duplicate font: assets/fonts/inter-full.woff2
Removed duplicate font: assets/fonts/noto-sans-jp.woff2
Removed duplicate font: assets/fonts/noto-sans-kr.woff2
Removed duplicate font: assets/fonts/noto-sans-sc.woff2
Removed duplicate font: assets/fonts/noto-sans-tc.woff2
Removed duplicate font: assets/fonts/noto-sans-v42-latin-regular.woff2
Removed duplicate font: assets/fonts/OFL.txt

BUILD SUCCESSFUL
```

## 발견된 버그 및 수정

### 로딩 화면 멈춤 문제 (2026-05-30)

**문제:** 폰트 파일만 삭제하고 preload.txt 항목을 제거하지 않아 로딩 화면에서 멈춤

**원인:**
- preload.txt의 `/fonts/*.woff2` 경로는 `assets/fonts/` 디렉토리 참조
- 파일 삭제 후 프리로더가 존재하지 않는 파일을 찾으려 함 → 무한 대기

**해결:**
- preload.txt에서 woff2 항목을 완전히 제거하는 정규식 추가
- `replaceAll('(?m)^i:b:/fonts/[^:]+\\.woff2:[^:]+:[01]\\r?\\n', '')`

---
