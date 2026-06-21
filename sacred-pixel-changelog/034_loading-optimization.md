# 034. 초기 로딩 최적화

**날짜**: 2026-04-02

## 개요

초기 로딩 시간을 대폭 단축하기 위해 음악 파일과 CJK 폰트를 lazy load로 변경.

---

## 변경 사항

### 예상 효과

| 사용자 | 변경 전 | 변경 후 | 절감 |
|--------|---------|---------|------|
| **영어** | ~100 MB | ~26 MB | **-74 MB (74%)** |
| **한국어** | ~100 MB | ~30 MB | **-70 MB (70%)** |
| **일본어** | ~100 MB | ~37 MB | **-63 MB (63%)** |
| **중국어** | ~100 MB | ~34 MB | **-66 MB (66%)** |

---

## 1. 음악 Lazy Load 버그 수정

### 문제
`build.gradle`의 regex에서 경로 앞 슬래시(`/`)가 누락되어 음악 파일의 lazy load 설정이 적용되지 않음.

### 수정
```groovy
# 변경 전
content.replaceAll('(i:b:music/[^:]+:[^:]+):1', '$1:0')

# 변경 후
content.replaceAll('(i:b:/music/[^:]+:[^:]+):1', '$1:0')
```

### 결과
- 31개 음악 파일 (48MB) → 필요 시 다운로드
- 타이틀 진입 시 타이틀 음악만 로드

**파일:** `teavm/build.gradle` (4곳 수정)

---

## 2. CJK 폰트 Lazy Load 설정

### 변경
CJK 폰트 4종을 lazy load로 변경:
- `noto-sans-jp.woff2` (11.4 MB) - 일본어
- `noto-sans-sc.woff2` (7.8 MB) - 중국어 간체
- `noto-sans-tc.woff2` (5.4 MB) - 중국어 번체
- `noto-sans-kr.woff2` (3.9 MB) - 한국어

### 추가 regex
```groovy
content.replaceAll('(i:b:/fonts/noto-sans-(jp|kr|sc|tc)\\.woff2:[^:]+):1', '$1:0')
```

**파일:** `teavm/build.gradle` (4곳 추가)

---

## 3. 언어별 폰트 선택 로딩

### 변경
브라우저 언어 설정에 따라 필요한 CJK 폰트만 로드.

### 로직
```javascript
var lang = navigator.language;
var langCode = lang.substring(0, 2).toLowerCase();

if (langCode === 'ko') → NotoSansKR 로드
if (langCode === 'ja') → NotoSansJP 로드
if (langCode === 'zh') {
    if (TW/HK/MO/Hant) → NotoSansTC 로드
    else → NotoSansSC 로드
}
```

### On-Demand 로딩 API
```javascript
// 언어 변경 시 폰트를 동적으로 로드할 수 있는 함수 추가
window._spdLoadFont(fontName, fontUrl)
```

**파일:** `teavm/webapp/index.html`

---

## 4. 빌드 설정 개선

### 추가
- `styles.css` 복사를 `buildRelease`, `buildDebug` 태스크에 추가

**파일:** `teavm/build.gradle`

---

## 검증 결과

### preload.txt 확인
```bash
# 음악 파일 - :0 (lazy load)
i:b:/music/caves_1.mp3:768083:0
i:b:/music/caves_2.mp3:1456442:0

# CJK 폰트 - :0 (lazy load)
i:b:/fonts/noto-sans-jp.woff2:11440444:0
i:b:/fonts/noto-sans-kr.woff2:3908600:0
i:b:/fonts/noto-sans-sc.woff2:7781820:0
i:b:/fonts/noto-sans-tc.woff2:5424268:0

# Latin 폰트 - :1 (프리로드 유지)
i:b:/fonts/noto-sans-v42-latin-regular.woff2:13120:1
```

---

## 수정 파일 목록

| 파일 | 변경 |
|------|------|
| `teavm/build.gradle` | 음악 regex 수정, CJK 폰트 lazy load 추가, styles.css 복사 추가 |
| `teavm/webapp/index.html` | 언어별 폰트 선택 로딩, `_spdLoadFont()` API 추가 |

---

## 주의사항

1. **음악**: 첫 재생 시 1-2초 로딩 대기 발생 가능
2. **폰트**: 언어 변경 시 해당 CJK 폰트 다운로드 필요 (일회성)
3. **캐시**: 한 번 로드된 에셋은 브라우저 캐시에 저장됨
