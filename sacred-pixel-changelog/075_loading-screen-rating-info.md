# 075. 로딩 화면 게임 등급 정보 표시

**날짜**: 2026-05-29

## 개요

게임 시작 시 GRAC 게임 등급 정보를 3초간 스플래시 화면으로 표시한 후, libGDX 로딩 화면으로 페이드 아웃 전환.

---

## 변경 사항

### 1. 게임 등급 스플래시 화면 구현

**파일:** `teavm/webapp/index.html`

- 게임 등급 정보를 전체 화면 스플래시로 표시
- 3초 후 0.5초 페이드 아웃 애니메이션으로 사라짐
- 백그라운드에서 에셋 로딩 진행 (libGDX 프리로더)

```html
<div id="rating-splash">
    <img src="GRAC_Game_Grade_white_text.png" alt="게임 등급 정보">
</div>
```

### 2. 스플래시 화면 JavaScript 로직

**파일:** `teavm/webapp/index.html`

```javascript
// Rating splash screen - show for 3 seconds then fade out
(function() {
    var splash = document.getElementById('rating-splash');
    if (splash) {
        // After 3 seconds, start fade out
        setTimeout(function() {
            splash.classList.add('fade-out');
            // After fade animation completes, hide completely
            setTimeout(function() {
                splash.style.display = 'none';
            }, 500); // 0.5s fade duration
        }, 3000); // 3 second display
    }
})();
```

### 3. 스플래시 화면 CSS 스타일

**파일:** `teavm/webapp/styles.css`

```css
#rating-splash {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: #000;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10000;
    opacity: 1;
    transition: opacity 0.5s ease-out;
}

#rating-splash.fade-out {
    opacity: 0;
}

#rating-splash img {
    max-width: 90vw;
    max-height: 70vh;
    width: auto;
    height: auto;
}
```

### 4. 빌드 설정 업데이트

**파일:** `teavm/src/.../TeaVMBuilder.java`

```java
// 게임 등급 이미지 복사 추가
copyFile(new File(srcDir, "GRAC_Game_Grade_white_text.png"),
         new File(webappDir, "GRAC_Game_Grade_white_text.png"));
```

---

## 추가된 파일

| 파일 | 설명 |
|------|------|
| `teavm/webapp/GRAC_Game_Grade_white_text.png` | GRAC 12세 이용가 등급 이미지 (흰색 텍스트 포함) |

---

## 삭제된 항목

- `GRAC_Game_Grade.png` - 텍스트 없는 이전 버전
- `GRAC_Game_Grade_text.png` - 검은색 텍스트 버전 (가독성 문제)
- `TeaVMLauncher.hideRatingInfo()` - 더 이상 Java에서 호출하지 않음
- 커스텀 프리로더 HTML/CSS - 원래 libGDX 프리로더 사용

---

## 수정된 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `teavm/webapp/index.html` | 스플래시 화면 HTML + JavaScript 타이머 |
| `teavm/webapp/styles.css` | 스플래시 화면 스타일 + 페이드 아웃 애니메이션 |
| `teavm/src/.../TeaVMLauncher.java` | hideRatingInfo 호출/메서드 제거 |
| `teavm/src/.../TeaVMBuilder.java` | 새 이미지 파일 복사 경로 변경 |

---

## 동작 흐름

1. **0초**: 페이지 로드, 검은 배경에 게임 등급 이미지 표시
2. **0~3초**: 스플래시 화면 표시, 백그라운드에서 libGDX 에셋 프리로딩 진행
3. **3초**: 스플래시 화면 페이드 아웃 시작 (0.5초)
4. **3.5초**: 스플래시 완전히 숨김, libGDX 로고 및 로딩바 표시
5. **로딩 완료**: 게임 타이틀 화면으로 전환

---

## 설계 결정

### 왜 3초 타이머인가?

- 게임 등급 정보를 충분히 읽을 수 있는 시간
- 대부분의 에셋이 이 시간 내에 로딩됨
- 너무 길지 않아 사용자 경험 저해 최소화

### 왜 JavaScript 타이머인가?

- 에셋 로딩과 독립적으로 동작
- Java 코드 변경 없이 타이밍 조정 가능
- 페이드 아웃 애니메이션을 CSS transition으로 구현

### 왜 libGDX 프리로더를 그대로 사용하는가?

- 원래 잘 동작하던 기능을 재사용
- 로딩 진행률 표시가 이미 구현되어 있음
- 커스텀 구현의 복잡성 회피

---

## 빌드 완료 (2025-05-29)

### Cloudflare 릴리스 빌드

- **출력 경로:** `teavm/build/dist/webapp/`
- **버전:** `3.3.8-HTML5`
- **빌드 시간:** 약 2분 24초

### Android APK 빌드

- **출력 경로:** `capacitor-app/android/app/build/outputs/apk/release/app-release.apk`
- **파일 크기:** 약 121MB
- **빌드 시간:** 약 1분 36초

---
