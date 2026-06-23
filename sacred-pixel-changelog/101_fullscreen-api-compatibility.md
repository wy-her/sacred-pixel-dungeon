# 101. Fullscreen API 브라우저 호환성 수정

**날짜**: 2026-06-23

## 개요

`requestFullscreen' is undefined` 에러 수정. 브라우저 벤더 프리픽스(webkit, moz, ms) 지원 및 Promise 호환성 처리 추가.

---

## 변경 사항

### Bug Fixes (3)

---

### [B-1] TeaVMPlatformSupport.java - Fullscreen API 브라우저 호환성

**문제**: 특정 브라우저(Safari, Firefox 구버전, IE11)에서 `requestFullscreen' is undefined` 에러 발생

**원인 분석**:
- 표준 `requestFullscreen` API만 사용
- 브라우저별 벤더 프리픽스 미지원 (webkit, moz, ms)
- 구형 브라우저에서 Promise를 반환하지 않아 `.catch()` 호출 시 에러 발생

**파일**: `teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/TeaVMPlatformSupport.java`

**수정**: 벤더 프리픽스 폴백 + Promise 호환성 체크 + IIFE 제거

```java
// @JSBody는 이미 함수로 래핑하므로 IIFE 불필요
@JSBody(script =
    "var elem = document.documentElement;" +
    "var fullscreenElem = document.fullscreenElement || " +
    "                     document.webkitFullscreenElement || " +
    "                     document.mozFullScreenElement || " +
    "                     document.msFullscreenElement;" +
    "if (!fullscreenElem) {" +
    "  var requestFn = elem.requestFullscreen || " +
    "                  elem.webkitRequestFullscreen || " +
    "                  elem.mozRequestFullScreen || " +
    "                  elem.msRequestFullscreen;" +
    "  if (requestFn) {" +
    "    var result = requestFn.call(elem);" +
    "    if (result && typeof result.catch === 'function') {" +
    "      result.catch(function(e){ console.warn('Fullscreen request failed:', e); });" +
    "    }" +
    "  }" +
    "}")
private static native void jsRequestFullscreen();

@JSBody(script =
    "var fullscreenElem = document.fullscreenElement || " +
    "                     document.webkitFullscreenElement || " +
    "                     document.mozFullScreenElement || " +
    "                     document.msFullscreenElement;" +
    "if (fullscreenElem) {" +
    "  var exitFn = document.exitFullscreen || " +
    "               document.webkitExitFullscreen || " +
    "               document.mozCancelFullScreen || " +
    "               document.msExitFullscreen;" +
    "  if (exitFn) {" +
    "    var result = exitFn.call(document);" +
    "    if (result && typeof result.catch === 'function') {" +
    "      result.catch(function(e){ console.warn('Exit fullscreen failed:', e); });" +
    "    }" +
    "  }" +
    "}")
private static native void jsExitFullscreen();
```

**브라우저별 API 차이점**:

| 브라우저 | requestFullscreen | exitFullscreen | fullscreenElement |
|---------|-------------------|----------------|-------------------|
| Chrome/Edge | `requestFullscreen` | `exitFullscreen` | `fullscreenElement` |
| Firefox | `mozRequestFullScreen` | `mozCancelFullScreen` | `mozFullScreenElement` |
| Safari | `webkitRequestFullscreen` | `webkitExitFullscreen` | `webkitFullscreenElement` |
| IE 11 | `msRequestFullscreen` | `msExitFullscreen` | `msFullscreenElement` |

---

### [B-2] TeaVMPlatformSupport.java - IIFE 패턴 TeaVM 호환성

**문제**: TeaVM 컴파일 후 `SyntaxError: Function statements require a function name` 에러로 게임 미실행

**원인 분석**:
- `@JSBody` 내에서 IIFE 패턴 `(function() { ... })()` 사용
- TeaVM 컴파일러가 화살표 함수로 래핑: `() => { function() { ... }(); }`
- JavaScript strict mode에서 익명 함수 선언문 불가 → 문법 에러
- `app.js` 파싱 실패로 `main` 함수 미정의 → 게임 시작 불가

**에러 로그**:
```
Uncaught SyntaxError: Function statements require a function name (at app.js:326812:5)
Uncaught (in promise) ReferenceError: main is not defined
```

**TeaVM 생성 코드 (오류)**:
```javascript
csst_TeaVMPlatformSupport_jsRequestFullscreen$js_body$_4 = () => {
    function() {  // ← 함수 선언문에 이름 없음 = SyntaxError
        var elem = document.documentElement;
        ...
    }();
},
```

**파일**: `teavm/src/main/java/com/sacredpixel/sacredpixeldungeon/teavm/TeaVMPlatformSupport.java`

**수정**: IIFE 래퍼 제거 (`@JSBody`가 이미 함수로 래핑하므로 불필요)

```java
// 이전 (오류)
@JSBody(script = "(function() { ... })()")

// 수정 후 (정상)
@JSBody(script = "var elem = ...; if (...) { ... }")
```

---

### [B-3] appsintoss-app/index.html - iframe fullscreen 권한 추가

**문제**: iframe 내에서 fullscreen API 호출 시 `NotAllowedError` 발생 가능

**원인 분석**:
- iframe에 `allow="fullscreen"` 속성 없음
- 브라우저 보안 정책으로 iframe 내 fullscreen 차단

**파일**: `appsintoss-app/index.html`

**수정**: iframe에 `allow="fullscreen"` 속성 추가

```html
<!-- 이전 -->
<iframe id="game-frame" src="game/index.html"></iframe>

<!-- 수정 후 -->
<iframe id="game-frame" src="game/index.html" allow="fullscreen"></iframe>
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `teavm/.../TeaVMPlatformSupport.java:62-105` | 벤더 프리픽스 폴백 + IIFE 제거 + Promise 호환성 |
| `appsintoss-app/index.html:158` | iframe fullscreen 권한 추가 |

---
